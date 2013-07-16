package uk.ac.ed.learn9.bb.timetabling.dao.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ed.learn9.bb.timetabling.dao.ConfigurationDao;
import uk.ac.ed.learn9.bb.timetabling.data.Configuration;


@Transactional
@Scope("singleton")
@Component("configurationDao")
public class ConfigurationDaoImpl extends HibernateDaoSupport implements ConfigurationDao {
    @Override
    public Configuration getDefault() {
        Configuration configuration = this.get(Configuration.DEFAULT_CONFIGURATION_ID);
        
        if (null == configuration) {
            configuration = new Configuration(Configuration.DEFAULT_CONFIGURATION_ID);
            this.getSession().persist(configuration);
        }
        
        return configuration;
    }
    
    public Configuration get(final int serial) {
        return (Configuration)this.getSession().get(Configuration.class, serial);
    }
}
