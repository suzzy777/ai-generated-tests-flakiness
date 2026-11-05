The 5 new focal methods (rows 16-30, generated 2026-10-30): the test files that go into the flaky checks
(run_new10_A.sh on machine A, machine_b_new10 / machine_c_new10). Copies; the originals are unchanged.
  prompt_default/, prompt_cleanup/  ChatUniTest: only the files that passed alone in the replay (prompt_experiment/task_csvs/new10_tests.csv)
  agent_A/, agent_F/, agent_FC/, agent_FCV/  every generated file (agent_experiment/task_csvs/new10_agent_tests.csv);
      as for rows 1-15, an agent file is dropped if a method fails every one of its 100 reruns (broken test); one failing only some reruns is flaky alone and kept. Known when the checks finish.
tests_info.csv in each folder: one line per test method. all_tests.csv: all folders.
tests.csv in each folder: the same methods with the columns of the two CSVs above (incl. mockito_fix) plus file_here, as in flaky_checked_tests.
BROKEN_DO_NOT_RUN.txt / .csv: the 13 broken agent files (fail every rerun alone); leave them out of flaky checks.
