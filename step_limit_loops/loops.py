import json,csv,re,sys,collections
base=sys.argv[1]
def sig(out):
    s=set()
    for m in re.finditer(r"\[ERROR\] /\S+\.java:\[\d+,\d+\] (.+)",out): s.add("C:"+m.group(1).strip()[:80])
    for m in re.finditer(r"\b((?:[a-z]+\.)+[A-Z]\w*(?:Exception|Error))\b",out): s.add("E:"+m.group(1).split(".")[-1])
    for m in re.finditer(r"\b(\w+(?:Exception|Error))\b(?::| )",out):
        if m.group(1)[0].isupper() and m.group(1) not in ("ERROR","BUILD","AssertionFailedError"): s.add("E:"+m.group(1))
    if "AssertionError" in out or "expected:<" in out or "AssertionFailedError" in out: s.add("ASSERT")
    if "BUILD SUCCESS" in out: s.add("OK")
    return frozenset(s)
rows=[]
for c in ["A","F","FC","FCV"]:
    for r in csv.DictReader(open(f"{base}/out_{c}/generation_results.csv")):
        if r["exit_status"]!="LimitsExceeded": continue
        t=json.load(open(f"{base}/out_{c}/runs/row_{int(r['row_number']):03d}/trajectory.json"))
        sigs=[]
        msgs=t["messages"]
        for i,m in enumerate(msgs):
            if m.get("role")=="tool":
                out=str(m.get("content"))
                if "Building " in out or "Tests run:" in out or "COMPILATION ERROR" in out or "BUILD FAILURE" in out:
                    sigs.append(sig(out))
        n=len(sigs)
        fails=[s for s in sigs if "OK" not in s and s]
        top,cnt=(collections.Counter(fails).most_common(1) or [(frozenset(),0)])[0]
        streak=best=0;prev=None
        for s in sigs:
            streak=streak+1 if s==prev and s and "OK" not in s else 1; prev=s; best=max(best,streak)
        share=sum(1 for s in fails if s & top)
        rows.append((c,r["row_number"],r["selected_method"].split(".")[-1],n,cnt,share,best,sorted(top)[:3]))
for x in rows: print(*x,sep=" | ")
