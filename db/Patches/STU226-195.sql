/* Expand width of fields for the module and courses tables to ensure they can handle
 * the full possible width of a Learn course code.
 */

ALTER TABLE MODULE
  MODIFY LEARN_COURSE_CODE VARCHAR2(106);
  
ALTER TABLE MODULE_COURSE
  MODIFY LEARN_COURSE_CODE VARCHAR2(106);
  