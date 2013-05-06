package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import blackboard.data.course.Course;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentRemove;

public interface EnrolmentRemoveDao {
    public List<EnrolmentRemove> getById(final int changeId);
    public List<EnrolmentRemove> getByCourse(final Course course);
}
