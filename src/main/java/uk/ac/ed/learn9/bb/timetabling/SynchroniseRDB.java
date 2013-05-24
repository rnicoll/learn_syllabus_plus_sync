package uk.ac.ed.learn9.bb.timetabling;

import blackboard.data.ValidationException;
import java.sql.SQLException;

import blackboard.persist.PersistenceException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.service.ConcurrencyService;
import uk.ac.ed.learn9.bb.timetabling.service.ConcurrencyService.SynchronisationAlreadyInProgressException;

import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationService;

/**
 * Command-line client for synchronising the RDB
 */
public class SynchroniseRDB extends Object {
    public static void main(final String[] argv)
        throws SQLException, PersistenceException, ValidationException, SynchronisationAlreadyInProgressException {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        final ConcurrencyService concurrencyService = context.getBean(ConcurrencyService.class);
        final SynchronisationService synchronisationService = context.getBean(SynchronisationService.class);
        
        final long startTime = System.currentTimeMillis();
        
        final SynchronisationRun run = concurrencyService.startNewRun();
        
        synchronisationService.synchroniseTimetablingData();
        synchronisationService.synchroniseEugexData();
        // XXX: Handle merged courses here
        synchronisationService.generateDiff(run);
        synchronisationService.updateGroupDescriptions();
        //service.mapModulesToCourses();
        //service.createGroupsForActivities(run);
        //service.mapStudentSetsToUsers(run);
        //service.applyEnrolmentChanges(run);
        
        // XXX: Mark the run completed
        
        System.out.println("Sync took "
                + ((System.currentTimeMillis() - startTime) / 1000L) + " seconds.");
    }
}
