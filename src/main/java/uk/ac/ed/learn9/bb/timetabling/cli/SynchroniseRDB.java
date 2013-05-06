package uk.ac.ed.learn9.bb.timetabling.cli;

import blackboard.data.ValidationException;
import java.sql.SQLException;

import blackboard.persist.PersistenceException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationService;

/**
 * Command-line client for synchronising the RDB
 */
public class SynchroniseRDB extends Object {
    public static void main(final String[] argv)
        throws SQLException, PersistenceException, ValidationException {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        final SynchronisationService service = context.getBean(SynchronisationService.class);
        
        final long startTime = System.currentTimeMillis();
        
        service.synchroniseData();
        final SynchronisationRun run = service.generateDiff();
        service.mapModulesToCourses();
        service.createGroupsForActivities(run);
        
        System.out.println("Sync took "
                + ((System.currentTimeMillis() - startTime) / 1000L) + " seconds.");
    }
}
