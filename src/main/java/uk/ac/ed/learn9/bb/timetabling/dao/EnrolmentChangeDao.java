package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import blackboard.data.course.Course;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;

public interface EnrolmentChangeDao {
    public EnrolmentChange getById(final int changeId);
    public List<EnrolmentChange> getByCourse(final Course course);
}
