package uk.ac.ed.learn9.bb.timetabling.cli;

import java.sql.SQLException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ed.learn9.bb.timetabling.service.SynchronizationService;

/**
 * Command-line client for synchronizing the RDB
 */
public class SynchronizeRDB extends Object {
    public static void main(final String[] argv) throws SQLException {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        final SynchronizationService service = context.getBean(SynchronizationService.class);
        
        final long startTime = System.currentTimeMillis();
        
        service.syncModulesAndActivities();
        service.generateDiff();
        
        System.out.println("Sync took "
                + ((System.currentTimeMillis() - startTime) / 1000L) + " seconds.");
    }
}
