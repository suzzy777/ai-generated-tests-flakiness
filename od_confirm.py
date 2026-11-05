#!/usr/bin/env python3
import argparse
import csv
import os
from pathlib import Path
import shutil
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
import flaky_check_basic as F
import collect_nonflaky as C

FIELDS = ["id", "zip", "module", "test", "generated", "idflakies_type", "category", "victim", "brittle",
          "polluter", "state_setter", "polluter_generated", "state_setter_generated", "alone", "with_candidates",
          "full_order", "repeat", "maven_calls", "note"]
FAILED = ("failed",)


def candidates(t, before, other, fqcn):
    seen = set(other)
    out = [x for x in before[:-1] if x not in seen and x != t]
    return out if F.generated_name(t, fqcn) else [x for x in out if F.generated_name(x, fqcn)]


class Runner:
    def __init__(self, p, args):
        self.p, self.args, self.calls, self.first = p, args, 0, True
        self.logs = p.row_dir / "logs"
        self.logs.mkdir(parents=True, exist_ok=True)

    def run(self, order, tag):
        p = self.p
        shutil.rmtree(p.module_dir / "target" / "surefire-reports", ignore_errors=True)
        listing = self.logs / f"{tag}.tests.txt"
        listing.write_text("\n".join(order) + "\n")
        test = ",".join(order) if len(",".join(order)) < 4000 else str(listing.resolve())
        log = self.logs / f"{tag}.log"
        code = p.mvn(["test", "-Dsurefire.runOrder=testorder", "-Dtest=" + test, *F.TEST_FLAGS], log,
                     offline=not self.first and self.args.offline)
        self.first = False
        self.calls += 1
        if "Address already in use" in log.read_text(encoding="utf-8", errors="replace"):
            return "port_in_use"
        tests, _ = C.read_reports(p.module_dir)
        return tests.get(order[-1]) or ("timeout" if code == "timeout" else "missing")

    def bisect(self, cands, t, want_fail, tag):
        seen = []
        r = self.run(cands + [t], f"{tag}_all")
        seen.append(r)
        if (r in FAILED) != want_fail or r not in ("passed", "failed"):
            return None, seen
        n = 0
        while len(cands) > 1:
            half = len(cands) // 2
            for part in (cands[:half], cands[half:]):
                n += 1
                r = self.run(part + [t], f"{tag}_part{n}")
                seen.append(r)
                if (r in FAILED) == want_fail and r in ("passed", "failed"):
                    cands = part
                    break
            else:
                break
        return cands, seen


