package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ed.learn9.bb.timetabling.dao.ActivityGroupDao;
import uk.ac.ed.learn9.bb.timetabling.data.Activity;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityGroup;

/**
 * Implementation of {@link ActivityGroupDao}
 */
@Transactional
@Scope("singleton")
@Component("moduleDao")
public class ActivityGroupDaoImpl extends HibernateDaoSupport implements ActivityGroupDao {

    @Override
    public List<ActivityGroup> getByActivity(Activity activity) {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.ActivityGroup ag "
            + "WHERE ag.activity.activityId=?").setString(0, activity.getActivityId())
                .list();
    }
    
}
