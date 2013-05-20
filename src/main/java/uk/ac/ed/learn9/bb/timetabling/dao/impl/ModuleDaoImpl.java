package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleDao;
import uk.ac.ed.learn9.bb.timetabling.data.cache.Module;

@Transactional
@Scope("singleton")
@Component("moduleDao")
public class ModuleDaoImpl extends HibernateDaoSupport implements ModuleDao {

    @Override
    public Module getById(final int changeId) {
        return (Module)this.getSession().get(Module.class, changeId);
    }

    @Override
    public List<Module> getAll() {
        return this.getSession().createQuery("FROM uk.ac.ed.learn9.bb.timetabling.data.cache.Module").list();
    }
    
}
