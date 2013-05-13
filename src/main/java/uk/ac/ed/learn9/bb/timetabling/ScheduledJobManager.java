package uk.ac.ed.learn9.bb.timetabling;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import blackboard.data.ValidationException;
import blackboard.persist.PersistenceException;
import blackboard.platform.log.LogService;
import blackboard.platform.log.LogServiceFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationService;

public class ScheduledJobManager extends Object implements ApplicationListener<ApplicationContextEvent> {
    public static final int START_HOUR_OF_DAY = 6;
    public static final int START_MINUTE = 0;
    public static final long RUN_INTERVAL = 24 * 60 * 60 * 1000L;
    
    @Autowired
    private SynchronisationService service;
    
    private final Timer timer = new Timer("Timetabling Group Sync", true);
    private boolean cancelled = false;
    private Task task = new Task();
    private LogService logService;
    
    public              ScheduledJobManager() {
        
    }

    /**
     * Cancels any scheduled synchronisation.
     */
    public void cancel() {
        this.cancelled = true;
        this.timer.cancel();
    }

    /**
     * Schedules the synchronisation task.
     */
    public void scheduleRun() {
        final long delay = calculateDelay();
        
        this.logService.logInfo("Scheduling synchronisation job after a delay of "
            + delay + "ms.");
        
        this.timer.schedule(this.task, delay);
    }

    /**
     * Calculates the delay (in milliseconds) before running the synchronisation
     * again.
     * 
     * @return the delay in milliseconds.
     */
    public long calculateDelay() {
        final Date now = new Date();
        final Calendar calendar = Calendar.getInstance();
        
        calendar.setTime(now);
        
        // Zero the seconds & millis first
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        
        // Set the time of day
        calendar.set(Calendar.MINUTE, START_MINUTE);
        calendar.set(Calendar.HOUR_OF_DAY, START_HOUR_OF_DAY);
        
        if (calendar.getTime().before(now)) {
            // The calendar now reflects a time in the past, so add a day.
            calendar.add(Calendar.DATE, 1);
        }
        
        return calendar.getTime().getTime() - now.getTime();
    }

    /**
     * Retrieves the synchronisation service for this task.
     * 
     * @return the synchronisation service
     */
    public SynchronisationService getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(SynchronisationService service) {
        this.service = service;
    }

    @Override
    public void onApplicationEvent(final ApplicationContextEvent e) {
        if (e instanceof ContextStartedEvent) {
            this.logService = LogServiceFactory.getInstance();
            this.scheduleRun();
        } else if (e instanceof ContextStoppedEvent) {
            this.cancel();
        }
    }
    
    public class Task extends TimerTask {
        @Override
        public void run() {
            ScheduledJobManager.this.logService.logInfo("Running Learn/Timetabling synchronisation.");
            
            /* final SynchronisationService service = ScheduledJobManager.this.getService();
            try {
                doSynchronisation(service);
            } catch(PersistenceException e) {
                ScheduledJobManager.this.logService.logError("Error while persisting/loading entities in Learn.", e);
            } catch(SQLException e) {
                ScheduledJobManager.this.logService.logError("Database error while synchronising groups from Timetabling.", e);
            } catch(ValidationException e) {
                ScheduledJobManager.this.logService.logError("Error validating entities to be persisted in Learn.", e);
            } */
            
            if (!ScheduledJobManager.this.cancelled) {
                ScheduledJobManager.this.scheduleRun();
            }
        }

        private void doSynchronisation(final SynchronisationService service)
            throws PersistenceException, SQLException, ValidationException {
            service.synchroniseData();
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            final SynchronisationRun run = service.generateDiff();
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            service.updateGroupDescriptions();
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            service.mapModulesToCourses();
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            service.createGroupsForActivities(run);
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            service.mapStudentSetsToUsers(run);
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            service.applyEnrolmentChanges(run);
        }
    }
}
