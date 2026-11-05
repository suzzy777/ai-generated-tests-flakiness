#!/usr/bin/env python3
import argparse
import csv
import fcntl
import os
import re
import shutil
import signal
import subprocess
import sys
import time
import types
import zipfile
from collections import Counter, OrderedDict, defaultdict
from pathlib import Path

HOME = Path.home()
PLUGIN = "edu.utexas.ece:flakesync-maven-plugin:1.0-SNAPSHOT"
HADOOP_FAMILY = ("HDFS-", "HADOOP-", "YARN-", "HBASE-")
NAME_OK = re.compile(r"^[A-Za-z0-9._=+-]+$")
TEST_ID_OK = re.compile(r"^[A-Za-z_$][\w.$]*#[A-Za-z_$][\w$]*$")
RE_CLASS_LINE = re.compile(
    r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+), Time elapsed: [^\n]*? - in ")
RE_LINKAGE = re.compile(r'duplicate class definition for name:\s*"([^"]+)"')
REQUIRED = ["project", "sha", "module", "generated_test", "focal_method", "zip", "java", "junit",
            "mockito_fix", "focal_from_test", "file_here"]
FIELDS = ["folder", "file", "java", "junit", "zip", "module", "mockito_fix", "test", "setup", "build",
          "cf_status", "cf_tests_run", "cf_problem", "linkage_classes", "rm_total", "rm_in_module",
          "module_classes", "module_classes_blacklisted", "focal_blacklisted", "dl_status", "dl_runs",
          "dl_last_delay", "dl_locations", "secs", "note"]
BAD_STATUSES = ("build-failure", "no-result", "script-error", "timeout")
DONE_CF = ("pass", "fail", "error", "no-tests", "timeout")
BUILD_OK = ("built", "cached", "not_needed")


def say(msg):
    print(time.strftime("%H:%M:%S"), msg, flush=True)


def kill_group(p):
    try:
        os.killpg(p.pid, signal.SIGTERM)
    except ProcessLookupError:
        return
    try:
        p.wait(timeout=20)
    except subprocess.TimeoutExpired:
        pass
    try:
        os.killpg(p.pid, signal.SIGKILL)
    except ProcessLookupError:
        pass
    try:
        p.wait(timeout=10)
    except Exception:
        pass


def sh(cmd, cwd, log, timeout_min, env=None):
    log.parent.mkdir(parents=True, exist_ok=True)
    with open(log, "w", encoding="utf-8", errors="replace") as f:
        f.write("$ " + " ".join(cmd) + "\n(cwd: %s)\n\n" % cwd)
        f.flush()
        p = subprocess.Popen(cmd, cwd=str(cwd), env=env, stdout=f, stderr=subprocess.STDOUT,
                             stdin=subprocess.DEVNULL, start_new_session=True)
        try:
            return p.wait(timeout=timeout_min * 60), False
        except subprocess.TimeoutExpired:
            kill_group(p)
            return -9, True
        except BaseException:
            kill_group(p)
            raise


def read_text(p):
    try:
        return Path(p).read_text(encoding="utf-8", errors="replace")
    except OSError:
        return ""


RE_TOTAL_LINE = re.compile(
    r"^(?:\[[A-Z]+\]\s*)?Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)[ \t]*$", re.M)


def surefire_runs(text):
    totals = [tuple(int(x) for x in m.groups()) for m in RE_TOTAL_LINE.finditer(text)]
    if totals:
        return totals
    return [tuple(int(x) for x in m.groups()) for m in RE_CLASS_LINE.finditer(text)]


def verdict(runs, text):
    last_ok = bool(runs) and runs[-1][0] > 0 and runs[-1][2] == 0 and runs[-1][1] == 0
    if RE_LINKAGE.search(text) and not last_ok:
        return "error"
    if not runs:
        if "No tests were executed" in text or "No tests to run" in text:
            return "no-tests"
        if "BUILD FAILURE" in text:
            return "build-failure"
        return "no-result"
    tests, fails, errors, skipped = runs[-1]
    if errors:
        return "error"
    if fails:
        return "fail"
    if tests == 0 or tests == skipped:
        return "no-tests"
    return "pass"


def first_problem(text):
    lines = text.splitlines()
    for i, l in enumerate(lines):
        bare = re.sub(r"^\[[A-Z]+\]\s*", "", l)
        if ("<<< ERROR!" in l or "<<< FAILURE!" in l) and " - in " not in l and not bare.startswith("Tests run:"):
            nxt = lines[i + 1].strip() if i + 1 < len(lines) else ""
            return nxt[:200]
    for l in lines:
        if "LinkageError" in l or "duplicate class definition" in l:
            return l.strip()[:200]
    for l in lines:
        if "Failed to execute goal" in l:
            return l.replace("[ERROR]", "").strip()[:200]
    return ""


def read_blacklist(path):
    try:
        return [l.rstrip("\n") for l in open(path, encoding="utf-8") if l.strip()]
    except OSError:
        return []


def blacklisted_by(dotted, bl):
    for prefix in bl:
        if dotted.startswith(prefix):
            return prefix
    return ""


