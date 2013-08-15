DELETE FROM MODULE_COURSE WHERE MODULE_COURSE_ID IN (SELECT A.MODULE_COURSE_ID FROM module_course A
  JOIN MODULE_COURSE B ON B.MODULE_COURSE_ID<A.MODULE_COURSE_ID AND
    a.tt_module_id=b.tt_module_id AND a.learn_course_code=B.learn_course_code
  WHERE a.module_course_id NOT IN (SELECT module_course_id FROM ACTIVITY_GROUP)
    AND a.module_course_id NOT IN (SELECT module_course_id FROM ENROLMENT_CHANGE_PART));
  