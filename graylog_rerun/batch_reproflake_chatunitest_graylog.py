#!/usr/bin/env python3
import argparse
import csv
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import urllib.request
import zipfile

EXTRA = ["row_number", "archive_root", "test_file", "module_dir", "junit", "parse_mode", "jaccard_method",
         "jaccard_score", "jaccard_ambiguous", "llm_method", "llm_score",
         "production_candidates", "selected_method", "status", "note"]
GEN_FIELDS = ["row_number", "zip", "flaky_test", "selected_method", "junit", "chatunitest_mode",
              "pom_added", "build_status", "status", "exit_code", "tests_generated", "api_errors",
              "overload_match", "focal_overloads", "matching_tests", "calls_focal",
              "log_file", "tmp_output", "test_output"]
PLUGIN = "io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method"
BUILD_SKIPS = ["-DskipTests", "-Drat.skip", "-Dcheckstyle.skip", "-Denforcer.skip", "-Djacoco.skip",
               "-Dmaven.javadoc.skip", "-Danimal.sniffer.skip", "-Dspotbugs.skip", "-Dpmd.skip",
               "-Dlicense.skip", "-Dgpg.skip", "-Djapicmp.skip", "-Dspotless.check.skip",
               "-Dmaven.source.skip",
               "-Dskip.web.build=true"]


