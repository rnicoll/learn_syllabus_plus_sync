package uk.ac.ed.learn9.bb.timetabling.dao;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

@Transactional
@Scope("singleton")
@Component("runDao")
public class SynchronisationRunDao extends HibernateDaoSupport {
    public SynchronisationRun getRun(int runId) {
        return (SynchronisationRun)this.getSession().get(SynchronisationRun.class, runId);
    }
    
}
