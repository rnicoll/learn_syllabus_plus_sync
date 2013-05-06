package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;
import blackboard.data.course.Course;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentAddDao;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentAdd;

@Transactional
@Scope("singleton")
@Component("enrolmentAddDao")
public class EnrolmentAddDaoImpl extends HibernateDaoSupport implements EnrolmentAddDao {

    @Override
    public EnrolmentAdd getById(final int changeId) {
        return (EnrolmentAdd)this.getSession().get(EnrolmentAdd.class, changeId);
    }

    @Override
    public List<EnrolmentAdd> getByCourse(final Course course) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
