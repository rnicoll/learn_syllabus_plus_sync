package uk.ac.ed.learn9.bb.timetabling.cli;

import java.sql.SQLException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationService;

/**
 * Command-line client for synchronising the RDB
 */
public class SynchroniseRDB extends Object {
    public static void main(final String[] argv) throws SQLException {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        final SynchronisationService service = context.getBean(SynchronisationService.class);
        
        final long startTime = System.currentTimeMillis();
        
        service.synchroniseData();
        service.generateDiff();
        service.mapModulesToCourses();
        
        System.out.println("Sync took "
                + ((System.currentTimeMillis() - startTime) / 1000L) + " seconds.");
    }
}
