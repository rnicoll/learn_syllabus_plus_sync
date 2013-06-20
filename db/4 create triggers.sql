set define off;
CREATE OR REPLACE TRIGGER ACTIVITY_GROUP_PK
  BEFORE INSERT ON ACTIVITY_GROUP
    for each row
    begin
      select ACTIVITY_GROUP_SEQ.nextval into :new.activity_group_id from dual;
    end;
/
CREATE OR REPLACE TRIGGER ENROLMENT_CHANGE_PK
  BEFORE INSERT ON ENROLMENT_CHANGE
    for each row
    begin
      select ENROLMENT_CHANGE_SEQ.nextval into :new.change_id from dual;
    end;
/
CREATE OR REPLACE TRIGGER ENROLMENT_CHANGE_PART_PK
  BEFORE INSERT ON ENROLMENT_CHANGE_PART
    for each row
    begin
      select ENROLMENT_CHANGE_PART_SEQ.nextval into :new.part_id from dual;
    end;
/
CREATE OR REPLACE TRIGGER MODULE_COURSE_PK
  BEFORE INSERT ON MODULE_COURSE
    for each row
    begin
      select MODULE_COURSE_SEQ.nextval into :new.module_course_id from dual;
    end;
/

CREATE OR REPLACE TRIGGER course_code_ins BEFORE INSERT OR UPDATE ON module
   FOR EACH ROW WHEN ( REGEXP_LIKE (new.tt_course_code, '^[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+_[A-Z][A-Z0-9\\+]+$') )
   BEGIN
     :new.cache_course_code := SUBSTR(:new.tt_course_code, 0, INSTR(:new.tt_course_code, '_')-1);
     :new.cache_semester_code := SUBSTR(:new.tt_course_code, INSTR(:new.tt_course_code, '_', LENGTH(:new.cache_course_code)+2)+1);
     :new.cache_occurrence_code := SUBSTR(:new.tt_course_code, LENGTH(:new.cache_course_code)+2, (LENGTH(:new.tt_course_code) - LENGTH(:new.cache_course_code) - LENGTH(:new.cache_semester_code) - 2));
     :new.learn_academic_year := REPLACE(:new.tt_academic_year, '/', '-');
     :new.learn_course_code := :new.cache_course_code || :new.learn_academic_year || :new.cache_occurrence_code || REPLACE(:new.cache_semester_code, '+', 'plus');
   END;
/
SET DEFINE ON;
