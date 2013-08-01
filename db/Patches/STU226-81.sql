/**
 * Add the run result code used to indicate that the threshold for removal has been
 * exceeded.
 */

INSERT INTO run_result (result_code, result_label)
  VALUES ('THRESHOLD_EXCEEDED', 'Synchronisation aborted as number of removal operations exceeds threshold.');