def module_classes(module_dir):
    base = module_dir / "target" / "classes"
    out = set()
    if base.is_dir():
        for p in base.rglob("*.class"):
            if "Tests" in str(p):
                continue
            out.add(p.relative_to(base).as_posix()[:-6])
    return out


def resultmethods_stats(path, mod_classes):
    total = in_mod = 0
    for line in read_text(path).splitlines():
        line = line.strip()
        if not line:
            continue
        total += 1
        cls = line.split("(")[0].rsplit(".", 1)[0]
        if cls in mod_classes:
            in_mod += 1
    return total, in_mod


def zip_ok(p):
    try:
        with zipfile.ZipFile(p):
            return True
    except (OSError, zipfile.BadZipFile):
        return False


def download(url, dest):
    if not (url.startswith("https://zenodo.org/records/") and url.endswith(".zip")):
        raise RuntimeError("refusing unexpected archive URL: " + url)
    dest.parent.mkdir(parents=True, exist_ok=True)
    for attempt in range(1, 7):
        if dest.is_file() and zip_ok(dest):
            return
        say(f"  downloading {dest.name} (try {attempt}) ...")
        rc = subprocess.run(["curl", "-sSL", "-C", "-", "--connect-timeout", "30", "--speed-limit", "1000",
                             "--speed-time", "60", "-o", str(dest), url], stdin=subprocess.DEVNULL).returncode
        if rc == 33:
            dest.unlink(missing_ok=True)
        elif rc == 0 and dest.is_file() and not zip_ok(dest):
            dest.unlink(missing_ok=True)
    if not (dest.is_file() and zip_ok(dest)):
        raise RuntimeError("download failed / zip unreadable: " + dest.name)


def load_units(tests_dir):
    units, problems = OrderedDict(), []
    if not tests_dir.is_dir():
        return [], [f"tests folder not found: {tests_dir}"]
    folders = sorted(p for p in tests_dir.iterdir() if p.is_dir() and (p / "tests.csv").is_file())
    if not folders:
        problems.append(f"no <folder>/tests.csv found in {tests_dir}")
    for d in folders:
        for n, r in enumerate(csv.DictReader(open(d / "tests.csv", newline="", encoding="utf-8")), 2):
            where = f"{d.name}/tests.csv line {n}"
            miss = [c for c in REQUIRED if not (r.get(c) or "").strip()]
            if miss:
                problems.append(f"{where}: empty/missing column(s) {miss}")
                continue
            r = {k: (v or "").strip() for k, v in r.items() if k is not None}
            if r["java"] not in ("8", "11") or r["junit"] not in ("4", "5"):
                problems.append(f"{where}: java={r['java']!r} junit={r['junit']!r} (expected 8/11 and 4/5)")
            if not NAME_OK.match(r["zip"]):
                problems.append(f"{where}: unexpected archive name {r['zip']!r}")
            if not TEST_ID_OK.match(r["generated_test"]):
                problems.append(f"{where}: generated_test {r['generated_test']!r} is not pkg.Class#method")
                continue
            if r["mockito_fix"].lower() not in ("true", "false"):
                problems.append(f"{where}: mockito_fix={r['mockito_fix']!r}")
            path = (d / r["file_here"]).resolve()
            if d.resolve() not in path.parents or path.suffix != ".java" or not path.is_file():
                problems.append(f"{where}: test file missing or outside the folder: {r['file_here']}")
                continue
            key = (d.name, r["file_here"])
            u = units.get(key)
            if u is None:
                text = read_text(path)
                pk = re.search(r"^\s*package\s+([\w.]+)\s*;", text, re.M)
                units[key] = u = types.SimpleNamespace(
                    folder=d.name, file=r["file_here"], path=path, zip=r["zip"], module=r["module"],
                    java=r["java"], junit=r["junit"], fix=r["mockito_fix"].lower() == "true",
                    focal_from_test=r["focal_from_test"], project=r["project"], sha=r["sha"],
                    archive_url=r.get("archive_url", ""), methods=OrderedDict(), lines=text.count("\n") + 1,
                    package=pk.group(1) if pk else "", cls=path.stem)
            else:
                for attr, col in (("zip", "zip"), ("module", "module"), ("java", "java"), ("junit", "junit"),
                                  ("focal_from_test", "focal_from_test")):
                    if getattr(u, attr) != r[col]:
                        problems.append(f"{where}: {col} differs between rows of the same file")
                if u.fix != (r["mockito_fix"].lower() == "true"):
                    problems.append(f"{where}: mockito_fix differs between rows of the same file")
            test = r["generated_test"]
            if test in u.methods:
                problems.append(f"{where}: {test} listed twice")
            u.methods[test] = r["focal_method"]
            fq = u.package + "." + u.cls if u.package else u.cls
            if test.split("#")[0] != fq:
                problems.append(f"{where}: {test} does not match package/class of {r['file_here']} ({fq})")
    return list(units.values()), problems