def load_rows(path):
    with open(path, newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        rows = list(reader)
        required = {"zip", "module", "flaky_test", "java", "url"}
        missing = required - set(reader.fieldnames or ())
        if missing:
            raise ValueError(f"Missing required CSV columns: {sorted(missing)}")
        return rows, list(reader.fieldnames)


def csv_write(path, rows, columns):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=columns, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def read_previous(path):
    if not path.exists():
        return {}
    with path.open(newline="", encoding="utf-8-sig") as f:
        return {int(row["row_number"]): row for row in csv.DictReader(f)}


def load_module(path, filename, tag):
    source = path / filename
    if not source.is_file():
        raise FileNotFoundError(f"Required focal script not found: {source}")
    spec = importlib.util.spec_from_file_location(tag, str(source))
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def sha256(s):
    return hashlib.sha256(s.encode("utf-8")).hexdigest()


def save_json_atomic(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_suffix(path.suffix + ".tmp")
    with tmp.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    tmp.replace(path)


def safe_extract(zippath, dest):
    dest.mkdir(parents=True, exist_ok=True)
    base = dest.resolve()
    with zipfile.ZipFile(zippath) as z:
        for zi in z.infolist():
            name = zi.filename
            target = (base / name).resolve()
            if target != base and base not in target.parents:
                raise ValueError(f"Unsafe ZIP entry: {name}")
            if ((zi.external_attr >> 16) & 0o170000) == 0o120000:
                raise ValueError(f"Symlink in ZIP: {name}")
        z.extractall(base)


def zip_ok(path):
    try:
        with zipfile.ZipFile(path) as z:
            return z.testzip() is None
    except (OSError, zipfile.BadZipFile):
        return False


def get_archive(row, args):
    archive_name = row["zip"]
    if archive_name in ("", ".", "..") or "/" in archive_name or "\\" in archive_name:
        raise ValueError(f"Unexpected ZIP identifier: {archive_name!r}")
    path = args.archives / archive_name
    if path.is_dir():
        return path.resolve()
    unpacked = args.out / "extracted" / archive_name
    if unpacked.is_dir() and (unpacked / ".batch_extracted").is_file():
        return unpacked.resolve()
    zpath = args.archives / (archive_name + ".zip")
    if zpath.is_file() and args.download and not zip_ok(zpath):
        print(f"  {zpath.name} is incomplete or broken, downloading it again", flush=True)
        zpath.unlink()
    if not zpath.is_file() and args.download:
        url = row["url"].strip()
        if not url.startswith("https://zenodo.org/records/") or not url.endswith(".zip"):
            raise ValueError("Refusing unexpected archive URL (expected HTTPS Zenodo ZIP)")
        zpath.parent.mkdir(parents=True, exist_ok=True)
        temp = zpath.with_suffix(".zip.part")
        for attempt in range(1, 4):
            print(f"  Downloading {archive_name} (try {attempt}) ...", flush=True)
            try:
                with urllib.request.urlopen(url, timeout=180) as source, temp.open("wb") as sink:
                    expected = int(source.headers.get("Content-Length") or 0)
                    shutil.copyfileobj(source, sink)
                if expected and temp.stat().st_size != expected:
                    raise OSError(f"incomplete download: {temp.stat().st_size} of {expected} bytes")
                if not zip_ok(temp):
                    raise OSError("downloaded file is not a valid zip")
                temp.replace(zpath)
                break
            except OSError as ex:
                print(f"  Download failed: {ex}", flush=True)
            finally:
                if temp.exists():
                    temp.unlink()
        else:
            raise OSError(f"Could not download {url} after 3 tries")
    if zpath.is_file():
        print(f"  Extracting {archive_name} ...", flush=True)
        safe_extract(zpath, unpacked)
        (unpacked / ".batch_extracted").touch()
        if args.delete_zips:
            zpath.unlink()
        return unpacked.resolve()
    raise FileNotFoundError(f"Need {path}/ or {zpath}; use --download for Zenodo ZIPs")


def locate_test(root, module_name, test_id):
    fq_class, sep, method = test_id.partition("#")
    if not sep or not fq_class or not method:
        raise ValueError(f"Unexpected test ID: {test_id}")
    rel = Path(*fq_class.split("."))
    expected_tail = tuple(["src", "test", "java", *rel.with_suffix(".java").parts])
    names = []
    for f in root.rglob(rel.name + ".java"):
        if tuple(f.parts[-len(expected_tail):]) == expected_tail:
            names.append(f.resolve())
    if not names:
        raise FileNotFoundError(f"Test source {('/'.join(expected_tail))} not found")

    if module_name not in ("", ".") and len(names) > 1:
        module_parts = tuple(Path(module_name).parts)
        matches = []
        for f in names:
            ps = f.parts
            for i in range(len(ps) - len(module_parts) + 1):
                if ps[i:i + len(module_parts)] == module_parts:
                    matches.append(f)
                    break
        if matches:
            names = matches
    if len(names) != 1:
        raise ValueError("Ambiguous test file locations: " + "; ".join(map(str, names[:6])))

    f = names[0]
    parts = f.parts
    for i in range(len(parts) - 2):
        if parts[i:i + 3] == ("src", "test", "java"):
            root_dir = Path(*parts[:i])
            if not (root_dir / "pom.xml").is_file():
                raise FileNotFoundError(f"Maven pom.xml missing next to test source: {root_dir}")
            return f, root_dir.resolve(), method
    raise ValueError(f"Couldn't locate Maven module for {f}")


def detect_junit(src):
    if re.search(r"^\s*import\s+(static\s+)?org\.junit\.jupiter\.", src, re.M):
        return "5"
    has_test_annotation = re.search(r"@(org\.junit\.)?Test\b", src)
    if has_test_annotation and re.search(r"^\s*import\s+(static\s+)?org\.junit\.(?!jupiter|platform|vintage)", src, re.M):
        return "4"
    if re.search(r"\bextends\s+[\w.]*TestCase\b", src) or re.search(r"^\s*import\s+junit\.framework\.", src, re.M):
        return "3"
    return "4" if has_test_annotation else ""


def is_junit3_test(m):
    return (m.name.startswith("test") and not m.parameters and m.return_type is None
            and "public" in m.modifiers)


def junit3_only(module_dir, archive_root):
    d = module_dir
    while True:
        pom = d / "pom.xml"
        if pom.is_file():
            s = pom.read_text(encoding="utf-8", errors="replace")
            if re.search(r"<junit\.version>\s*3\.", s) or re.search(
                    r"<artifactId>junit</artifactId>\s*<version>\s*3\.", s):
                return True
        if d == archive_root or d == d.parent:
            return False
        d = d.parent


def find_jdk(version):
    if not version:
        return None
    env = os.environ.get("JAVA_HOME_" + version)
    if env:
        return env
    patterns = [f"java-{version}-openjdk*", f"java-1.{version}.0-openjdk*", f"openjdk-{version}*",
                f"jdk-{version}*", f"jdk{version}*", f"temurin-{version}*", f"jdk1.{version}*"]
    for base in (Path("/usr/lib/jvm"), Path("/Library/Java/JavaVirtualMachines")):
        if not base.is_dir():
            continue
        for pat in patterns:
            for p in sorted(base.glob(pat)):
                home = p / "Contents" / "Home" if (p / "Contents" / "Home").is_dir() else p
                if (home / "bin" / "java").exists():
                    return str(home)
    return None


def method_line(m):
    pos = getattr(m, "position", None) or getattr(m, "start_position", None)
    return pos.line if pos else None


def isolate_test_method(src, method, javalang, extract_body):
    lines = src.split("\n")
    sig = re.compile(r"\bvoid\s+" + re.escape(method) + r"\s*\(")
    hits = [i for i, line in enumerate(lines) if sig.search(line)]
    if len(hits) != 1:
        raise ValueError(f"Found {len(hits)} declarations of {method} while isolating it")
    start = hits[0]
    while start > 0 and lines[start - 1].strip().startswith("@"):
        start -= 1
    for first in dict.fromkeys((start, hits[0])):
        text = extract_body(lines, first + 1)
        if not text:
            continue
        for attempt in (text, LOCAL_CLASS_ANNOTATIONS.sub(lambda mo: re.sub(r"\S", " ", mo.group(0)), text)):
            try:
                cu = javalang.parse.parse("class IsolatedTest {\n" + attempt + "\n}")
            except Exception:
                continue
            ms = [m for m in cu.types[0].methods if m.name == method]
            if len(ms) == 1:
                return ms[0], hits[0] + 1
    raise ValueError(f"Could not parse {method} on its own either")


LOCAL_CLASS_ANNOTATIONS = re.compile(
    r"(?:@[\w.]+(?:\s*\((?:[^()]|\([^()]*\))*\))?\s+)+(?=(?:(?:final|abstract|static)\s+)*class\s)")


def project_method_filter(extractor, testfile, cache):
    src_main = extractor.find_src_main(str(testfile))
    if not src_main:
        return lambda n: n.lower() not in extractor.LIBRARY_CALLS
    if src_main not in cache:
        cache[src_main] = extractor.project_methods(src_main)
    defined = cache[src_main]
    return lambda n: n in defined


def call_arg_counts(test_node, method, javalang):
    if test_node is None:
        return set()
    return {len(c.arguments or []) for _, c in test_node.filter(javalang.tree.MethodInvocation)
            if c.member == method}


def declared_param_counts(module_dir, cls, method, javalang):
    src = (module_dir / "src" / "main" / "java" / Path(*cls.split("."))).with_suffix(".java")
    if not src.is_file():
        return None
    try:
        cu = javalang.parse.parse(src.read_text(encoding="utf-8", errors="replace"))
    except Exception:
        return None
    out = [(len(m.parameters), bool(m.parameters and m.parameters[-1].varargs))
           for t in cu.types if t.name == cls.split(".")[-1]
           for m in getattr(t, "methods", []) if m.name == method]
    return out or None


def arity_fits(decls, arg_counts):
    if not decls:
        return True
    return any((a == n) or (varargs and a >= n - 1) for n, varargs in decls for a in arg_counts)


def normalize(name):
    if not name:
        return ""
    return (re.findall(r"\w+", re.sub(r"\(.*?\)", "", str(name))) or [""])[-1]


def production_candidates(module_dir, method, parser):
    src = module_dir / "src" / "main" / "java"
    if not src.is_dir():
        return []
    candidates = set()
    matcher = re.compile(r"\b" + re.escape(method) + r"\s*\(")
    for p in src.rglob("*.java"):
        try:
            s = p.read_text(encoding="utf-8", errors="replace")
            if not matcher.search(s):
                continue
            cu = parser.parse.parse(s)
        except Exception:
            continue
        package = (cu.package.name + ".") if cu.package else ""
        for klass in cu.types:
            if method in {m.name for m in getattr(klass, "methods", [])}:
                candidates.add(package + klass.name)
    return sorted(candidates)


def run_identification(row, args, modules, cache, previous):
    out = dict(row)
    for k in EXTRA:
        out[k] = previous.get(k, "")
    try:
        root = get_archive(row, args)
        testfile, module_dir, testmethod = locate_test(root, row["module"], row["flaky_test"])
        out.update(archive_root=str(root), test_file=str(testfile), module_dir=str(module_dir))
    except (OSError, ValueError, zipfile.BadZipFile) as ex:
        out.update(status="input_error", note=str(ex))
        return out
    junit = previous.get("junit") or detect_junit(testfile.read_text(encoding="utf-8", errors="replace"))
    out["junit"] = junit

    if not args.run_llm:
        out.update(status=out.get("status") if out.get("selected_method") else "awaiting_llm",
                   note="Run prepare with --run-llm to identify the focal method")
        return out
    if previous.get("selected_method") and not args.force:
        out.update(status="reviewed", note="Preserved existing selected_method")
        return out

    extractor, finder, llm, javalang = modules
    extractor.is_test_method = extractor.junit_annotated
    llm.is_test = llm.junit_annotated
    if junit == "3":
        extractor.is_test_method = lambda m: extractor.junit_annotated(m) or is_junit3_test(m)
        llm.is_test = lambda m: llm.junit_annotated(m) or is_junit3_test(m)
    try:
        src = testfile.read_text(encoding="utf-8", errors="replace")
        try:
            tree = javalang.parse.parse(src)
        except Exception:
            tree = None
        if tree is not None:
            out["parse_mode"] = "file"
            key = str(testfile)
            if key not in cache["extract"]:
                cache["extract"][key] = extractor.main(str(testfile))
            entries = [e for e in cache["extract"][key] if e.get("test_name") == testmethod]
            found = [(p, m) for p, m in tree.filter(javalang.tree.MethodDeclaration)
                     if m.name == testmethod and llm.is_test(m)]
            nested = (row.get("test") or "").partition("#")[0].rpartition(".")[2].split("$")
            if len(entries) > 1 and len(nested) > 1:
                found = [(p, m) for p, m in found
                         if [n.name for n in p if isinstance(n, javalang.tree.ClassDeclaration)] == nested]
                if len(found) == 1:
                    entries = [extractor.process_test(found[0][1], project_method_filter(
                        extractor, testfile, cache["src_main"]))]
            if len(entries) != 1:
                raise ValueError(f"Expected exactly one test method named {testmethod}; found {len(entries)}")
            entry = entries[0]
            ms = [m for _, m in found]
            start_line = method_line(ms[0]) if len(ms) == 1 else None
            test_node = ms[0] if len(ms) == 1 else None
        else:
            out["parse_mode"] = "method_only"
            m, start_line = isolate_test_method(src, testmethod, javalang, llm.extract_body)
            test_node = m
            entry = extractor.process_test(m, project_method_filter(extractor, testfile, cache["src_main"]))
        ranked = finder.find_focal_method_ranked(entry["test_tokens"], entry["candidates"])
        if not ranked:
            out.update(status="no_jaccard_candidates", note="No project method calls identified")
            return out
        jaccard_name, score = ranked[0]
        ambiguous = len(ranked) > 1 and abs(score - ranked[1][1]) < 0.01
        out.update(jaccard_method=jaccard_name, jaccard_score=f"{score:.3f}",
                   jaccard_ambiguous=str(bool(ambiguous)).lower())
        if not start_line:
            raise ValueError(f"Could not isolate test method {testmethod}")
        body = llm.extract_body(src.split("\n"), start_line)
        if body is None:
            raise ValueError(f"Could not extract body for {testmethod}")
        prompt = llm.PROMPT.format(name=testmethod, body=body)
        ck = sha256(args.model + "|0|" + prompt)
        if ck not in cache["llm"]:
            reply, err = llm.call_api(prompt, args.model, os.environ["OPENAI_API_KEY"], 0.0)
            cands, err2 = llm.parse_reply(reply) if reply is not None else (None, err)
            cache["llm"][ck] = {"candidates": cands, "error": err2}
            save_json_atomic(args.out / "llm_cache.json", cache["llm"])
        info = cache["llm"][ck]
        cands = info.get("candidates") or {}
        if not cands:
            out.update(status="llm_failed", note=str(info.get("error") or "No candidates"))
            return out
        llm_name, llm_score = max(cands.items(), key=lambda pair: pair[1])
        out.update(llm_method=llm_name, llm_score=f"{llm_score:.3f}")
        if normalize(llm_name) != normalize(jaccard_name):
            if score <= 0 or ambiguous:
                out.update(status="review_jaccard", note="Jaccard score zero or tied and LLM picked another method")
            else:
                out.update(status="disagreed", note="LLM and Jaccard did not agree")
            return out
        how = []
        if score <= 0:
            how.append("jaccard 0 accepted" + (" (tied)" if ambiguous else " (only candidate)"
                                               if len(ranked) == 1 else ""))
        elif ambiguous:
            how.append("jaccard tie accepted")

        module_key = str(module_dir) + "|" + jaccard_name
        if module_key not in cache["production"]:
            cache["production"][module_key] = production_candidates(module_dir, jaccard_name, javalang)
        pcs = cache["production"][module_key]
        out["production_candidates"] = ";".join(pcs)
        arg_counts = call_arg_counts(test_node, jaccard_name, javalang)
        if arg_counts and pcs:
            fits = [c for c in pcs
                    if arity_fits(declared_param_counts(module_dir, c, jaccard_name, javalang), arg_counts)]
            if not fits:
                out.update(status="review_class",
                           note=f"test calls {jaccard_name} with {sorted(arg_counts)} argument(s) but no project "
                                "declaration takes that many; probably a library method with the same name")
                return out
            pcs = fits
        if len(pcs) > 1:
            test_fq = row["flaky_test"].partition("#")[0]
            base = re.sub(r"(Tests?|TestCase|IT)$", "", test_fq.split(".")[-1])
            base = re.sub(r"^Test(?=[A-Z])", "", base)
            named = [c for c in pcs if c.split(".")[-1] == base]
            if len(named) == 1:
                pcs = named
                how.append("class: named after test")
            else:
                test_pkg = test_fq.rpartition(".")[0]
                same = [c for c in (named or pcs) if c.rpartition(".")[0] == test_pkg]
                if len(same) == 1:
                    pcs = same
                    how.append("class: same package as test")
        if len(pcs) != 1:
            out.update(status="review_class", note=f"Found {len(pcs)} possible production classes; manually select")
            return out
        cls = pcs[0]
        decl = declared_param_counts(module_dir, cls, jaccard_name, javalang)
        overload_count = len(decl) if decl else 0
        if overload_count > 1:
            how.append(f"overload: all {overload_count}")
        out.update(selected_method=cls + "#" + jaccard_name, status="auto_resolved",
                   note="; ".join(how + ["review selection before generation"]))
    except Exception as ex:
        out.update(status="identify_error", note=f"{type(ex).__name__}: {ex}")
    return out


def prepare(args):
    rows, input_cols = load_rows(args.csv)
    prev = read_previous(args.out / "focal_manifest.csv")
    args.archives = args.archives.resolve()
    args.out = args.out.resolve()
    if args.run_llm and not os.getenv("OPENAI_API_KEY"):
        raise ValueError("Set OPENAI_API_KEY in the environment before --run-llm")
    modules = None
    if args.run_llm:
        import javalang
        extractor = load_module(args.focal_scripts, "focal_extract.py", "repro_focal_extract")
        finder = load_module(args.focal_scripts, "focal_method_finder_batch.py", "repro_focal_jaccard")
        llm = load_module(args.focal_scripts, "llm_focal.py", "repro_focal_llm")
        extractor.junit_annotated = extractor.is_test_method
        llm.junit_annotated = llm.is_test
        modules = (extractor, finder, llm, javalang)

    llm_cache_path = args.out / "llm_cache.json"
    try:
        llm_cache = json.loads(llm_cache_path.read_text())
    except (OSError, json.JSONDecodeError):
        llm_cache = {}
    cache = {"extract": {}, "production": {}, "src_main": {}, "llm": llm_cache}
    manifest = []
    for i, row in enumerate(rows, 1):
        if args.limit and i > args.limit:
            break
        old = prev.get(i, {})
        entry = run_identification(row, args, modules, cache, old)
        entry["row_number"] = i
        manifest.append(entry)
        csv_write(args.out / "focal_manifest.csv", manifest, input_cols + EXTRA)
        print(f"[{i}/{min(len(rows), args.limit or len(rows))}] {row['flaky_test']} -> "
              f"{entry['selected_method'] or '-'} ({entry['status']})", flush=True)
    print(f"\nManifest: {args.out / 'focal_manifest.csv'}")
    print("Inspect/edit selected_method entries; blank selections are skipped by generate.")


def chatunitest_mode(r):
    junit = (r.get("junit") or "").strip()
    if junit == "5":
        return "5", ""
    if junit == "4":
        return "4", ""
    if junit == "3":
        if junit3_only(Path(r["module_dir"]), Path(r.get("archive_root") or "/")):
            return None, "project pins junit 3.x; JUnit 4 tests would not compile"
        return "4", ""
    return None, "unknown JUnit version; set the junit column to 3, 4 or 5"


def pom_text(pom):
    text = pom.read_text(encoding="utf-8", errors="replace")
    text = re.sub(r"<!--.*?-->", "", text, flags=re.S)
    return re.sub(r"<profiles>.*?</profiles>", "", text, flags=re.S)


def own_artifact_id(text):
    return (re.findall(r"<artifactId>\s*([^<]+?)\s*</artifactId>",
                       re.sub(r"<parent>.*?</parent>", "", text, flags=re.S)) or [""])[0]


def imported_bom_modules(root, module_dir):
    imports, d = set(), module_dir.resolve()
    root = root.resolve()
    while True:
        if (d / "pom.xml").is_file():
            for dep in re.findall(r"<dependency>(.*?)</dependency>", pom_text(d / "pom.xml"), re.S):
                if re.search(r"<scope>\s*import\s*</scope>", dep):
                    imports.update(re.findall(r"<artifactId>\s*([^<]+?)\s*</artifactId>", dep))
        if d == root or d == d.parent:
            break
        d = d.parent
    found, todo = [], [Path(".")]
    while todo:
        rel = todo.pop()
        pom = root / rel / "pom.xml"
        if not pom.is_file():
            continue
        text = pom_text(pom)
        if rel != Path(".") and own_artifact_id(text) in imports:
            found.append(rel.as_posix())
        block = re.search(r"<modules>(.*?)</modules>", text, re.S)
        todo += [rel / m for m in re.findall(r"<module>\s*([^<]+?)\s*</module>", block.group(1) if block else "")]
    return sorted(found)


def build_module(r, env, args, row_dir, log_name="build.log"):
    module_dir = Path(r["module_dir"])
    module = (r.get("module") or "").strip()
    root, project_list = module_dir, []
    if module not in ("", "."):
        parts = Path(module).parts
        if module_dir.parts[-len(parts):] == parts:
            parent = Path(*module_dir.parts[:-len(parts)])
            if (parent / "pom.xml").is_file():
                extra = [m for m in imported_bom_modules(parent, module_dir) if m != module]
                root, project_list = parent, ["-pl", ",".join([module, *extra]), "-am"]
    stamp = module_dir / ".batch_built"
    jdk = env.get("JAVA_HOME", "")
    stamp_value = "v2|" + jdk
    if stamp.is_file() and stamp.read_text().strip() == stamp_value:
        return "cached"
    cmd = ["mvn", "-B", "install", *project_list, *BUILD_SKIPS, "-Dmaven.repo.local=" + str(args.repo)]
    log = row_dir / log_name
    print("  build: (cd " + str(root) + " && " + " ".join(cmd) + ")")
    with log.open("w", encoding="utf-8") as fout:
        try:
            p = subprocess.run(cmd, cwd=root, env=env, stdout=fout, stderr=subprocess.STDOUT,
                               timeout=args.timeout)
        except subprocess.TimeoutExpired:
            fout.write("Build timed out after " + str(args.timeout) + " seconds.\n")
            return "build_timeout"
    if p.returncode != 0:
        return "build_failed"
    stamp.write_text(stamp_value)
    return "built"


MOCKITO_VERSION = "4.11.0"


def project_dependencies_close(pom_text):
    masked = re.sub(r"<!--.*?-->|<!\[CDATA\[.*?\]\]>", lambda m: " " * len(m.group(0)), pom_text, flags=re.S)
    stack = []
    for m in re.finditer(r"<(/?)([\w.:-]+)[^>]*?(/?)>", masked):
        closing, name, selfclosing = m.group(1), m.group(2).split(":")[-1], m.group(3)
        if masked[m.start() + 1] in "?!":
            continue
        if closing:
            if stack and stack[-1] == name:
                stack.pop()
                if name == "dependencies" and stack == ["project"]:
                    return m.start()
        elif not selfclosing:
            stack.append(name)
    return None


def ensure_mockito(module_dir, mode):
    pom = module_dir / "pom.xml"
    text = pom.read_text(encoding="utf-8")
    wanted = ["mockito-core"] + (["mockito-junit-jupiter"] if mode == "5" else [])
    missing = [a for a in wanted if f"<artifactId>{a}</artifactId>" not in text]
    if not missing:
        return []
    block = "".join(f"""
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>{a}</artifactId>
            <version>{MOCKITO_VERSION}</version>
            <scope>test</scope>
        </dependency>""" for a in missing) + "\n    "
    close = project_dependencies_close(text)
    if close is not None:
        text = text[:close] + block.lstrip(" ") + text[close:]
    else:
        end = text.rindex("</project>")
        text = text[:end] + "    <dependencies>" + block + "</dependencies>\n" + text[end:]
    backup = pom.with_name("pom.xml.batch_orig")
    if not backup.exists():
        shutil.copy2(pom, backup)
    pom.write_text(text, encoding="utf-8")
    return missing


def merge_archive_m2(r, repo):
    root = Path(r.get("archive_root") or "")
    m2 = next((p for p in [root / "Flakym2", *root.glob("*/Flakym2")] if p.is_dir()), None)
    if m2 is None:
        return None
    if (m2 / ".m2" / "repository").is_dir():
        m2 = m2 / ".m2" / "repository"
    copied = skipped = 0
    for f in m2.rglob("*"):
        if not f.is_file():
            continue
        dest = repo / f.relative_to(m2)
        if dest.exists():
            skipped += 1
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(f, dest)
        copied += 1
    return copied, skipped


def relocate_row(r, args):
    if (Path(r.get("module_dir", "")) / "pom.xml").is_file():
        return r
    rowid = r.get("row_number", "?")
    try:
        root = get_archive(r, args)
        test_file, module_dir, _ = locate_test(root, r["module"], r["flaky_test"])
        print(f"[{rowid}] project found at {module_dir}")
        return dict(r, archive_root=str(root), test_file=str(test_file), module_dir=str(module_dir))
    except (OSError, ValueError, zipfile.BadZipFile) as ex:
        print(f"[{rowid}] project not found on this machine: {ex}")
        return r


PRIMITIVES = {"int", "long", "short", "byte", "char", "boolean", "float", "double"}
BOXES = {"Integer": "int", "Long": "long", "Short": "short", "Byte": "byte", "Character": "char",
         "Boolean": "boolean", "Float": "float", "Double": "double"}
WIDENS_TO = {"byte": {"short", "int", "long", "float", "double"}, "short": {"int", "long", "float", "double"},
             "char": {"int", "long", "float", "double"}, "int": {"long", "float", "double"},
             "long": {"float", "double"}, "float": {"double"}}
STRING_SUPERTYPES = {"String", "Object", "CharSequence", "Comparable", "Serializable"}
WELL_KNOWN = PRIMITIVES | set(BOXES) | {"String", "Class"}


def type_name(t):
    if t is None:
        return None
    name = t.name
    sub = getattr(t, "sub_type", None)
    while sub is not None:
        name, sub = sub.name, getattr(sub, "sub_type", None)
    return name + "[]" * len(getattr(t, "dimensions", None) or [])


def argument_type(arg, local_types, javalang):
    T = javalang.tree
    if isinstance(arg, T.Literal):
        v = str(arg.value)
        low = v.lower()
        if v.startswith('"'):
            return "String"
        if v.startswith("'"):
            return "char"
        if v in ("true", "false"):
            return "boolean"
        if v == "null":
            return "null"
        if low.endswith("l"):
            return "long"
        if low.endswith("f"):
            return "float"
        if not low.startswith("0x") and ("." in v or low.endswith("d") or "e" in low):
            return "double"
        return "int"
    if isinstance(arg, T.Cast):
        return type_name(arg.type)
    if isinstance(arg, T.ClassCreator):
        return type_name(arg.type)
    if isinstance(arg, T.ArrayCreator):
        return type_name(arg.type) + "[]" * len(arg.dimensions or [])
    if isinstance(arg, T.ClassReference):
        return "Class"
    if isinstance(arg, T.MemberReference) and not arg.qualifier and not arg.selectors:
        return local_types.get(arg.member)
    return None


def fit_score(arg, param):
    param = re.sub(r"<.*>", "", param).strip()
    if arg is None:
        return 1
    if arg == "null":
        return 2 if param not in PRIMITIVES else 0
    if arg == param:
        return 3
    if param == "Object" or re.fullmatch(r"[A-Z]", param):
        return 2
    a, p = BOXES.get(arg, arg), BOXES.get(param, param)
    if a in PRIMITIVES and p in PRIMITIVES:
        return 2 if a == p or p in WIDENS_TO.get(a, ()) else 0
    if arg == "String":
        return 2 if param in STRING_SUPERTYPES else 0
    if arg.endswith("[]") or param.endswith("[]"):
        return 0
    if arg in WELL_KNOWN or param in WELL_KNOWN:
        return 0
    return 1


def type_fits(arg, param):
    return fit_score(arg, param) > 0


def call_scores(params, varargs, args, allow_varargs):
    n = len(params)
    if not (varargs and allow_varargs):
        if len(args) != n:
            return None
        pairs = list(zip(args, params))
    else:
        if len(args) < n - 1:
            return None
        element = params[-1][:-2] if params[-1].endswith("[]") else params[-1]
        pairs = list(zip(args[:n - 1], params[:n - 1])) + [(a, element) for a in args[n - 1:]]
    scores = [fit_score(a, p) for a, p in pairs]
    return scores if all(scores) else None


def call_applies(params, varargs, args, allow_varargs):
    return call_scores(params, varargs, args, allow_varargs) is not None


def resolve_call(overloads, args):
    for allow_varargs in (False, True):
        cands = {n: sc for n, (_, ps, va) in overloads.items()
                 if (sc := call_scores(ps, va, args, allow_varargs)) is not None}
        if cands:
            break
    else:
        return []
    key = {n: (sum(1 for x in sc if x == 1), -sum(sc)) for n, sc in cands.items()}
    best = min(key.values())
    return sorted(n for n in cands if key[n] == best)


def class_info_overloads(tmp_dir, cls, method):
    rel = Path(*cls.split("."))
    out = {}
    for d in Path(tmp_dir).glob(f"**/class-info/{rel.as_posix()}"):
        for f in d.glob("*.json"):
            if not f.stem.isdigit():
                continue
            try:
                info = json.loads(f.read_text(encoding="utf-8"))
            except (OSError, ValueError):
                continue
            if info.get("methodName") != method:
                continue
            sig = info.get("methodSignature", "")
            inside = sig[sig.find("(") + 1:sig.rfind(")")]
            params = [t.strip() for t in inside.split(",") if t.strip()]
            varargs = "..." in str(info.get("sourceCode", "")).split("{")[0]
            out[int(f.stem)] = (sig, params, varargs)
    return out


def brace_body(lines, start_line):
    text = "\n".join(lines[start_line - 1:])
    depth, i, started = 0, 0, False
    while i < len(text):
        two = text[i:i + 2]
        if two == "//":
            i = text.find("\n", i)
            if i < 0:
                return None
        elif two == "/*":
            i = text.find("*/", i + 2) + 1
            if i <= 0:
                return None
        elif text[i] in "\"'":
            q, i = text[i], i + 1
            while i < len(text) and text[i] != q:
                i += 2 if text[i] == "\\" else 1
        elif text[i] == "{":
            depth, started = depth + 1, True
        elif text[i] == "}":
            depth -= 1
            if started and depth == 0:
                return text[:i + 1]
        i += 1
    return None


def match_overloads(r, tmp_dir, tests_dir):
    res = {"overload_match": "", "focal_overloads": "", "matching_tests": "", "calls_focal": ""}
    cls, _, method = (r.get("selected_method") or "").partition("#")
    if not cls or not method or method.isdigit():
        res["overload_match"] = "not_applicable"
        return res
    overloads = class_info_overloads(tmp_dir, cls, method)
    if not overloads:
        res["overload_match"] = "no_class_info"
        return res
    files = sorted(p.name for p in Path(tests_dir).rglob(f"{cls.split('.')[-1]}_{method}_*_Test.java"))

    def files_for(numbers):
        return [f for f in files if int(f.split("_")[-3]) in numbers]

    if len(overloads) == 1:
        (num, (sig, _, _)), = overloads.items()
        res.update(overload_match="single_overload", focal_overloads=f"{num}: {sig}",
                   matching_tests=";".join(files_for({num})))
        res["calls_focal"] = focal_call_report(tests_dir, res["matching_tests"], method, overloads)
        return res
    try:
        import javalang
    except ImportError:
        res["overload_match"] = "needs_javalang (pip install javalang)"
        return res
    testmethod = r["flaky_test"].partition("#")[2]
    try:
        src = Path(r["test_file"]).read_text(encoding="utf-8", errors="replace")
        try:
            tree = javalang.parse.parse(src)
            nodes = [m for _, m in tree.filter(javalang.tree.MethodDeclaration) if m.name == testmethod]
            node = nodes[0] if len(nodes) == 1 else None
        except Exception:
            tree = None
            node, _ = isolate_test_method(src, testmethod, javalang, brace_body)
    except (OSError, ValueError, KeyError):
        node = None
    if node is None:
        res["overload_match"] = "test_unparseable"
        return res
    local_types = {}
    for _, decl in (tree.filter(javalang.tree.FieldDeclaration) if tree is not None else []):
        for d in decl.declarators:
            local_types.setdefault(d.name, type_name(decl.type))
    for _, decl in node.filter(javalang.tree.LocalVariableDeclaration):
        for d in decl.declarators:
            local_types[d.name] = type_name(decl.type)
    called, ambiguous = set(), False
    for _, call in node.filter(javalang.tree.MethodInvocation):
        if call.member != method:
            continue
        args = [argument_type(a, local_types, javalang) for a in (call.arguments or [])]
        found = resolve_call(overloads, args)
        if len(found) != 1:
            ambiguous = True
        called.update(found)
    if not called:
        res["overload_match"] = "no_call_matched"
        return res
    matching = files_for(called)
    res.update(focal_overloads="; ".join(f"{n}: {overloads[n][0]}" for n in sorted(called)),
               matching_tests=";".join(matching),
               overload_match=("ambiguous" if ambiguous else "exact") if matching
               else "no_test_for_called_overload")
    res["calls_focal"] = focal_call_report(tests_dir, res["matching_tests"], method, overloads)
    return res


def erased(t):
    t = re.sub(r"<.*>", "", t).strip()
    return re.sub(r"^[A-Z](?=(\[\])*$)", "Object", t)


def focal_call_report(tests_dir, file_names, method, overloads):
    if not file_names:
        return ""
    try:
        import javalang
    except ImportError:
        return "needs_javalang (pip install javalang)"
    paths = {p.name: p for p in Path(tests_dir).rglob("*_Test.java")}
    out = []
    for name in file_names.split(";"):
        target = int(name.split("_")[-3])
        out.append(f"{name}: {classify_focal_calls(paths.get(name), target, method, overloads, javalang)}")
    return "; ".join(out)


def classify_focal_calls(path, target, method, overloads, javalang):
    if path is None:
        return "unclear"
    try:
        tree = javalang.parse.parse(path.read_text(encoding="utf-8", errors="replace"))
    except Exception:
        return "unclear"
    local_types = {}
    for kind in (javalang.tree.FieldDeclaration, javalang.tree.LocalVariableDeclaration):
        for _, decl in tree.filter(kind):
            for d in decl.declarators:
                local_types.setdefault(d.name, type_name(decl.type))
    direct, via_reflection, unclear = set(), set(), False
    for _, call in tree.filter(javalang.tree.MethodInvocation):
        args = call.arguments or []
        if call.member == method:
            found = resolve_call(overloads, [argument_type(a, local_types, javalang) for a in args])
            if len(found) == 1:
                direct.add(found[0])
            elif target in found:
                direct.add(target)
            else:
                unclear = True
        elif (call.member in ("getDeclaredMethod", "getMethod") and args
              and isinstance(args[0], javalang.tree.Literal) and args[0].value == f'"{method}"'):
            if all(isinstance(a, javalang.tree.ClassReference) for a in args[1:]):
                wanted = [erased(type_name(a.type)) for a in args[1:]]
                hit = [n for n, (_, ps, _) in overloads.items() if [erased(p) for p in ps] == wanted]
                if not hit:
                    flat = [w.replace("[]", "") for w in wanted]
                    hit = [n for n, (_, ps, _) in overloads.items()
                           if [erased(p).replace("[]", "") for p in ps] == flat]
                via_reflection.update(hit) if hit else None
                unclear |= not hit
            else:
                unclear = True
    if target in direct:
        return "direct"
    if target in via_reflection:
        return "reflection"
    if direct:
        return "other_overload(" + ",".join(map(str, sorted(direct))) + ")"
    if via_reflection:
        return "reflection_other_overload(" + ",".join(map(str, sorted(via_reflection))) + ")"
    return "unclear" if unclear else "not_called"


def move_other_overloads(res, tests_dir):
    if res.get("overload_match") not in ("exact", "ambiguous") or not res.get("matching_tests"):
        return 0
    keep = set(res["matching_tests"].split(";"))
    tests_dir = Path(tests_dir)
    moved = 0
    for f in list(tests_dir.rglob("*_Test.java")):
        if f.name in keep:
            continue
        dest = tests_dir.parent / "other_overloads" / f.relative_to(tests_dir)
        dest.parent.mkdir(parents=True, exist_ok=True)
        f.replace(dest)
        moved += 1
    return moved


def match(args):
    args.out = args.out.resolve()
    args.archives = args.archives.resolve()
    manifest = {r["row_number"]: r for r in csv.DictReader(args.manifest.resolve().open(encoding="utf-8-sig"))}
    path = args.out / "generation_results.csv"
    results = read_previous_runs(path)
    for rowid, rec in sorted(results.items(), key=lambda kv: int(kv[0])):
        if rec.get("status") != "generated" or rowid not in manifest:
            continue
        r = relocate_row(manifest[rowid], args)
        rec.update(match_overloads(r, rec["tmp_output"], rec["test_output"]))
        moved = move_other_overloads(rec, rec["test_output"])
        print(f"[{rowid}] {rec['overload_match']}: {rec['focal_overloads'] or '-'} -> {rec['matching_tests'] or '-'}"
              + (f" ({moved} other-overload tests moved to other_overloads/)" if moved else ""))
    csv_write(path, [results[k] for k in sorted(results, key=int)], GEN_FIELDS)
    print(f"\nUpdated {path}")


def generate(args):
    path = args.manifest.resolve()
    with path.open(newline="", encoding="utf-8-sig") as f:
        selected = list(csv.DictReader(f))
    args.repo = args.maven_repo.expanduser().resolve()
    prompts = args.junit4_dir.resolve() / "prompts-junit4"
    if not prompts.is_dir():
        raise FileNotFoundError(f"JUnit4 prompt directory not found: {prompts}")
    if args.execute and not os.getenv("OPENAI_API_KEY"):
        raise ValueError("Set OPENAI_API_KEY before --execute")
    args.out = args.out.resolve()
    args.archives = args.archives.resolve()
    rows = []
    earlier = read_previous_runs(args.out / "generation_results.csv")
    candidates = [r for r in selected if (r.get("selected_method") or "").strip()]
    if args.limit:
        candidates = candidates[:args.limit]
    for r in candidates:
        rowid = r["row_number"]
        if (args.resume and earlier.get(rowid, {}).get("status") == "generated"
                and earlier[rowid].get("selected_method") == r["selected_method"]):
            print(f"[{rowid}] skipped (tests already generated)")
            rows.append(earlier[rowid]); continue
        row_dir = args.out / "runs" / ("row_" + str(rowid).zfill(3))
        tmp = row_dir / "tmp"
        tests = row_dir / "tests"
        row_dir.mkdir(parents=True, exist_ok=True)
        log = row_dir / "maven.log"
        r = relocate_row(r, args)
        target = Path(r.get("module_dir", ""))
        selected_method = r["selected_method"].strip()
        mode, reason = chatunitest_mode(r)
        record = {"row_number": rowid, "zip": r["zip"], "flaky_test": r["flaky_test"],
                  "selected_method": selected_method, "junit": r.get("junit", ""),
                  "chatunitest_mode": mode or "", "build_status": "", "status": "preview",
                  "exit_code": "", "tests_generated": "", "api_errors": "",
                  "log_file": str(log), "tmp_output": str(tmp), "test_output": str(tests)}
        if not r.get("module_dir", "").strip() or not (target / "pom.xml").is_file():
            record.update(status="missing_pom")
            print(f"[{rowid}] missing POM: {target}")
        elif mode is None:
            record.update(status="skipped_junit")
            print(f"[{rowid}] skipped: {reason}")
        else:
            cmd = ["mvn", PLUGIN, "-DselectMethod=" + selected_method, "-Dskip.web.build=true"]
            if mode == "4":
                cmd += ["-Dchatunitest.junit=4", "-DpromptPath=" + str(prompts)]
            cmd += ["-DtmpOutput=" + str(tmp), "-DtestOutput=" + str(tests),
                    "-Dmodel=" + args.model,
                    "-Durl=https://api.openai.com/v1/chat/completions",
                    "-DapiKeys=" + (os.getenv("OPENAI_API_KEY", "REDACTED") if args.execute else "REDACTED"),
                    "-Dmaven.repo.local=" + str(args.repo)]
            jdk = find_jdk(str(r.get("java", "")).strip())
            print(f"[{rowid}] cwd={target} selected={selected_method} junit={r.get('junit') or '?'} "
                  f"mode=JUnit{mode} JDK={r.get('java','?')} ({jdk or 'not found, using current java'})")
            print("  " + " ".join(x if not x.startswith("-DapiKeys=") else "-DapiKeys=[hidden]" for x in cmd))
            if args.execute:
                tmp.mkdir(parents=True, exist_ok=True)
                tests.mkdir(parents=True, exist_ok=True)
                env = os.environ.copy()
                if jdk:
                    env["JAVA_HOME"] = jdk
                    env["PATH"] = str(Path(jdk) / "bin") + os.pathsep + env.get("PATH", "")
                if args.add_mockito:
                    record["pom_added"] = ";".join(ensure_mockito(target, mode))
                    if record["pom_added"]:
                        print(f"  added to pom.xml (test scope): {record['pom_added']} {MOCKITO_VERSION}")
                build = build_module(r, env, args, row_dir)
                if build == "build_failed" and args.archive_m2 == "fallback":
                    merged = merge_archive_m2(r, args.repo)
                    if merged is None:
                        print("  build failed and the archive has no Flakym2/ to fall back on")
                    else:
                        print(f"  build failed; merged archive Flakym2/ into {args.repo} "
                              f"({merged[0]} files added, {merged[1]} already there); retrying")
                        retry = build_module(r, env, args, row_dir, "build_archive_m2.log")
                        build = "built_with_archive_m2" if retry == "built" else retry
                record["build_status"] = build
                if build not in ("built", "cached", "built_with_archive_m2"):
                    record.update(status=build)
                    print(f"  => {build}; logs: {row_dir}/build*.log")
                else:
                    with log.open("w", encoding="utf-8") as fout:
                        try:
                            p = subprocess.run(cmd, cwd=target, env=env, stdout=subprocess.PIPE,
                                               stderr=subprocess.STDOUT, timeout=args.timeout, text=True,
                                               errors="replace")
                            output = p.stdout.replace(os.environ["OPENAI_API_KEY"], "[redacted]")
                            fout.write(output)
                            n_tests = sum(1 for _ in tests.rglob("*.java"))
                            n_api = output.count("In AskGPT.askChatGPT")
                            if p.returncode != 0:
                                status = "maven_failed"
                            elif n_tests:
                                status = "generated"
                            elif n_api:
                                status = "api_error"
                            else:
                                status = "no_tests"
                            record.update(status=status, exit_code=p.returncode,
                                          tests_generated=n_tests, api_errors=n_api)
                        except subprocess.TimeoutExpired:
                            fout.write("Maven timed out after " + str(args.timeout) + " seconds.\n")
                            record.update(status="timeout", exit_code="")
                    print(f"  => {record['status']} (tests={record['tests_generated']}, "
                          f"api_errors={record['api_errors']}); log: {log}")
                    if record["status"] == "generated":
                        record.update(match_overloads(r, tmp, tests))
                        moved = move_other_overloads(record, tests)
                        print(f"  overload match: {record['overload_match']} -> {record['matching_tests'] or '-'}"
                              + (f" ({moved} other-overload tests moved to {row_dir / 'other_overloads'})" if moved else ""))
        rows.append(record)
        all_records = dict(earlier)
        all_records.update({x["row_number"]: x for x in rows})
        csv_write(args.out / "generation_results.csv",
                  [all_records[k] for k in sorted(all_records, key=lambda x: int(x))], GEN_FIELDS)
    if not candidates:
        print("No selected_method entries found; prepare and review focal_manifest.csv first.")
    print("NOTE: \"generated\" means ChatUniTest wrote test files, not that they pass. Verify with Surefire: Tests run > 0.")


def read_previous_runs(path):
    if not path.exists():
        return {}
    with path.open(newline="", encoding="utf-8") as f:
        return {r["row_number"]: r for r in csv.DictReader(f)}


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="action", required=True)
    a = sub.add_parser("prepare", help="Create focal-method manifest from ReproFlake CSV")
    a.add_argument("--csv", type=Path, required=True)
    a.add_argument("--archives", type=Path, required=True,
                   help="Folder with extracted <zip>/ directories or <zip>.zip files")
    a.add_argument("--focal-scripts", type=Path,
                   help="Path to archive/03_focal_methods directory (required with --run-llm)")
    a.add_argument("--out", type=Path, default=Path("batch_output"))
    a.add_argument("--run-llm", action="store_true", help="Make OpenAI API calls for focal identification")
    a.add_argument("--download", action="store_true", help="Download missing ZIP files from CSV's Zenodo URLs")
    a.add_argument("--delete-zips", action="store_true",
                   help="Delete each ZIP once it is extracted (saves ~13 GB for the full CSV)")
    a.add_argument("--model", default="gpt-4o")
    a.add_argument("--limit", type=int, default=0, help="First N rows (pilot run)")
    a.add_argument("--force", action="store_true", help="Overwrite previously reviewed focal selections")

    b = sub.add_parser("generate", help="Run ChatUniTest JUnit 4 for reviewed focal methods")
    b.add_argument("--manifest", type=Path, required=True)
    b.add_argument("--junit4-dir", type=Path, required=True,
                   help="Folder containing the patched prompts-junit4 directory")
    b.add_argument("--out", type=Path, default=Path("batch_output"))
    b.add_argument("--maven-repo", type=Path, default=Path.home() / ".m2" / "repository")
    b.add_argument("--model", default="gpt-4o")
    b.add_argument("--limit", type=int, default=0, help="First N selected methods (pilot run)")
    b.add_argument("--timeout", type=int, default=1800)
    b.add_argument("--archives", type=Path, default=Path("zips"),
                   help="Where to look for / download <zip>.zip when the manifest's paths don't exist "
                        "on this machine; extracted copies go to <out>/extracted")
    b.add_argument("--download", action="store_true", help="Download missing archives from Zenodo")
    b.add_argument("--delete-zips", action="store_true", help="Delete each ZIP once it is extracted")
    b.add_argument("--no-add-mockito", dest="add_mockito", action="store_false",
                   help="Don't add test-scoped Mockito to the module pom.xml before building")
    b.add_argument("--archive-m2", choices=["fallback", "never"], default="fallback",
                   help="If a build fails with --maven-repo, merge the archive's Flakym2/ into it "
                        "(no overwrites) and retry the build once (default: fallback)")
    b.add_argument("--resume", action="store_true", help="Skip rows that already generated tests")
    b.add_argument("--execute", action="store_true", help="Actually run Maven/API (otherwise preview only)")
    c = sub.add_parser("match", help="Find which generated tests are for the overload the flaky test calls")
    c.add_argument("--manifest", type=Path, required=True)
    c.add_argument("--out", type=Path, required=True, help="The --out folder generate used")
    c.add_argument("--archives", type=Path, default=Path("zips"))
    c.add_argument("--download", action="store_true")
    c.add_argument("--delete-zips", action="store_true")
    args = parser.parse_args()
    try:
        if args.action == "prepare":
            if args.run_llm and not args.focal_scripts:
                parser.error("--focal-scripts required with --run-llm")
            prepare(args)
        elif args.action == "match":
            match(args)
        else:
            generate(args)
    except (OSError, ValueError, ImportError) as ex:
        parser.exit(2, f"ERROR: {ex}\n")


if __name__ == "__main__":
    main()
