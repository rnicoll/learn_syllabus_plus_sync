package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityDao;
import uk.ac.ed.learn9.bb.timetabling.data.cache.Activity;

@Transactional
@Scope("singleton")
@Component("templateDao")
public class ActivityDaoImpl extends HibernateDaoSupport implements ActivityDao {

    @Override
    public Activity getById(final int activityId) {
        return (Activity)this.getSession().get(Activity.class, activityId);
    }

    @Override
    public List<Activity> getAll() {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.cache.Activity").list();
    }
    
}