def select_units(units, a):
    def text(u):
        return " ".join([u.folder, u.file, u.zip, *u.methods])
    out = []
    for u in units:
        if a.folder and not any(x in u.folder for x in a.folder):
            continue
        if a.only and not any(x in text(u) for x in a.only):
            continue
        if any(x in text(u) for x in a.skip):
            continue
        if a.java and u.java != a.java or a.junit and u.junit != a.junit:
            continue
        if a.no_hadoop and u.zip.startswith(HADOOP_FAMILY):
            continue
        out.append(u)
    if a.pilot:
        best = {}
        for u in out:
            if u.fix:
                continue
            k, rank = (u.java, u.junit), (u.zip.startswith(HADOOP_FAMILY), len(u.methods), u.lines, u.folder, u.file)
            if k not in best or rank < best[k][0]:
                best[k] = (rank, u)
        picks = [v[1] for _, v in sorted(best.items())]
        fixed = [u for u in out if u.fix]
        if fixed:
            picks.append(min(fixed, key=lambda u: (u.zip.startswith(HADOOP_FAMILY), len(u.methods), u.lines,
                                                   u.folder, u.file)))
        out = picks
    return sorted(out, key=lambda u: (u.zip, u.folder, u.file))


def rid_of(u):
    return f"{u.folder}__{Path(u.file).parent.name}__{u.cls}"


def key_of(u, test):
    return (u.folder, u.file, test)


def find_pkg_dir(a):
    cands = [a.pkg_dir] if a.pkg_dir else [Path(__file__).resolve().parent, Path.cwd(), HOME / "flaky_check_package"]
    for c in cands:
        if c and (c / "batch_reproflake_chatunitest.py").is_file() and (c / "flaky_check_basic.py").is_file():
            return c.expanduser().resolve()
    sys.exit("The flaky_check_package folder not found. Put this script into that folder or pass "
             "--suz-dir /path/to/flaky_check_package (the folder that has batch_reproflake_chatunitest.py).")


def import_suz(pkg_dir):
    sys.path.insert(0, str(pkg_dir))
    try:
        import batch_reproflake_chatunitest as B
        import collect_nonflaky as C
        import replay_generated as R
        import flaky_check_basic as F
        import setup_archives as S
    except Exception as ex:
        sys.exit(f"could not import the flaky-check scripts from {pkg_dir}: {type(ex).__name__}: {ex}")
    M = types.SimpleNamespace(B=B, C=C, R=R, F=F, S=S)
    need = [("B", "BUILD_SKIPS"), ("B", "build_module"), ("B", "find_jdk"), ("B", "safe_extract"),
            ("B", "ensure_mockito"), ("B", "locate_test"), ("C", "maven_without_extensions"), ("R", "TempPom"),
            ("R", "CopiedTests"), ("R", "mockito_fixed"), ("F", "Prepared"), ("F", "test_classes"),
            ("S", "seed_m2")]
    gone = [f"{m}.{n}" for m, n in need if not hasattr(getattr(M, m), n)]
    if gone:
        sys.exit(f"{pkg_dir} is a different version than this script expects; missing: {', '.join(gone)}")
    return M


def archive_urls(pkg_dir, units):
    urls = {}
    p = pkg_dir / "archives.csv"
    if p.is_file():
        for r in csv.DictReader(open(p, newline="", encoding="utf-8")):
            urls[r["zip"]] = r["url"]
    for u in units:
        if u.archive_url and u.zip not in urls:
            urls[u.zip] = u.archive_url
    return urls


def jdk_for(M, version):
    return M.B.find_jdk(version)


def run_quiet(cmd, env=None, timeout=120):
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, env=env, timeout=timeout, stdin=subprocess.DEVNULL)
        return p.returncode, (p.stdout or "") + (p.stderr or "")
    except (OSError, subprocess.TimeoutExpired) as ex:
        return -1, f"{type(ex).__name__}: {ex}"


def jdk_major(java_out):
    m = re.search(r'version "(\d+)(?:\.(\d+))?', java_out)
    if not m:
        return None
    return m.group(2) if m.group(1) == "1" else m.group(1)


def flakesync_state(a):
    plug = list((a.m2 / "edu/utexas/ece/flakesync-maven-plugin/1.0-SNAPSHOT").glob("*.jar"))
    core = list((a.m2 / "edu/utexas/ece/flakesync-core/1.0-SNAPSHOT").glob("*.jar"))
    has_scw, bl = False, None
    for j in core:
        try:
            with zipfile.ZipFile(j) as z:
                names = set(z.namelist())
                if "edu/utexas/ece/flakesync/agent/SafeClassWriter.class" in names:
                    has_scw = True
                if "blacklist.txt" in names:
                    bl = z.read("blacklist.txt").decode("utf-8", "replace").splitlines()
        except (OSError, zipfile.BadZipFile):
            pass
    return bool(plug), bool(core), has_scw, bl


def other_runners():
    scripts = (r"night_run\.py|flaky_check_basic\.py|flaky_check\.py|run_flaky_all\.sh|"
               r"flakesync_td_run\.py|flakesync_checked_run\.py")
    rc, out = run_quiet(["pgrep", "-af", r"(python[0-9.]*|bash|sh) +(-[^ ]+ +)*[^ ]*(" + scripts + ")"])
    if rc not in (0, 1):
        return []
    mine = {str(os.getpid()), str(os.getppid())}
    found = []
    for line in out.splitlines():
        pid = line.split(" ", 1)[0]
        if pid not in mine and "pgrep" not in line:
            found.append(line[:120])
    return found


