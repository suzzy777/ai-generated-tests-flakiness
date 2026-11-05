# Artifact: Flakiness of LLM-Generated Tests

This folder has the code, results and generated tests of our paper.

- `code/` has the scripts, prompts and input lists.
- `results/` has all results, one folder per study.
- `generated_tests/` has the generated test files that we checked for flakiness, with a `tests.csv` per folder (project, commit, module, JDK, JUnit version, focal method).

## Results

`results/1_main_study/` is the main study with ChatUniTest. It has the generation results for the flaky and non-flaky side (plus hadoop and graylog, which we ran separately), how we selected the non-flaky side, which generated files pass alone, and the flakiness checks (reruns, NonDex, NIO, iDFlakies and the OD confirmation).

`results/2_chatunitest_prompts/` is ChatUniTest with its default prompt and with the cleanup prompt, on the 10 focal methods.

`results/3_agent/` is mini-swe-agent under conditions A, F, FC and FCV. Every run is in `runs/<condition>/runs/row_NNN/`, with its trajectory, the test it wrote, and any other file it changed. `runs/<condition>/generation_results.csv` has the status, steps and cost of each run.

`results/4_od_followup/` has the recheck of rejected victims, which methods of each polluter file pollute, the check that an `@After` method fixes the victims, and the elastic-job timing test.

`results/5_quality/` has coverage (JaCoCo) and mutation scores (PIT).

`results/6_stress_ng/` and `results/7_flakesync/` have the stress check and the FlakeSync check.

`results/8_cost_and_step_limit/` has the cost per run and the runs that hit the step limit.

The 10 focal methods use rows 1-30 (three runs each). `code/prompt_experiment/manifest.csv` and `manifest_new.csv` list the focal method of each row.

## Code

The main scripts are:

- `batch_reproflake_chatunitest.py`: finds the focal method of each flaky test (with `focal_scripts/`) and runs ChatUniTest.
- `replay_generated.py`: runs each generated file alone.
- `flaky_check_basic.py`: reruns, NonDex, NIO check and iDFlakies.
- `od_confirm.py`: confirms the OD tests that iDFlakies reports. It first narrows down the candidates by halving, then runs one generated method before the developer test, twice.
- `prompt_experiment/`: the ChatUniTest prompts and how we ran them.
- `agent_experiment/`: the agent (`run_agent.py`, `agent_config.yaml`) and its checks.
- `quality/covmut.py`: coverage and mutation score.
- `stress_ng/` and `flakesync/`: the stress and FlakeSync checks.
- `cost_table.py` and `step_limit_loops/loops.py`: cost and step-limit loops.

## Running the scripts


The scripts expect the results at their original paths (for example `out_part1/`). To run them, first run `bash code/link_results.sh` once, which links those paths to `results/`, and then run the scripts from `code/`.

Linux, Python 3.10 with `javalang`, Maven, and the JDK of each project (8, 11 or 17) is needed. Install the patched ChatUniTest with `code/generation_package/install_chatunitest.sh`, and the agent with `pip install -r code/agent_experiment/requirements.txt`. The LLM steps need `OPENAI_API_KEY` (we used `gpt-4o-2024-08-06` with temperature 0.5). The projects are the ReproFlake archives on Zenodo; the scripts download them.
