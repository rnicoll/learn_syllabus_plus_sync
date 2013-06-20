package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import blackboard.data.course.Course;
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentChangePartDao;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChangePart;

/**
 * Implementation of {@link EnrolmentChangeDao}
 */
@Transactional
@Scope("singleton")
@Component("enrolmentChangeDao")
public class EnrolmentChangePartDaoImpl extends HibernateDaoSupport implements EnrolmentChangePartDao {

    @Override
    public EnrolmentChangePart getById(final int changeId) {
        return (EnrolmentChangePart)this.getSession().get(EnrolmentChangePart.class, changeId);
    }

    @Override
    public List<EnrolmentChangePart> getByCourse(final Course course) {
        return (List<EnrolmentChangePart>)this.getSession().createCriteria(EnrolmentChangePart.class)
            .createCriteria("change")
            .createCriteria("activity")
            .createCriteria("module")
            .createCriteria("module_course")
            .add(Restrictions.eq("learnCourseId", course.getId().getExternalString()))
                .list();
    }
    
}