def checks(a, M, units, urls, bl):
    res = []
    add = lambda lvl, txt: res.append((lvl, txt))
    add("OK" if sys.version_info >= (3, 8) else "FAIL", f"python {sys.version.split()[0]} (need 3.8+)")
    nm = sum(len(u.methods) for u in units)
    add("OK" if units else "FAIL", f"{len(units)} test files / {nm} test methods selected")
    mvn = shutil.which("mvn")
    if not mvn:
        add("FAIL", "mvn not found in PATH")
    else:
        home = Path(mvn).resolve().parent.parent
        ext = list((home / "lib" / "ext").glob("*.jar"))
        add("WARN" if ext else "OK", f"maven at {home}" + (f" has lib/ext jars {[e.name for e in ext]}: they can "
                                                           "change surefire; a private copy without them is used" if ext else ""))
    need_java = sorted({u.java for u in units})
    for v in need_java:
        jdk = jdk_for(M, v)
        if not jdk:
            add("FAIL", f"JDK {v} not found (set JAVA_HOME_{v}=/path/to/jdk or install it under /usr/lib/jvm)")
            continue
        env = dict(os.environ, JAVA_HOME=jdk, PATH=str(Path(jdk) / "bin") + os.pathsep + os.environ["PATH"])
        rc, out = run_quiet([str(Path(jdk) / "bin" / "java"), "-version"], env)
        major = jdk_major(out)
        has_javac = (Path(jdk) / "bin" / "javac").exists()
        has_rt = v != "8" or (Path(jdk) / "jre" / "lib" / "rt.jar").is_file() or (Path(jdk) / "lib" / "rt.jar").is_file()
        lvl = "OK" if (major == v and has_javac and has_rt) else "FAIL"
        add(lvl, f"JDK {v}: {jdk} (java reports {major}; javac {'yes' if has_javac else 'MISSING'}"
                 + ("" if has_rt else "; rt.jar MISSING, FlakeSync needs it on JDK 8") + ")")
        if mvn and lvl == "OK":
            rc, out = run_quiet(["mvn", "-v"], env)
            mj = re.search(r"Java version: ([^,\s]+)", out)
            mv = re.search(r"Apache Maven (\S+)", out)
            ok = bool(mj) and jdk_major('version "%s"' % mj.group(1)) == v
            add("OK" if ok else "FAIL", f"mvn under JDK {v}: Maven {mv.group(1) if mv else '?'}, "
                                        f"Java {mj.group(1) if mj else '?'}")
    plugin, core, scw, jar_bl = flakesync_state(a)
    if not (plugin and core):
        add("FAIL", f"FlakeSync is not installed in {a.m2} (cd FlakeSync && mvn -B clean install -DskipTests)")
    else:
        add("OK", "FlakeSync plugin + core are installed in ~/.m2")
        add("OK" if scw else ("WARN" if a.allow_unpatched else "FAIL"),
            "the installed agent HAS the SafeClassWriter fix" if scw else
            "the installed agent does NOT have the SafeClassWriter fix (the LinkageError will come back): "
            "apply flakesync_safeclasswriter.patch and run mvn -B clean install -DskipTests")
        if jar_bl is not None and bl:
            jar_set, src_set = {l.strip() for l in jar_bl if l.strip()}, {l.strip() for l in bl if l.strip()}
            only_jar, only_src = jar_set - src_set, src_set - jar_set
            if only_jar or only_src:
                add("WARN", f"blacklist inside the installed jar differs from {a.blacklist} "
                            f"({len(only_jar)} only in jar, {len(only_src)} only in the file): rebuild FlakeSync "
                            "if you edited the file")
            else:
                add("OK", f"blacklist in the installed jar = {a.blacklist} ({len(bl)} entries)")
        elif not bl:
            add("WARN", f"blacklist file not found: {a.blacklist} (only used for notes)")
    for p, label in ((a.work, "work folder"), (a.archive_dir[0], "archive folder")):
        probe = p
        while not probe.exists() and probe != probe.parent:
            probe = probe.parent
        free = shutil.disk_usage(probe).free / 1e9
        add("FAIL" if free < 5 else "WARN" if free < 15 else "OK", f"{free:.0f} GB free for the {label} ({p})")
    others = other_runners()
    add("FAIL" if others else "OK", "another FlakeRake/FlakeSync/flaky-check run is active: " + "; ".join(others)
        if others else "no other FlakeRake / flaky-check / FlakeSync run is active")
    for d in a.archive_dir + [a.work]:
        if str(d).startswith("/mnt/"):
            add("WARN", f"{d} is on a Windows drive (/mnt/...): Maven builds are many times slower there and "
                        "file modes can break; a folder under your Linux home (~) is much better")
        if "Tests" in str(d) and d != a.work:
            add("FAIL", f"the path {d} contains 'Tests': FlakeSync's whitelist drops every class whose FULL path "
                        "contains 'Tests', so nothing would be delayed. Use a path without 'Tests' (--archive-dir)")
    for z in sorted({u.zip for u in units}):
        state, root = archive_state(a, z)
        if state == "ready":
            add("OK", f"archive {z}: extracted in {root.parent}")
        elif state == "unmarked":
            add("FAIL", f"archive {z}: folder {root} exists but has no .batch_extracted stamp (half-extracted?). "
                        f"If it is complete: touch {root}/.batch_extracted . Otherwise delete the folder.")
        elif z not in urls:
            add("FAIL", f"archive {z}: not extracted and NO DOWNLOAD URL known")
        elif state == "partial" or (zips_dir(a) / (z + ".zip")).is_file():
            add("WARN", f"archive {z}: zip is here / extraction was interrupted; it will be (re)extracted")
        else:
            code = url_status(urls[z])
            add("OK" if code == "200" else "WARN",
                f"archive {z}: not downloaded yet; Zenodo answers HTTP {code} for {urls[z]}")
    return res


