package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;
import blackboard.data.course.Course;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentChangeDao;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;

@Transactional
@Scope("singleton")
@Component("enrolmentChangeDao")
public class EnrolmentChangeDaoImpl extends HibernateDaoSupport implements EnrolmentChangeDao {

    @Override
    public EnrolmentChange getById(final int changeId) {
        return (EnrolmentChange)this.getSession().get(EnrolmentChange.class, changeId);
    }

    @Override
    public List<EnrolmentChange> getByCourse(final Course course) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
