
import json
import sys
from typing import Dict, List, Tuple, Optional


def jaccard_similarity(set1: set, set2: set) -> float:
    if not set1 and not set2:
        return 0.0
    intersection = len(set1 & set2)
    union = len(set1 | set2)
    return intersection / union if union else 0.0


def find_focal_method(
    test_tokens: List[str],
    candidates: Dict[str, List[str]],
) -> Tuple[Optional[str], float]:
    if not candidates:
        return None, 0.0

    test_set = set(test_tokens)
    best_method = None
    best_score = -1.0

    for method_name, tokens in candidates.items():
        score = jaccard_similarity(test_set, set(tokens))
        if score > best_score:
            best_score = score
            best_method = method_name

    return best_method, best_score


def find_focal_method_ranked(
    test_tokens: List[str],
    candidates: Dict[str, List[str]],
) -> List[Tuple[str, float]]:
    test_set = set(test_tokens)
    scored = [
        (name, jaccard_similarity(test_set, set(tokens)))
        for name, tokens in candidates.items()
    ]
    scored.sort(key=lambda pair: pair[1], reverse=True)
    return scored


def load_input(path: str) -> List[dict]:
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def process_all(entries: List[dict]) -> List[dict]:
    results = []
    for entry in entries:
        test_name = entry.get("test_name", "UNKNOWN")
        test_tokens = entry.get("test_tokens", [])
        candidates = entry.get("candidates", {})

        ranked = find_focal_method_ranked(test_tokens, candidates)
        best_method, best_score = (ranked[0] if ranked else (None, 0.0))

        is_ambiguous = (
            len(ranked) >= 2 and abs(ranked[0][1] - ranked[1][1]) < 0.01
        )

        results.append({
            "test_name": test_name,
            "focal_method": best_method,
            "score": round(best_score, 3),
            "ranked_candidates": [[n, round(s, 3)] for n, s in ranked],
            "ambiguous": is_ambiguous,
            "no_candidates_found": len(candidates) == 0,
        })
    return results


def save_output(results: List[dict], path: str) -> None:
    with open(path, "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2)


def main():
    if len(sys.argv) != 3:
        print("Usage: python focal_method_finder_batch.py <input.json> <output.json>")
        print("Running with built-in mock data instead...\n")
        entries = MOCK_ENTRIES
        output_path = "mock_output.json"
    else:
        entries = load_input(sys.argv[1])
        output_path = sys.argv[2]

    results = process_all(entries)
    save_output(results, output_path)

    print(f"Processed {len(results)} test(s). Results written to {output_path}\n")
    for r in results:
        flag = ""
        if r["no_candidates_found"]:
            flag = "  [NO CANDIDATES - flag for Shanto]"
        elif r["ambiguous"]:
            flag = "  [AMBIGUOUS TIE - flag for Shanto]"
        print(f"  {r['test_name']:20s} -> {r['focal_method']} (score={r['score']}){flag}")


MOCK_ENTRIES = [
    {
        "test_name": "testGetFromMap",
        "test_tokens": ["test", "get", "from", "map"],
        "candidates": {
            "getFromMap": ["get", "from", "map"],
            "createMap": ["create", "map"],
            "setUp": ["set", "up"],
            "putIntoMap": ["put", "into", "map"],
        },
    },
    {
        "test_name": "testValidateInput",
        "test_tokens": ["test", "validate", "input"],
        "candidates": {},
    },
]


if __name__ == "__main__":
    main()