def zips_dir(a):
    return a.archive_dir[0].parent / "zips"


def archive_state(a, z):
    for d in a.archive_dir:
        root = d / z
        if not root.is_dir():
            continue
        if (root / ".batch_extracted").is_file():
            return "ready", root
        if (root / ".flakesync_extracting").is_file():
            return "partial", root
        return "unmarked", root
    return "missing", None


def url_status(url):
    if not (url.startswith("https://zenodo.org/records/") and url.endswith(".zip")):
        return "refused-url"
    rc, out = run_quiet(["curl", "-sIL", "--max-time", "20", "-o", "/dev/null", "-w", "%{http_code}", url], timeout=40)
    out = out.strip()
    return out if rc == 0 and re.fullmatch(r"\d{3}", out) else "?"


def ensure_archive(a, M, u, urls):
    z = u.zip
    state, root = archive_state(a, z)
    if state == "unmarked":
        raise RuntimeError(f"{root} exists but has no .batch_extracted stamp (half-extracted?). If it is complete: "
                           f"touch {root}/.batch_extracted ; otherwise delete that folder and run again")
    if state in ("missing", "partial"):
        url = urls.get(z)
        if not url:
            raise RuntimeError(f"no download URL known for {z}")
        zpath = zips_dir(a) / (z + ".zip")
        download(url, zpath)
        need = 3 * zpath.stat().st_size + 2 * 1024 ** 3
        free = shutil.disk_usage(zpath.parent).free
        if free < need:
            raise RuntimeError(f"not enough disk: {free / 1e9:.1f} GB free, want ~{need / 1e9:.1f} GB")
        root = root if state == "partial" else a.archive_dir[0] / z
        say("  extracting ...")
        shutil.rmtree(root, ignore_errors=True)
        root.mkdir(parents=True)
        marker = root / ".flakesync_extracting"
        marker.touch()
        M.B.safe_extract(zpath, root)
        marker.unlink()
        (root / ".batch_extracted").touch()
    if not (root / ".batch_m2_seeded").is_file():
        n = M.S.seed_m2(root, a.m2)
        say(f"  {n} files copied from Flakym2 into {a.m2}")
        (root / ".batch_m2_seeded").touch()
    _, module_dir, _ = M.B.locate_test(root, u.module, u.focal_from_test)
    added = M.B.ensure_mockito(module_dir, u.junit)
    if added:
        say(f"  Mockito added to {module_dir / 'pom.xml'}: {', '.join(added)} (original: pom.xml.batch_orig)")
    return root, module_dir


def sweep_stale(M, out):
    for rd in sorted((out / "rows").glob("*")):
        mdf = rd / "module_dir.txt"
        if not mdf.is_file():
            continue
        module_dir = Path(read_text(mdf).strip())
        if not module_dir.is_dir():
            continue
        if (rd / "copied.txt").is_file():
            say(f"cleaning up test files left in {module_dir} by an interrupted run ({rd.name})")
            M.R.CopiedTests(module_dir, [], rd).cleanup()
        if restore_pom_in(rd, module_dir / "pom.xml"):
            say(f"restored {module_dir / 'pom.xml'}, left swapped by an interrupted run ({rd.name})")


def restore_pom_in(row_dir, pom):
    used, before = row_dir / "pom.xml.used", row_dir / "pom.xml.before"
    if used.is_file() and before.is_file() and pom.is_file() and pom.read_bytes() == used.read_bytes():
        shutil.copy2(before, pom)
        return True
    return False


def restore_pom(p):
    if p is not None:
        try:
            restore_pom_in(p.row_dir, p.pom)
        except OSError:
            pass


def base_row(u, test):
    row = {k: "" for k in FIELDS}
    row.update(folder=u.folder, file=u.file, java=u.java, junit=u.junit, zip=u.zip, module=u.module,
               mockito_fix=str(u.fix).lower(), test=test)
    return row


