/* Expand width of fields for the merged courses table to ensure they can handle
 * the full width of the source data.
 */

ALTER TABLE LEARN_MERGED_COURSE
  MODIFY LEARN_SOURCE_COURSE_CODE VARCHAR2(106);
ALTER TABLE LEARN_MERGED_COURSE
  MODIFY LEARN_TARGET_COURSE_CODE VARCHAR2(106);