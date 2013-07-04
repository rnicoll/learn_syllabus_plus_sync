package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ed.learn9.bb.timetabling.dao.SynchronisationRunDao;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

/**
 * Implementation of {@link SynchronisationRunDao}
 */
@Transactional
@Scope("singleton")
@Component
public class SynchronisationRunDaoImpl extends HibernateDaoSupport implements SynchronisationRunDao {
    @Override
    public List<SynchronisationRun> getAll() {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun").list();
    }
    
    @Override
    public SynchronisationRun getRun(int runId) {
        return (SynchronisationRun)this.getSession().get(SynchronisationRun.class, runId);
    }

    @Override
    public void refresh(SynchronisationRun run) {
        this.getSession().refresh(run);
    }
    
}