def process_method(a, u, p, test, bl, outroot, fs_skips, build):
    t0 = time.time()
    method = test.split("#", 1)[1]
    tname = test.replace("#", ".")
    outdir = outroot / method
    outdir.mkdir(parents=True, exist_ok=True)
    module_dir = p.module_dir
    fs = module_dir / ".flakesync"
    for suffix in ("ResultMethods", "Locations", "whitelist"):
        (fs / f"{tname}-{suffix}.txt").unlink(missing_ok=True)
    row = base_row(u, test)
    row.update(setup="ok", build=build)
    row["focal_blacklisted"] = blacklisted_by(u.methods[test].split("#")[0], bl)

    goal = lambda g: (["mvn", "-B", f"{PLUGIN}:{g}", f"-Dflakesync.testName={test}",
                       f"-Dmaven.repo.local={a.m2}"] + fs_skips)
    say(f"  [{method}] concurrentfind ...")
    rc, to = sh(goal("concurrentfind"), module_dir, outdir / "concurrentfind.log", a.cf_timeout, p.env)
    text = read_text(outdir / "concurrentfind.log")
    runs = surefire_runs(text)
    v = "timeout" if to else verdict(runs, text)
    row["cf_status"] = v
    row["cf_tests_run"] = runs[-1][0] if runs else ""
    row["cf_problem"] = first_problem(text)
    if not row["cf_problem"] and "The test did not run" in text:
        row["cf_problem"] = "FlakeSync printed: the test did not run (see concurrentfind.log)"
    row["linkage_classes"] = " ".join(sorted(set(RE_LINKAGE.findall(text))))
    notes = []
    if v == "pass" and runs and runs[-1][0] != 1:
        notes.append(f"surefire ran {runs[-1][0]} tests, expected 1")

    mod_classes = module_classes(module_dir)
    row["module_classes"] = len(mod_classes)
    if not mod_classes:
        notes.append("no module classes found in target/classes (compile problem, or 'Tests' in the folder path)")
    row["module_classes_blacklisted"] = sum(1 for c in mod_classes if blacklisted_by(c.replace("/", "."), bl))
    rm = fs / f"{tname}-ResultMethods.txt"
    if rm.is_file():
        row["rm_total"], row["rm_in_module"] = resultmethods_stats(rm, mod_classes)
        shutil.copy2(rm, outdir / rm.name)

    if v != "pass":
        row["dl_status"] = "skipped"
        notes.append(f"delaylocs skipped: concurrentfind result was {v}")
    elif row["rm_in_module"] in ("", 0) and not a.force_delaylocs:
        row["dl_status"] = "skipped"
        notes.append("delaylocs skipped: no concurrent method in the module's own classes (nothing can be delayed)")
    else:
        say(f"  [{method}] delaylocs ({row['rm_in_module']} concurrent methods in module classes) ...")
        rc, to = sh(goal("delaylocs"), module_dir, outdir / "delaylocs.log", a.dl_timeout, p.env)
        text2 = read_text(outdir / "delaylocs.log")
        runs2 = surefire_runs(text2)
        row["dl_status"] = "timeout" if to else verdict(runs2, text2)
        row["dl_runs"] = len(runs2)
        loc = fs / f"{tname}-Locations.txt"
        if loc.is_file():
            lines = [l for l in read_text(loc).splitlines() if l.strip()]
            row["dl_last_delay"] = lines[0] if lines else ""
            row["dl_locations"] = max(len(lines) - 1, 0)
            shutil.copy2(loc, outdir / loc.name)
        wl = fs / f"{tname}-whitelist.txt"
        if wl.is_file():
            shutil.copy2(wl, outdir / wl.name)
        pb = first_problem(text2)
        if pb:
            notes.append("delaylocs failure: " + pb)
    row["note"] = "; ".join(notes)
    row["secs"] = int(time.time() - t0)
    return row


def load_done(path):
    done = set()
    if path.is_file():
        for r in csv.DictReader(open(path, newline="", encoding="utf-8")):
            if r["setup"] == "ok" and r["build"] in BUILD_OK and r["cf_status"] in DONE_CF:
                done.add((r["folder"], r["file"], r["test"]))
    return done


def append_row(path, row):
    rows = []
    if path.is_file():
        rows = [r for r in csv.DictReader(open(path, newline="", encoding="utf-8"))
                if (r["folder"], r["file"], r["test"]) != (row["folder"], row["file"], row["test"])]
    rows.append(row)
    tmp = path.with_suffix(".tmp")
    with open(tmp, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=FIELDS)
        w.writeheader()
        w.writerows(rows)
    tmp.replace(path)


def show_results(path, keys=None):
    if not path.is_file():
        return
    rows = [r for r in csv.DictReader(open(path, newline="", encoding="utf-8"))
            if keys is None or (r["folder"], r["file"], r["test"]) in keys]
    print("\n%-5s %-5s %-24s %-34s %-10s %-4s %-7s %-6s %-5s" % (
        "java", "junit", "archive", "test", "concurrf.", "n", "in-mod", "dl", "locs"))
    for r in sorted(rows, key=lambda r: (r["java"], r["junit"], r["zip"], r["test"])):
        cf = r["cf_status"] or ("setup-failed" if r["setup"] != "ok" else r["build"])
        print("%-5s %-5s %-24s %-34s %-10s %-4s %-7s %-6s %-5s" % (
            r["java"], r["junit"], r["zip"][:24], r["test"].split("#")[-1][:34], cf, r["cf_tests_run"],
            r["rm_in_module"], r["dl_status"], r["dl_locations"]))
    by = defaultdict(Counter)
    for r in rows:
        by[(r["java"], r["junit"])][r["cf_status"] or r["build"] or "setup-failed"] += 1
    print("\nconcurrentfind result per JDK x JUnit (counts of test methods):")
    for k in sorted(by):
        print(f"  JDK {k[0]:>2} + JUnit {k[1]}: " + ", ".join(f"{s}={n}" for s, n in sorted(by[k].items())))


