package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityTemplateDao;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityTemplate;

@Transactional
@Scope("singleton")
@Component("templateDao")
public class ActivityTemplateDaoImpl extends HibernateDaoSupport implements ActivityTemplateDao {

    @Override
    public ActivityTemplate getById(final int changeId) {
        return (ActivityTemplate)this.getSession().get(ActivityTemplate.class, changeId);
    }

    @Override
    public List<ActivityTemplate> getAll() {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.ActivityTemplate").list();
    }
}
