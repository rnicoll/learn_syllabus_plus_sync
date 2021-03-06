package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityTypeDao;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityType;

/**
 * Implementation of {@link ActivityTypeDao}
 */
@Transactional
@Scope("singleton")
@Component("activityTypeDao")
public class ActivityTypeDaoImpl extends HibernateDaoSupport implements ActivityTypeDao {

    @Override
    public ActivityType getById(final String typeId) {
        return (ActivityType)this.getSession().get(ActivityType.class, typeId);
    }

    @Override
    public List<ActivityType> getAll() {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.ActivityType").list();
    }
    
}
