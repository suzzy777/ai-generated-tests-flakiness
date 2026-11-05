import sys, os, re, json, time, argparse, urllib.request, urllib.error
import javalang

TEST_ANNOTATIONS = {"Test", "ParameterizedTest", "RepeatedTest", "TestFactory", "TestTemplate"}

PROMPT = """You are analysing a Java unit test to find its focal method (the method under test).

Test name: {name}

Test body:
```java
{body}
```

List the method calls in this test that could be the focal method, each with a
probability score between 0 and 1 for how likely it is to be the method under test.
Only include methods that actually appear in the test body. Exclude assertions,
mocking calls, and standard library calls.

Reply with JSON only, no other text, in this exact form:
{{"candidates": {{"methodName": 0.9, "otherMethod": 0.1}}}}
If no method could be the focal method, reply {{"candidates": {{}}}}."""


def is_test(m):
    return any(a.name.split(".")[-1] in TEST_ANNOTATIONS for a in m.annotations)


def extract_body(src_lines, start_line):
    text = "\n".join(src_lines[start_line - 1:])
    depth = 0; started = False; i = 0; n = len(text)
    in_str = in_chr = in_line_c = in_blk_c = False
    while i < n:
        c = text[i]; nxt = text[i + 1] if i + 1 < n else ""
        if in_line_c:
            if c == "\n": in_line_c = False
        elif in_blk_c:
            if c == "*" and nxt == "/": in_blk_c = False; i += 1
        elif in_str:
            if c == "\\": i += 1
            elif c == '"': in_str = False
        elif in_chr:
            if c == "\\": i += 1
            elif c == "'": in_chr = False
        else:
            if c == "/" and nxt == "/": in_line_c = True; i += 1
            elif c == "/" and nxt == "*": in_blk_c = True; i += 1
            elif c == '"': in_str = True
            elif c == "'": in_chr = True
            elif c == "{": depth += 1; started = True
            elif c == "}":
                depth -= 1
                if started and depth == 0:
                    return text[:i + 1]
        i += 1
    return None


def parse_reply(text):
    t = text.strip()
    t = re.sub(r"^```(?:json)?|```$", "", t, flags=re.MULTILINE).strip()
    try:
        obj = json.loads(t)
    except json.JSONDecodeError:
        m = re.search(r"\{.*\}", t, re.DOTALL)
        if not m:
            return None, "no json in reply"
        try:
            obj = json.loads(m.group(0))
        except json.JSONDecodeError as e:
            return None, f"bad json: {e}"
    cands = obj.get("candidates") if isinstance(obj, dict) else None
    if not isinstance(cands, dict):
        return None, "no candidates key"
    clean = {}
    for k, v in cands.items():
        try:
            clean[str(k)] = float(v)
        except (TypeError, ValueError):
            continue
    return clean, None


def call_api(prompt, model, key, temperature, retries=3):
    body = json.dumps({
        "model": model,
        "messages": [{"role": "user", "content": prompt}],
        "temperature": temperature,
    }).encode()
    req = urllib.request.Request(
        "https://api.openai.com/v1/chat/completions",
        data=body,
        headers={"Content-Type": "application/json", "Authorization": f"Bearer {key}"},
    )
    delay = 2
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(req, timeout=90) as r:
                data = json.loads(r.read().decode())
            return data["choices"][0]["message"]["content"], None
        except urllib.error.HTTPError as e:
            detail = e.read().decode()[:200]
            if e.code == 429 or e.code >= 500:
                if attempt < retries - 1:
                    time.sleep(delay); delay *= 2; continue
            return None, f"http {e.code}: {detail}"
        except (urllib.error.URLError, TimeoutError) as e:
            if attempt < retries - 1:
                time.sleep(delay); delay *= 2; continue
            return None, f"network: {e}"
        except (KeyError, json.JSONDecodeError) as e:
            return None, f"unexpected response shape: {e}"
    return None, "retries exhausted"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("testfile")
    ap.add_argument("-o", "--out", default=None)
    ap.add_argument("--model", default="gpt-4o")
    ap.add_argument("--temperature", type=float, default=0.0)
    ap.add_argument("--cache", default="llm_cache.json")
    ap.add_argument("--dry-run", action="store_true",
                    help="show prompts, make no api calls")
    a = ap.parse_args()

    try:
        src = open(a.testfile, encoding="utf-8", errors="ignore").read()
    except OSError as e:
        print(f"[skip] cannot read file: {e}", file=sys.stderr)
        print("[]"); return
    try:
        tree = javalang.parse.parse(src)
    except Exception as e:
        print(f"[skip] could not parse Java: {e}", file=sys.stderr)
        print("[]"); return

    lines = src.split("\n")
    tests = [m for _, m in tree.filter(javalang.tree.MethodDeclaration) if is_test(m)]
    if not tests:
        print("[skip] no @Test methods found", file=sys.stderr)
        print("[]"); return

    key = os.environ.get("OPENAI_API_KEY")
    if not key and not a.dry_run:
        print("[error] OPENAI_API_KEY not set", file=sys.stderr); sys.exit(1)

    cache = {}
    if os.path.exists(a.cache):
        try: cache = json.load(open(a.cache))
        except Exception: cache = {}

    results = []
    for m in tests:
        body = extract_body(lines, m.position.line)
        if body is None:
            results.append({"test_name": m.name, "llm_focal_method": None,
                            "llm_probability": 0.0, "llm_candidates": {},
                            "error": "could not extract body"})
            continue
        prompt = PROMPT.format(name=m.name, body=body)

        if a.dry_run:
            print(f"--- {m.name} ---\n{prompt}\n", file=sys.stderr)
            results.append({"test_name": m.name, "llm_focal_method": None,
                            "llm_probability": 0.0, "llm_candidates": {},
                            "error": "dry-run"})
            continue

        ck = f"{a.model}|{a.temperature}|{m.name}|{hash(body)}"
        if ck in cache:
            cands, err = cache[ck].get("candidates"), cache[ck].get("error")
        else:
            reply, err = call_api(prompt, a.model, key, a.temperature)
            cands = None
            if reply is not None:
                cands, err = parse_reply(reply)
            cache[ck] = {"candidates": cands, "error": err}
            try: json.dump(cache, open(a.cache, "w"), indent=2)
            except OSError: pass

        if not cands:
            results.append({"test_name": m.name, "llm_focal_method": None,
                            "llm_probability": 0.0, "llm_candidates": cands or {},
                            "error": err or "no candidates returned"})
            continue

        best = max(cands.items(), key=lambda kv: kv[1])
        hallucinated = best[0] not in body
        results.append({"test_name": m.name,
                        "llm_focal_method": best[0],
                        "llm_probability": round(best[1], 3),
                        "llm_candidates": {k: round(v, 3) for k, v in cands.items()},
                        "not_in_test_body": hallucinated,
                        "error": None})

    out = json.dumps(results, indent=2)
    if a.out:
        open(a.out, "w").write(out)
        print(f"wrote {len(results)} test(s) to {a.out}", file=sys.stderr)
    else:
        print(out)


if __name__ == "__main__":
    main()
