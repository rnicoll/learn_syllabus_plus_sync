package uk.ac.ed.learn9.bb.timetabling.dao;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ed.learn9.bb.timetabling.data.SynchronizationRun;

@Transactional
@Scope("singleton")
@Component("runDao")
public class SynchronizationRunDao extends HibernateDaoSupport {
    public SynchronizationRun getRun(int runId) {
        return (SynchronizationRun)this.getSession().get(SynchronizationRun.class, runId);
    }
    
}
