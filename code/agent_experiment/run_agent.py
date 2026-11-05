#!/usr/bin/env python3
import argparse
import csv
import json
import os
from pathlib import Path
import shutil
import sys

import yaml
from minisweagent.agents.default import DefaultAgent
from minisweagent.environments.local import LocalEnvironment
from minisweagent.models.litellm_model import LitellmModel

E = Path(__file__).resolve().parent
H = E.parent
sys.path.insert(0, str(H / "prompt_experiment"))
import batch_reproflake_chatunitest as B
import collect_nonflaky as C

FIELDS = ["row_number", "zip", "flaky_test", "selected_method", "junit", "chatunitest_mode", "status",
          "exit_status", "steps", "cost_usd", "test_classes", "other_changes", "overload_match", "test_output"]


def snapshot(root):
    out = {}
    for dp, dns, fns in os.walk(root):
        dns[:] = [d for d in dns if d not in ("target", ".git", ".dtfixingtools", ".nondex")]
        for f in fns:
            p = Path(dp) / f
            try:
                s = p.lstat()
            except OSError:
                continue
            out[str(p.relative_to(root))] = (s.st_size, s.st_mtime_ns)
    return out


def prompt_for(row):
    focal = row["selected_method"].replace("#", "_")
    d = E / "prompts" / focal
    if not (d / "user.txt").is_file():
        src = sorted((H / "prompt_experiment" / "out_default" / "runs" / f"row_{int(row['row_number']):03d}" / "tmp")
                     .rglob("attempt0/records.json"))
        if not src:
            sys.exit(f"no ChatUniTest prompt for row {row['row_number']}")
        msgs = json.loads(src[0].read_text())[0]["prompt"]
        d.mkdir(parents=True, exist_ok=True)
        (d / "system.txt").write_text(msgs[0]["content"], encoding="utf-8")
        (d / "user.txt").write_text(msgs[1]["content"], encoding="utf-8")
        (d / "source.txt").write_text(str(src[0].relative_to(H)) + "\n", encoding="utf-8")
    return (d / "system.txt").read_text(encoding="utf-8"), (d / "user.txt").read_text(encoding="utf-8")


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--condition", choices=["A", "B", "T", "F", "N", "FC", "FCV"], required=True)
    a.add_argument("--only", action="append", default=[], help="Only these row numbers")
    a.add_argument("--redo", action="store_true")
    args = a.parse_args()
    if not os.environ.get("OPENAI_API_KEY"):
        sys.exit("export OPENAI_API_KEY first")
    cfg = yaml.safe_load((E / "agent_config.yaml").read_text())
    out = E / f"out_{args.condition}"
    out.mkdir(exist_ok=True)
    (out / "extracted").exists() or (out / "extracted").symlink_to(E / "extracted")
    repo = (Path.home() / ".m2" / "repository").resolve()
    mvn_bin = C.maven_without_extensions(E)
    res_path = out / "generation_results.csv"
    done = {r["row_number"]: r for r in csv.DictReader(open(res_path))} if res_path.is_file() else {}

    rows = list(csv.DictReader(open(E / "manifest.csv", encoding="utf-8")))
    for row in rows:
        rn = row["row_number"]
        if args.only and rn not in args.only:
            continue
        if rn in done and not args.redo:
            print(f"row {rn}: done ({done[rn]['status']})")
            continue
        run_dir = out / "runs" / f"row_{int(rn):03d}"
        shutil.rmtree(run_dir, ignore_errors=True)
        (run_dir / "tests").mkdir(parents=True)
        work = E / "work" / f"{args.condition}_row_{int(rn):03d}"
        shutil.rmtree(work, ignore_errors=True)
        print(f"row {rn}: copying {row['zip']}", flush=True)
        shutil.copytree(E / "extracted" / row["zip"], work, symlinks=True)
        try:
            _, module_dir, _ = B.locate_test(work, row["module"], row["flaky_test"])
            mvn_root, pl = C.project_root(module_dir, C.norm_module(row["module"]))
            cls, method = row["selected_method"].split("#")
            pkg, simple = cls.rsplit(".", 1)
            test_class = f"{simple}_{method}_0_0_Test"
            test_path = module_dir / "src" / "test" / "java" / Path(*pkg.split(".")) / (test_class + ".java")
            existing = test_path.parent / (simple + "Test.java")
            if args.condition == "T" and not existing.is_file():
                raise FileNotFoundError(f"no existing test {existing}")
            skips = " ".join(s for s in B.BUILD_SKIPS if s != "-DskipTests")
            old = " -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false" if args.condition == "A" else ""
            base = f"mvn -B -o test {' '.join(pl)} {skips}{old} -Dmaven.repo.local={repo}"
            jdk = B.find_jdk(row["java"])
            env = {"JAVA_HOME": jdk, "PATH": os.pathsep.join(
                [str(Path(jdk) / "bin")] + ([str(mvn_bin)] if mvn_bin else []) + [os.environ["PATH"]])}
            env.update(cfg["environment"].get("env", {}))
            if row["module"] not in ("", "."):
                (module_dir / ".batch_built").unlink(missing_ok=True)
                built = B.build_module({"module": row["module"], "module_dir": str(module_dir)},
                                       {**os.environ, **env}, argparse.Namespace(repo=repo, timeout=3600),
                                       run_dir, "sibling_build.log")
                print(f"row {rn}: siblings of {row['module']}: {built}", flush=True)
                if built != "built":
                    raise RuntimeError(f"sibling build {built}, see {run_dir / 'sibling_build.log'}")
            system, user = prompt_for(row)
            junit = "JUnit 4" if row["junit"] == "4" else "JUnit 5"
            tv = dict(chatunitest_system=system, chatunitest_user=user, project_root=str(mvn_root),
                      module_dir=str(module_dir), test_path=str(test_path), test_class=test_class,
                      package=pkg, junit=junit, java=row["java"], condition=args.condition,
                      existing_test=str(existing.relative_to(mvn_root)),
                      test_dir=str((module_dir / "src" / "test" / "java").relative_to(mvn_root)),
                      test_cmd=f"cd {mvn_root} && {base} -Dtest={test_class} 2>&1 | tail -n 80",
                      suite_cmd=f"cd {mvn_root} && {base} 2>&1 | tail -n 80")
            before = snapshot(work)
            model = LitellmModel(**cfg["model"])
            envr = LocalEnvironment(cwd=str(mvn_root), env=env, timeout=cfg["environment"]["timeout"])
            agent = DefaultAgent(model, envr, output_path=run_dir / "trajectory.json", **cfg["agent"])
            agent.extra_template_vars |= tv
            print(f"row {rn}: agent ({args.condition}) on {row['selected_method']}, JDK {row['java']}", flush=True)
            try:
                info = agent.run(task=row["selected_method"])
                exit_status = info.get("exit_status", "")
            except Exception as ex:
                exit_status = f"error: {type(ex).__name__}: {str(ex)[:200]}"
            after = snapshot(work)
            changed = sorted(p for p in set(before) | set(after) if before.get(p) != after.get(p))
            src_test = Path("src") / "test" / "java"
            kept, other = [], []
            for p in changed:
                full = work / p
                rel_mod = full.relative_to(module_dir) if full.is_relative_to(module_dir) else None
                if (p not in before and full.is_file() and p.endswith("_Test.java") and rel_mod
                        and rel_mod.parts[:3] == src_test.parts):
                    dest = run_dir / "tests" / rel_mod.relative_to(src_test)
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(full, dest)
                    kept.append(full.stem)
                else:
                    other.append(("new " if p not in before else "deleted " if p not in after else "changed ") + p)
            (run_dir / "changes.txt").write_text("".join(x + "\n" for x in other), encoding="utf-8")
            status = "generated" if kept else "no_tests"
            rec = {"steps": agent.n_calls, "cost_usd": f"{agent.cost:.4f}", "exit_status": exit_status}
        except Exception as ex:
            status, kept, other, rec = "setup_failed", [], [], {"exit_status": str(ex)[:300]}
        finally:
            shutil.rmtree(work, ignore_errors=True)
        done[rn] = dict(row_number=rn, zip=row["zip"], flaky_test=row["flaky_test"],
                        selected_method=row["selected_method"], junit=row["junit"],
                        chatunitest_mode=row["junit"], status=status, test_classes=";".join(kept),
                        other_changes=len(other), overload_match="agent", test_output=str(run_dir / "tests"), **rec)
        B.csv_write(res_path, sorted(done.values(), key=lambda r: int(r["row_number"])), FIELDS)
        print(f"row {rn}: {status} {kept} steps={rec.get('steps')} cost=${rec.get('cost_usd')} "
              f"exit={rec.get('exit_status')} other_changes={len(other)}", flush=True)


if __name__ == "__main__":
    main()
