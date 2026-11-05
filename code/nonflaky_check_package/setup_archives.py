#!/usr/bin/env python3
import argparse
import csv
import os
from pathlib import Path
import shutil
import sys
import urllib.request
import zipfile

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import batch_reproflake_chatunitest as B


def download(url, dest, tries=3):
    if dest.is_file():
        try:
            with zipfile.ZipFile(dest) as z:
                if z.testzip() is None:
                    return
        except zipfile.BadZipFile:
            pass
        print(f"  {dest.name} is broken, downloading again")
        dest.unlink()
    if not url.startswith("https://zenodo.org/records/") or not url.endswith(".zip"):
        sys.exit(f"Refusing unexpected archive URL: {url}")
    dest.parent.mkdir(parents=True, exist_ok=True)
    part = dest.with_suffix(".zip.part")
    for attempt in range(1, tries + 1):
        print(f"  downloading {dest.name} (try {attempt}) ...", flush=True)
        try:
            with urllib.request.urlopen(url, timeout=300) as src, part.open("wb") as out:
                expected = int(src.headers.get("Content-Length") or 0)
                shutil.copyfileobj(src, out, 1 << 20)
            if expected and part.stat().st_size != expected:
                raise IOError(f"incomplete: {part.stat().st_size} of {expected} bytes")
            with zipfile.ZipFile(part) as z:
                if z.testzip() is not None:
                    raise IOError("zip test failed")
            part.replace(dest)
            return
        except (OSError, zipfile.BadZipFile) as ex:
            print(f"  failed: {ex}")
            part.unlink(missing_ok=True)
    sys.exit(f"Could not download {url}")


def seed_m2(root, repo):
    m2 = next((p for p in [root / "Flakym2" / ".m2" / "repository", *root.glob("*/Flakym2/.m2/repository")]
               if p.is_dir()), None)
    if m2 is None:
        return 0
    copied = 0
    for f in m2.rglob("*"):
        dest = repo / f.relative_to(m2)
        if f.is_file() and not dest.exists():
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(f, dest)
            copied += 1
    return copied


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--maven-repo", type=Path, default=Path.home() / ".m2" / "repository")
    a.add_argument("--no-seed-m2", action="store_true", help="Skip step 3 (copying Flakym2 into --maven-repo)")
    args = a.parse_args()
    repo = args.maven_repo.resolve()

    for r in csv.DictReader(open(HERE / "archives.csv", encoding="utf-8")):
        z = r["zip"]
        print(f"[{z}]", flush=True)
        root = HERE / "extracted" / z
        if not (root / ".batch_extracted").is_file():
            zpath = HERE / "zips" / (z + ".zip")
            download(r["url"], zpath)
            print("  extracting ...", flush=True)
            shutil.rmtree(root, ignore_errors=True)
            B.safe_extract(zpath, root)
            (root / ".batch_extracted").touch()
        if not args.no_seed_m2 and not (root / ".batch_m2_seeded").is_file():
            print(f"  {seed_m2(root, repo)} files copied from Flakym2 into {repo}", flush=True)
            (root / ".batch_m2_seeded").touch()

    for r in csv.DictReader(open(HERE / "modules.csv", encoding="utf-8")):
        root = HERE / "extracted" / r["zip"]
        _, module_dir, _ = B.locate_test(root, r["module"], r["a_test_in_module"])
        added = B.ensure_mockito(module_dir, r["junit"])
        print(f"[{r['zip']} {r['module']}] Mockito " + (", ".join(added) + " added" if added else "already there"))
    print("\nDone. Next: step 2 in README.md.")


if __name__ == "__main__":
    main()
