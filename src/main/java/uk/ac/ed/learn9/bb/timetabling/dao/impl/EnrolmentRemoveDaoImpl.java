package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;
import blackboard.data.course.Course;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentRemoveDao;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentRemove;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentRemove;

@Transactional
@Scope("singleton")
@Component("enrolmentRemoveDao")
public class EnrolmentRemoveDaoImpl extends HibernateDaoSupport implements EnrolmentRemoveDao {

    @Override
    public EnrolmentRemove getById(final int changeId) {
        return (EnrolmentRemove)this.getSession().get(EnrolmentRemove.class, changeId);
    }

    @Override
    public List<EnrolmentRemove> getByCourse(final Course course) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
