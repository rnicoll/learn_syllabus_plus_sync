package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.cache.Module;

public interface ModuleDao {
    public Module getById(final int moduleId);
    public List<Module> getAll();
}
