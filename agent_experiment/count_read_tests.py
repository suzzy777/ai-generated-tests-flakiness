import csv, json, re, sys
from pathlib import Path

E = Path(__file__).resolve().parent
OPEN = re.compile(r"\b(cat|sed|head|tail|less|more|nl|awk|grep)\b[^|;&]*?(\S*src/test/\S*?\.java)")
LOOK = re.compile(r"\b(find|ls|tree|grep)\b[^|;&]*src/test")


def classify(traj, own):
    cmds = []
    for m in json.loads(traj.read_text())["messages"]:
        for t in m.get("tool_calls") or []:
            try:
                cmds.append(json.loads(t["function"]["arguments"]).get("command", ""))
            except Exception:
                pass
    opened = sorted({Path(f).name for c in cmds if not c.lstrip().startswith("cat >")
                     for _, f in OPEN.findall(c) if Path(f).stem != own})
    if opened:
        return "read", opened
    return ("searched" if any(LOOK.search(c) for c in cmds) else "no"), []


def main():
    rows = []
    for c in sys.argv[1:] or ["A", "F", "FC", "FCV"]:
        gen = E / f"out_{c}" / "generation_results.csv"
        if not gen.is_file():
            continue
        for g in csv.DictReader(open(gen)):
            run = E / f"out_{c}" / "runs" / f"row_{int(g['row_number']):03d}"
            traj = run / "trajectory.json"
            if not traj.is_file():
                continue
            own = [p.stem for p in (run / "tests").rglob("*_Test.java")]
            how, files = classify(traj, own[0] if own else "")
            rows.append({"condition": c, "row": g["row_number"], "zip": g["zip"], "status": g["status"],
                         "read_tests": how, "files_opened": " ".join(files)})
    with open(E / "read_tests.csv", "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0]))
        w.writeheader()
        w.writerows(rows)
    for c in dict.fromkeys(r["condition"] for r in rows):
        rs = [r for r in rows if r["condition"] == c]
        n = {k: sum(r["read_tests"] == k for r in rs) for k in ("read", "searched", "no")}
        print(f"{c}: {len(rs)} runs, read {n['read']}, only searched {n['searched']}, no {n['no']}")
        for r in rs:
            print(f"  row {r['row']:>2} {r['zip'][:34]:34} {r['read_tests']:8} {r['files_opened'][:90]}")


if __name__ == "__main__":
    main()
