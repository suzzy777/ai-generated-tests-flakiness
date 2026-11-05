import argparse, csv, os, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
import batch_reproflake_chatunitest_graylog as B
args = argparse.Namespace(repo=(Path.home() / ".m2" / "repository").resolve(), timeout=3600)
for r in csv.DictReader(open("manifest.csv")):
    env = os.environ.copy()
    jdk = B.find_jdk(r["java"].strip())
    if jdk:
        env["JAVA_HOME"] = jdk; env["PATH"] = str(Path(jdk) / "bin") + os.pathsep + env["PATH"]
    d = Path("runs") / ("row_" + r["row_number"].zfill(3)); d.mkdir(parents=True, exist_ok=True)
    print(r["row_number"], r["zip"], B.build_module(r, env, args, d), flush=True)