def parse_args():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--tests-dir", type=Path, default=HOME / "flakesync_work" / "flaky_checked_tests",
                    help="the unzipped flaky_checked_tests folder (the one with README.txt)")
    ap.add_argument("--suz-dir", type=Path, default=None,
                    help="The flaky_check_package folder (default: where this script is, then cwd)")
    ap.add_argument("--archive-dir", type=Path, action="append", default=None,
                    help="folder with the extracted <zip>/ archives (repeatable; default <suz-dir>/extracted)")
    ap.add_argument("--work", type=Path, default=HOME / "flakesync_work" / "checked")
    ap.add_argument("--m2", type=Path, default=HOME / ".m2" / "repository")
    ap.add_argument("--blacklist", type=Path, default=HOME / "flakesync_work" / "FlakeSync" / "flakesync-core" /
                    "src" / "main" / "resources" / "blacklist.txt")
    ap.add_argument("--folder", action="append", default=[], help="folder name contains TEXT (repeatable)")
    ap.add_argument("--only", action="append", default=[], help="folder/file/archive/test contains TEXT (repeatable)")
    ap.add_argument("--skip", action="append", default=[], help="same, to leave out (repeatable)")
    ap.add_argument("--java", choices=["8", "11"])
    ap.add_argument("--junit", choices=["4", "5"])
    ap.add_argument("--pilot", action="store_true",
                    help="only the smallest file for each JDK x JUnit combination + the smallest mockito_fix file")
    ap.add_argument("--no-hadoop", action="store_true", help="leave out HDFS-/HADOOP-/YARN-/HBASE- archives")
    ap.add_argument("--list", action="store_true", help="show the plan and exit")
    ap.add_argument("--preflight", action="store_true", help="run the checks and exit (changes nothing)")
    ap.add_argument("--redo", action="store_true", help="run rows again that are already in summary.csv")
    ap.add_argument("--force-delaylocs", action="store_true", help="run delaylocs even without module methods")
    ap.add_argument("--allow-unpatched", action="store_true", help="do not stop if the SafeClassWriter fix is missing")
    ap.add_argument("--keep-going", action="store_true", help="do not stop after many failures in a row")
    ap.add_argument("--stop-after", type=int, default=10, help="stop after N build/no-result/timeout rows in a row")
    ap.add_argument("--cf-timeout", type=float, default=20, help="minutes per concurrentfind (default 20)")
    ap.add_argument("--dl-timeout", type=float, default=45, help="minutes per delaylocs (default 45)")
    ap.add_argument("--compile-timeout", type=float, default=60, help="minutes per module build (default 60)")
    return ap.parse_args()


