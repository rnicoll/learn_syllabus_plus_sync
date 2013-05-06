package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import blackboard.data.course.Course;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentAdd;

public interface EnrolmentAddDao {
    public List<EnrolmentAdd> getById(final int changeId);
    public List<EnrolmentAdd> getByCourse(final Course course);
}
