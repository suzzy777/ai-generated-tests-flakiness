The generated test files that went into the flaky checks (iDFlakies, NonDex, NIO, reruns), i.e. the
"Pass alone (files)" column of MEETING_SUMMARY.md. Copies of the exact files the checks used.
tests.csv in each folder: one row per test method (from the check's test list), file_here = file in this folder.

main_flaky_side_23/     main study, focal methods of flaky tests      (task_csvs/flaky_generated_tests.csv)
main_nonflaky_side_94/  main study, focal methods of non-flaky tests  (td_nonflaky_tests_package/, set=main)
prompt_default_8/       prompt experiment, ChatUniTest's own prompt   (prompt_experiment/task_csvs/experiment_generated_tests.csv)
prompt_cleanup_12/      prompt experiment, cleanup prompt             (same csv)
agent_A_13/             agent A (agent_experiment/task_csvs/agent_A_tests.csv), without rows 5 and 9 (fail alone)
agent_F_11/             agent F (agent_F_tests.csv; row 3 made no test), without rows 6, 13, 14 (fail alone)
  (For the agents, the checks ran on all files; "fails alone" = a method failed every one of its 100 reruns (broken test). A method failing only some reruns would be flaky alone and kept; there were none.)

tests_info.csv in each folder (same rows as tests.csv, fewer columns): row, project, sha, module, zip, java,
junit, test_file, test_class, test_method, focal_method, focal_from_test. all_tests.csv = all folders in one file.
agent_FC_12/            agent FC (F + cleanup sentences), without rows 1, 3, 9 (fail alone)
agent_FCV_9/            agent FCV (FC + victim sentence): dubbo rows 4-6 (regenerated on machine A 2026-10-30; passed the
                        replay) and rows 8-13; rows 1-3 (fail alone), 7, 14, 15 (fail alone) left out
