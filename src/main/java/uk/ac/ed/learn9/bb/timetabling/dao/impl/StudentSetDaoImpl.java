/*

 */

package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ed.learn9.bb.timetabling.dao.StudentSetDao;
import uk.ac.ed.learn9.bb.timetabling.data.StudentSet;

@Transactional
@Scope("singleton")
@Component("activityDao")
public class StudentSetDaoImpl extends HibernateDaoSupport implements StudentSetDao {

    @Override
    public StudentSet getById(final String studentSetId) {
        return (StudentSet)this.getSession().get(StudentSet.class, studentSetId);
    }

    @Override
    public List<StudentSet> getAll() {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.StudentSetDao").list();
    }

    @Override
    public void refresh(final StudentSet studentSet) {
        this.getSession().refresh(studentSet);
    }

}