def check(run, t, typ, passing, failing, fqcn, alone_cache, full_order):
    rec = {"test": t, "generated": str(bool(F.generated_name(t, fqcn))).lower(), "idflakies_type": typ}
    key = t
    if key in alone_cache:
        alone = alone_cache[key]
    else:
        alone = run.run([t], "alone_" + t.rsplit(".", 1)[-1])
        if alone in ("passed", "failed"):
            alone_cache[key] = alone
    rec["alone"] = alone
    if alone not in ("passed", "failed"):
        rec.update(category="inconclusive", note=f"T alone: {alone}")
        return rec
    victim = alone == "passed"
    order, other = (failing, passing) if victim else (passing, failing)
    cands = candidates(t, order, other, fqcn)
    found, seen = run.bisect(cands, t, victim, "with_" + t.rsplit(".", 1)[-1]) if cands else (None, [])
    rec["with_candidates"] = ";".join(seen)
    role = "polluter" if victim else "state_setter"
    if found:
        rep_alone = run.run([t], "repeat_alone_" + t.rsplit(".", 1)[-1])
        rep_with = run.run(found + [t], "repeat_with_" + t.rsplit(".", 1)[-1])
        rec["repeat"] = f"{rep_alone};{rep_with}"
        if rep_alone == alone and (rep_with in FAILED) == victim and rep_with in ("passed", "failed"):
            rec.update(category="victim" if victim else "brittle", **{("victim" if victim else "brittle"): t,
                       role: ";".join(found), role + "_generated": str(all(F.generated_name(x, fqcn) for x in found)).lower()})
        else:
            rec.update(category="inconclusive", note="not the same when repeated")
        return rec
    if not (full_order or F.generated_name(t, fqcn)):
        rec.update(category="not_by_generated", note=f"T {'passed' if victim else 'failed'} alone and also after the generated test(s)" if cands
                   else f"T {'passed' if victim else 'failed'} alone; no generated test ran before it only in iDFlakies' {'failing' if victim else 'passing'} order")
        return rec
    full = run.run(order, "full_" + t.rsplit(".", 1)[-1])
    rec["full_order"] = full
    if full in ("passed", "failed") and (full in FAILED) == victim:
        rec.update(category="victim" if victim else "brittle", **{("victim" if victim else "brittle"): t},
                   note=f"{role} not among the {len(cands)} candidate(s)"
                        + (" (tests run before it in that order only)" if F.generated_name(t, fqcn) else " (generated tests)"))
    else:
        rec.update(category="not_reproduced", note=f"iDFlakies' {'failing' if victim else 'passing'} order gave {full}")
    return rec


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--tests", type=Path, default=F.HERE / "task_csvs" / "flaky_generated_tests.csv")
    a.add_argument("--archive-dir", type=Path, action="append")
    a.add_argument("--idflakies-out", type=Path, default=F.HERE / "flaky_check_out" / "idflakies")
    a.add_argument("--out", type=Path, default=F.HERE / "flaky_check_out" / "od_confirm")
    a.add_argument("--maven-repo", dest="repo", type=Path, default=Path.home() / ".m2" / "repository")
    a.add_argument("--offline", action="store_true", help="mvn -o after a row's first Maven call")
    a.add_argument("--timeout", type=int, default=1800, help="Seconds per Maven call (default 1800)")
    a.add_argument("--only", action="append", default=[], help="Only runs whose id, zip or generated test contains this text")
    a.add_argument("--test", action="append", default=[], help="Only flagged tests containing this text")
    a.add_argument("--full-order", action="store_true",
                   help="Also run iDFlakies' whole order for existing tests the generated tests do not explain")
    args = a.parse_args()
    args.command = "od_confirm"
    args.tests = args.tests.resolve()
    args.archive_dir = [d.resolve() for d in (args.archive_dir or [F.HERE / "out_part1" / "extracted",
                                                                   F.HERE / "out_part2" / "extracted"])]
    args.repo, args.out = args.repo.resolve(), args.out.resolve()
    args.out.mkdir(parents=True, exist_ok=True)
    args.env = os.environ.copy()
    out_csv = args.out / "od_confirm.csv"
    done = {}
    if out_csv.is_file():
        done = {(r["id"], r["test"]): r for r in csv.DictReader(open(out_csv, encoding="utf-8"))}
    alone_by_module = {}
    for r in done.values():
        if r["alone"] in ("passed", "failed"):
            alone_by_module.setdefault((r["zip"], r["module"]), {})[r["test"]] = r["alone"]
    rows = F.load_tests(args)
    for i, (rid, root, g, m, fix) in enumerate(rows, 1):
        dt = args.idflakies_out / "rows" / rid / "dtfixingtools"
        p = F.Prepared(rid, root, g, m, fix, args)
        flagged = {F.generated_name(k, [k.split("#")[0] if "#" in k else k.rpartition(".")[0]]) or k: v
                   for k, v in F.idflakies_flagged(dt).items()}
        todo = []
        for t, passing, failing in F.flagged_orders(dt):
            if args.test and not any(x in t for x in args.test):
                continue
            if F.generated_name(t, p.fqcn) or candidates(t, failing, passing, p.fqcn) or candidates(t, passing, failing, p.fqcn):
                todo.append((t, flagged.get(t, ""), passing, failing))
        left = [x for x in todo if (rid, x[0]) not in done]
        print(f"[{i}/{len(rows)}] {rid} {g['zip']}: {len(todo)} test(s) to check, {len(left)} left", flush=True)
        if not left:
            continue
        cache = alone_by_module.setdefault((g["zip"], m["module"]), {})
        with p:
            if p.build() in ("build_failed", "build_timeout"):
                print("  build failed, skipped")
                continue
            run = Runner(p, args)
            for t, typ, passing, failing in left:
                before = run.calls
                rec = check(run, t, typ, passing, failing, p.fqcn, cache, args.full_order)
                rec.update(id=rid, zip=g["zip"], module=m["module"], maven_calls=run.calls - before)
                done[(rid, t)] = rec
                B_write(out_csv, done)
                print(f"  {t}: {rec['category']}" + (f" (polluter {rec['polluter']})" if rec.get("polluter") else "")
                      + (f" (state-setter {rec['state_setter']})" if rec.get("state_setter") else ""), flush=True)


def B_write(path, done):
    F.B.csv_write(path, sorted(done.values(), key=lambda r: (r["id"], r["test"])), FIELDS)


if __name__ == "__main__":
    main()
