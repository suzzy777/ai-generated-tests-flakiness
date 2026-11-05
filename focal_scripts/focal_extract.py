import sys, os, re, json, javalang

LIBRARY_CALLS = {
    "assertequals", "asserttrue", "assertfalse", "assertnull", "assertnotnull",
    "assertsame", "assertnotsame", "assertthat", "assertthrows", "assertarrayequals",
    "fail", "mock", "when", "verify", "spy", "thenreturn", "given", "expect",
    "getdeclaredmethod", "getmethod", "setaccessible", "invoke", "printstacktrace",
}

TEST_ANNOTATIONS = {"Test", "ParameterizedTest", "RepeatedTest", "TestFactory", "TestTemplate"}

def is_test_method(m):
    return any(a.name.split(".")[-1] in TEST_ANNOTATIONS for a in m.annotations)

def tokenize(name):
    parts = re.findall(r'[A-Z]+(?=[A-Z][a-z])|[A-Z]?[a-z]+|[A-Z]+|[0-9]+', name)
    return sorted(p.lower() for p in parts if p)

def find_src_main(test_path):
    d = os.path.abspath(test_path)
    while d != os.path.dirname(d):
        d = os.path.dirname(d)
        if os.path.basename(d) == "test" and os.path.basename(os.path.dirname(d)) == "src":
            main = os.path.join(os.path.dirname(d), "main")
            return main if os.path.isdir(main) else None
    return None

def project_methods(src_main):
    names = set()
    for root, _, files in os.walk(src_main):
        for f in files:
            if not f.endswith(".java"):
                continue
            try:
                tree = javalang.parse.parse(open(os.path.join(root, f),
                                            encoding="utf-8", errors="ignore").read())
                for _, m in tree.filter(javalang.tree.MethodDeclaration):
                    names.add(m.name)
            except Exception:
                continue
    return names

def process_test(method_node, keep):
    candidates = {}
    for _, call in method_node.filter(javalang.tree.MethodInvocation):
        name = call.member
        if not name or name in candidates:
            continue
        if not keep(name):
            continue
        candidates[name] = tokenize(name)
    return {"test_name": method_node.name,
            "test_tokens": tokenize(method_node.name),
            "candidates": candidates}

def main(path):
    try:
        src = open(path, encoding="utf-8", errors="ignore").read()
    except OSError as e:
        print(f"[skip] cannot read file: {e}", file=sys.stderr); return []
    try:
        tree = javalang.parse.parse(src)
    except javalang.parser.JavaSyntaxError as e:
        print(f"[skip] could not parse Java: {e}", file=sys.stderr); return []

    src_main = find_src_main(path)
    if src_main:
        defined = project_methods(src_main)
        keep = lambda n: n in defined
        mode = f"project-source ({len(defined)} methods from {src_main})"
    else:
        keep = lambda n: n.lower() not in LIBRARY_CALLS
        mode = "blocklist fallback (src/main not found)"

    results = [process_test(m, keep)
               for _, m in tree.filter(javalang.tree.MethodDeclaration)
               if is_test_method(m)]
    if not results:
        print("[skip] no @Test methods found", file=sys.stderr); return []
    return results

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("usage: python3 focal_extract.py <TestFile.java>"); sys.exit(1)
    print(json.dumps(main(sys.argv[1]), indent=2))
