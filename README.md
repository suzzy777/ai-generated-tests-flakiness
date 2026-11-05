# Artifact: Flakiness of LLM-Generated Tests

This package has the scripts, inputs, results and generated tests of the paper.
The folder layout is the one the scripts expect, so run every command from this folder.

## Requirements

- Linux (we used Ubuntu 22.04), Python 3.10, Maven 3.6.3, and the JDK that each project needs (8, 11 or 17).
- `pip install javalang`; for the agent, `pip install -r agent_experiment/requirements.txt` (mini-swe-agent 2.4.6).
- ChatUniTest 2.1.1, patched for JUnit 4: `bash generation_package/install_chatunitest.sh`.
- iDFlakies, NonDex, JaCoCo 0.8.12 and PIT 1.15.2 are Maven plugins; the scripts download them.
- stress-ng 0.20.01, and FlakeSync (commit 54ebfa3 plus `flakesync/flakesync_changes_54ebfa3.patch`).
- `OPENAI_API_KEY` in the environment for the steps that call the LLM (model `gpt-4o-2024-08-06`, temperature 0.5).

The projects are the ReproFlake archives on Zenodo (the URLs are in `result_matched.csv`).
The scripts download them with `--download`, or `nonflaky_check_package/setup_archives.py` does.

The experiments on 10 focal methods use rows 1-30 (3 runs per focal method); `prompt_experiment/manifest.csv` and `manifest_new.csv` give the focal method of each row.

## Steps and where their files are

### 1. Dataset and focal methods
- `result_matched.csv`: the 144 ReproFlake flaky tests (36 per type).
- `generate_part1.csv`, `generate_part2.csv`: their focal methods (name-based Jaccard and LLM, `selected_method` where both agree).
  Made by `batch_reproflake_chatunitest.py prepare --csv result_matched.csv --focal-scripts focal_scripts --run-llm --download`.
- Non-flaky side: `collect_nonflaky.py` lists developer tests of the same modules (`nonflaky_out/`, `nonflaky_out2/`, `nonflaky_chunks/`);
  `run_focal_chunks.sh` finds their focal methods (`nonflaky_focal_out/`); `task_csvs/` holds the sampled lists.

### 2. Main study: generation with ChatUniTest
- `batch_reproflake_chatunitest.py generate --manifest generate_part1.csv --junit4-dir generation_package/chatunitest --out out_part1 --execute --resume` (same for part 2).
- Results: `out_part1/`, `out_part2/` (flaky side), `nonflaky_check_package/generation_results.csv`, `nonflaky_gen/` (non-flaky side),
  `hadoop_check_package/` (the hadoop module), `graylog_rerun/` (graylog2-server with a 3-hour limit).
- Default ChatUniTest prompts: `generation_package/chatunitest/prompts-junit4/`.

### 3. Pass-alone filter
- `replay_generated.py` runs each generated file alone, then again with the Mockito runner fix (`--mockito-fix`).
- Results: `genreplay_*_out/`, `nonflaky_check_package/genreplay_*_out/`, `prompt_experiment/replay_*/`, `agent_experiment/replay_FCV_dubbo_redo/`.

### 4. Flakiness checks
- `flaky_check_basic.py {nio,nondex,idflakies,rerun,summary} --tests <csv>`: NIO check (100), NonDex (100 seeds), iDFlakies (20 rounds), 100 reruns.
  Drivers: `run_flaky_all.sh` (flaky side), `nonflaky_check_package/run_all.sh` (non-flaky side), `hadoop_check_package/run.sh`.
- Results: `flaky_check_out/`, `nonflaky_check_package/flaky_check_out/`, `hadoop_check_package/machine_b_results/`.
- Stress check (CPU and memory load, 10 runs each): `stress_ng/scripts/`, results `stress_ng/stress_*.csv`.
- FlakeSync (time-dependent check): `flakesync/`, results `flakesync/results/`.

### 5. Confirming OD tests
- `od_confirm.py`: runs each generated test method before the developer test that iDFlakies reports, twice (`run_od_confirm.sh`).
  Results: `flaky_check_out/od_confirm_all.csv` and `od_confirm/` in each check folder.
- `od_recheck/od_recheck.py`: rechecks rejected victims one method at a time (`od_recheck/*/recheck.csv`).
- `pairwise_check/`, `pairwise_all/`: which methods of each polluter file pollute; `pairwise_check/setters/`: state-setters.
- `od_fix_check.py`: adds an `@After` that restores the state and reruns the victims (`od_fix_check/`).
- `new10_checks/recheck_elastic.py`: the elastic-job timing test (fails also after unrelated tests).

### 6. Experiments on the 10 focal methods
- ChatUniTest, default and cleanup prompt (`prompt_experiment/prompts/`):
  `prompt_experiment/run_generation.sh` (rows 1-15) and `run_generation_new.sh` (rows 16-30);
  checks `prompt_experiment/run_idflakies.sh` and `run_other_checks.sh`;
  results `prompt_experiment/out_*`, `prompt_experiment/out_idflakies/`, `prompt_experiment/machine_b_idflakies_results/`, `machine_b_other_checks/`.
- Agent (mini-swe-agent), conditions A, F, FC, FCV (prompts in `agent_experiment/agent_config.yaml` and `agent_experiment/run_agent.py`):
  `CONDITIONS="A F FC FCV" bash agent_experiment/run_agent.sh 1 2 ... 30`.
  Each run: `agent_experiment/out_<cond>/runs/row_NNN/` (`trajectory.json`, the test in `tests/`, `changes.txt`);
  summary `agent_experiment/out_<cond>/generation_results.csv` (status, steps, cost).
- Checks of the agent tests: `bash agent_experiment/run_checks.sh <cond>`; results `agent_experiment/out_flaky_<cond>/`,
  `agent_experiment/machine_B_FC_results/` (FC rows 1-6), `machine_C_FCVdubbo_results/` (FCV rows 4-6).
- Rows 16-30 (both generators): `run_new10_A.sh`; results `new10_checks/`, `machine_b_new10_results/` (rows 19-21), `machine_c_new10_results/` (rows 25-27).
- Victim check after the known developer polluter: `agent_experiment/direct_victim_check.py` (`agent_experiment/direct_victim_check/`).
- Which runs read the existing tests: `agent_experiment/count_read_tests.py` (`agent_experiment/read_tests.csv`).

### 7. Quality, cost and step limit
- Coverage and mutation score: `quality/covmut.py` (drivers `quality/run_*.sh`); results `quality/out_*/`;
  tables `quality/summarize_main.py`, `quality/class_level.py` (`quality/class_level.csv`), `quality/createschema_selected.py`.
- Cost: `cost_table.py` (`cost_table.csv`).
- Runs that hit the step limit: `step_limit_loops/loops.py` (`step_limit_loops/results.txt`).
- `analysis/verify_counts.py` recomputes the per-run table from the result files.

### 8. Generated tests
- `generated_tests/flaky_checked_tests/`, `generated_tests/flaky_checked_tests_new10/`: every generated test file that went into the flakiness checks,
  with `tests.csv` / `tests_info.csv` (project, commit, module, JDK, JUnit version, focal method).

## Notes
- Some checks ran on two other machines with the same scripts; their results are in the `machine_*` folders.
- `nonflaky_check_package/`, `hadoop_check_package/` and `agent_experiment/` have their own copies of the shared scripts, as they were run.
- Paths in the result files are relative to this folder (`./`) or start with `~/`.
