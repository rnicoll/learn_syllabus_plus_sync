package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import blackboard.persist.Id;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityDao;
import uk.ac.ed.learn9.bb.timetabling.data.Activity;

/**
 * Implementation of {@link ActivityDao}
 */
@Transactional
@Scope("singleton")
@Component("activityDao")
public class ActivityDaoImpl extends HibernateDaoSupport implements ActivityDao {

    @Override
    public Activity getById(final String activityId) {
        return (Activity)this.getSession().get(Activity.class, activityId);
    }

    @Override
    public List<Activity> getAll() {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.Activity").list();
    }
    
    @Override
    public List<Activity> getByCourseLearnId(final Id id) {
        return this.getSession().createQuery("SELECT a FROM uk.ac.ed.learn9.bb.timetabling.data.Activity a "
                + "JOIN a.module as m "
                + "JOIN m.courses as mc "
            + "WHERE mc.learnCourseId=?").setString(0, id.getExternalString())
                .list();
    }

    @Override
    public void refresh(final Activity activity) {
        this.getSession().refresh(activity);
    }
    
}