def main():
    a = parse_args()
    a.tests_dir = a.tests_dir.expanduser().resolve()
    a.work = a.work.expanduser().resolve()
    a.m2 = a.m2.expanduser().resolve()
    a.blacklist = a.blacklist.expanduser()
    pkg_dir = find_pkg_dir(a)
    a.archive_dir = [d.expanduser().resolve() for d in (a.archive_dir or [pkg_dir / "extracted"])]
    M = import_suz(pkg_dir)
    fs_skips = [s for s in M.B.BUILD_SKIPS if s != "-DskipTests"] + ["-Dimpsort.skip=true"]

    all_units, problems = load_units(a.tests_dir)
    if problems:
        print(f"{len(problems)} problem(s) in {a.tests_dir}:")
        for pr in problems[:30]:
            print("  -", pr)
        sys.exit("fix these first (is --tests-dir the unzipped flaky_checked_tests folder?)")
    units = select_units(all_units, a)
    urls = archive_urls(pkg_dir, all_units)
    bl = [l.strip() for l in read_blacklist(a.blacklist) if l.strip()]

    if a.list or a.preflight:
        zips = OrderedDict()
        for u in units:
            zips.setdefault(u.zip, []).append(u)
        for z, us in zips.items():
            u0 = us[0]
            print(f"{z}: module {u0.module} | JDK {u0.java} | JUnit {u0.junit} | {len(us)} file(s)")
            for u in us:
                print(f"    {u.folder}/{u.file}" + ("   [mockito_fix pom swap]" if u.fix else ""))
                for t, focal in u.methods.items():
                    why = blacklisted_by(focal.split("#")[0], bl)
                    print(f"        {t.split('#')[1]}" + (f"   NOTE: focal class is under blacklisted prefix '{why}'"
                                                          " (nothing of it is instrumented)" if why else ""))
        print(f"\n{len(zips)} archives, {len(units)} files, {sum(len(u.methods) for u in units)} test methods "
              f"(of {len(all_units)} files in the csvs).  extra mvn flags: {' '.join(fs_skips)}")
    if a.preflight or not a.list:
        res = checks(a, M, units, urls, bl)
        print("\npreflight:")
        for lvl, txt in res:
            print(f"  [{lvl:4}] {txt}")
        bad = [t for lvl, t in res if lvl == "FAIL"]
        if a.preflight:
            print("\n" + ("ALL CHECKS PASSED" if not bad else f"{len(bad)} CHECK(S) FAILED - do not start the run"))
            sys.exit(1 if bad else 0)
        if bad:
            sys.exit(f"{len(bad)} preflight check(s) failed (above); fix them first")
    if a.list:
        return

    results = a.work / "results"
    results.mkdir(parents=True, exist_ok=True)
    lock = open(a.work / ".lock", "w")
    try:
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    except OSError:
        sys.exit("another flakesync_checked_run.py is running with the same --work folder")

    env = os.environ.copy()
    mvn_bin = M.C.maven_without_extensions(a.work)
    if mvn_bin:
        env["PATH"] = str(mvn_bin) + os.pathsep + env["PATH"]
        env.pop("M2_HOME", None)
        env.pop("MAVEN_HOME", None)
    ns = types.SimpleNamespace(out=a.work / "prepared", env=env, repo=a.m2, timeout=int(a.compile_timeout * 60),
                               command="flakesync")
    ns.out.mkdir(parents=True, exist_ok=True)
    sweep_stale(M, ns.out)

    summary = results / "summary.csv"
    done = set() if a.redo else load_done(summary)
    say(f"{len(units)} test files in the plan; summary: {summary}")
    keys_run, bad_in_row, stop = set(), 0, False
    archives, builds = {}, {}
    try:
        for u in units:
            if stop:
                break
            todo = [t for t in u.methods if key_of(u, t) not in done]
            if not todo:
                continue
            say(f"[{u.zip}] {u.folder}/{u.file}  JDK {u.java}, JUnit {u.junit}, {len(todo)} method(s)"
                + ("  (mockito_fix)" if u.fix else ""))

            def fail_rows(setup, note, build=""):
                nonlocal bad_in_row
                for t in todo:
                    row = base_row(u, t)
                    row.update(setup=setup, build=build, note=note[:300])
                    append_row(summary, row)
                    keys_run.add(key_of(u, t))
                bad_in_row += len(todo)
                say(f"  {setup.upper()}: {note[:200]}")

            if u.zip not in archives:
                try:
                    archives[u.zip] = ensure_archive(a, M, u, urls)
                except (RuntimeError, OSError, ValueError, zipfile.BadZipFile) as ex:
                    archives[u.zip] = ex
            ctx = archives[u.zip]
            if isinstance(ctx, Exception):
                fail_rows("failed", f"archive setup: {ctx}")
            else:
                root, _ = ctx
                jdk = jdk_for(M, u.java)
                if not jdk:
                    fail_rows("failed", f"JDK {u.java} not found")
                else:
                    g = {"zip": u.zip, "flaky_test": u.focal_from_test, "selected_method": next(iter(u.methods.values())),
                         "chatunitest_mode": u.junit, "test_files": [u.path], "methods": list(u.methods)}
                    m = {"module": u.module, "java": u.java, "project": u.project, "flaky_commit": u.sha, "zip": u.zip}
                    p = None
                    try:
                        p = M.F.Prepared(rid_of(u), root, g, m, u.fix, ns)
                        (p.row_dir / "module_dir.txt").write_text(str(p.module_dir))
                        bkey = (u.zip, u.java)
                        if bkey not in builds:
                            builds[bkey] = p.build()
                            say(f"  build: {builds[bkey]}  (log: {p.row_dir / 'build.log'})")
                        build = builds[bkey]
                        if build not in BUILD_OK:
                            fail_rows("ok", f"module build: {build} (see {p.row_dir / 'build.log'}); "
                                            "not tried again for the other files of this archive in this run", build)
                        else:
                            with p:
                                outroot = results / rid_of(u)
                                for t in todo:
                                    try:
                                        row = process_method(a, u, p, t, bl, outroot, fs_skips, build)
                                    except Exception as ex:
                                        row = base_row(u, t)
                                        row.update(setup="ok", build=build, cf_status="script-error",
                                                   note=f"{type(ex).__name__}: {ex}"[:300])
                                    append_row(summary, row)
                                    keys_run.add(key_of(u, t))
                                    bad_in_row = bad_in_row + 1 if row["cf_status"] in BAD_STATUSES else 0
                                    say(f"  [{t.split('#')[1]}] concurrentfind={row['cf_status']} tests-run={row['cf_tests_run']} "
                                        f"in-module={row['rm_in_module']} delaylocs={row['dl_status']} "
                                        f"delay={row['dl_last_delay']} locations={row['dl_locations']}"
                                        + (f"\n      problem: {row['cf_problem']}" if row["cf_status"] != "pass"
                                           and row["cf_problem"] else ""))
                    except FileExistsError as ex:
                        restore_pom(p)
                        fail_rows("would_overwrite", str(ex))
                    except Exception as ex:
                        restore_pom(p)
                        fail_rows("failed", f"{type(ex).__name__}: {ex}")
            if bad_in_row >= a.stop_after and not a.keep_going:
                say(f"STOPPING: {bad_in_row} rows in a row without a usable result (build/setup problem?). "
                    "Look at the last logs, fix it, run the same command again (finished rows are kept). "
                    "--keep-going disables this stop.")
                stop = True
    except KeyboardInterrupt:
        say("Interrupted (Ctrl+C). Test files were taken out of the projects; finished rows are kept; "
            "run the same command again to continue.")
    show_results(summary, keys_run or None)
    print(f"\nResults: {results}  (summary.csv + one folder per test file with Locations/ResultMethods/logs)")


if __name__ == "__main__":
    main()
