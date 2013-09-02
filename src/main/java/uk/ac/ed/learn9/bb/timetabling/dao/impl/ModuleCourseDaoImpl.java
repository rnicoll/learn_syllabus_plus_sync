package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import blackboard.persist.Id;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleCourseDao;
import uk.ac.ed.learn9.bb.timetabling.data.ModuleCourse;

/**
 * Implementation of {@link ModuleCourseDao}
 */
@Transactional
@Scope("singleton")
@Component("moduleDao")
public class ModuleCourseDaoImpl extends HibernateDaoSupport implements ModuleCourseDao {

    @Override
    public ModuleCourse getById(final int moduleCourseId) {
        return (ModuleCourse)this.getSession().get(ModuleCourse.class, moduleCourseId);
    }

    @Override
    public List<ModuleCourse> getByTimetablingId(final String timetablingModuleId) {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.ModuleCourse mc "
            + "WHERE mc.module.moduleId=?").setString(0, timetablingModuleId)
                .list();
    }
    
    @Override
    public List<ModuleCourse> getByLearnId(final Id id) {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.ModuleCourse mc "
            + "WHERE mc.learnCourseId=?").setString(0, id.getExternalString())
                .list();
    }
}
