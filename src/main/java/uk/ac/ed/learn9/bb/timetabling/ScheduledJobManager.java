package uk.ac.ed.learn9.bb.timetabling;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import blackboard.data.ValidationException;
import blackboard.persist.PersistenceException;
import blackboard.platform.log.LogService;
import blackboard.platform.log.LogServiceFactory;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.service.ConcurrencyService;
import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationService;

/**
 * Application life-cycle listener used to start the scheduled synchronisation
 * tasks when the application context is created.
 */
public class ScheduledJobManager extends Object implements ApplicationListener<ApplicationContextEvent> {
    public static final int START_HOUR_OF_DAY = 6;
    public static final int START_MINUTE = 0;
    public static final long RUN_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L;
    public static final int MAX_FUZZ_MILLIS = 60 * 1000;
    
    @Autowired
    private ConcurrencyService concurrencyService;
    @Autowired
    private SynchronisationService synchronisationService;
    
    private final Timer timer = new Timer("Timetabling Group Sync", true);
    private boolean cancelled = false;
    private Task task = new Task();
    private LogService logService;
    
    public              ScheduledJobManager() {
        
    }

    /**
     * Listens for application lifecycle events (specifically start/stop),
     * and schedules the new run/cancels the next run as appropriate.
     * 
     * @param e the application event to process.
     */
    @Override
    public void onApplicationEvent(final ApplicationContextEvent e) {
        if (e instanceof ContextStartedEvent) {
            this.logService = LogServiceFactory.getInstance();
            this.scheduleRun();
        } else if (e instanceof ContextStoppedEvent) {
            this.cancel();
        }
    }

    /**
     * Cancels any scheduled synchronisation.
     */
    public void cancel() {
        this.cancelled = true;
        this.timer.cancel();
    }

    /**
     * Schedules the next run of the synchronisation task.
     */
    public void scheduleRun() {
        // We apply a small fuzz value to the delay to help avoid risk of race
        // conditions if two jobs start simultaneously.
        final long fuzz = Math.round(Math.random() * MAX_FUZZ_MILLIS);
        final long delay = calculateDelay(System.currentTimeMillis());
        
        this.logService.logInfo("Scheduling synchronisation job after a delay of "
            + delay + "ms.");
        
        this.timer.schedule(this.task, delay + fuzz);
    }

    /**
     * Calculates the delay (in milliseconds) before running the synchronisation
     * again.
     * 
     * @return the delay in milliseconds.
     */
    public long calculateDelay(final long nowMillis) {
        final Calendar calendar = Calendar.getInstance();
 
        // Strip seconds & milliseconds
        final long thisMinuteMillis = nowMillis - (nowMillis % (60 * 1000L));
        
        calendar.setTimeInMillis(thisMinuteMillis);
        
        // Set the time of day
        calendar.set(Calendar.MINUTE, START_MINUTE);
        calendar.set(Calendar.HOUR_OF_DAY, START_HOUR_OF_DAY);
        calendar.add(Calendar.DATE, 1);
        
        return calendar.getTimeInMillis() - nowMillis;
    }

    /**
     * Gets the concurrency service for the scheduled tasks to use to ensure
     * only one task is running at  time.
     * 
     * @return the concurrency service.
     */
    public ConcurrencyService getConcurrencyService() {
        return concurrencyService;
    }

    /**
     * Gets the synchronisation service for this task.
     * 
     * @return the synchronisation service.
     */
    public SynchronisationService getSynchronisationService() {
        return synchronisationService;
    }

    /**
     * Sets the concurrency service for the scheduled tasks to use to ensure
     * only one task is running at  time.
     * 
     * @param concurrencyService the concurrency service to set
     */
    public void setConcurrencyService(ConcurrencyService concurrencyService) {
        this.concurrencyService = concurrencyService;
    }

    /**
     * Sets the synchronisation service for the scheduled tasks to use.
     * 
     * @param service the synchronisation service to set.
     */
    public void setSynchronisationService(SynchronisationService service) {
        this.synchronisationService = service;
    }
    
    public class Task extends TimerTask {
        @Override
        public void run() {
            ScheduledJobManager.this.logService.logInfo("Running Learn/Timetabling synchronisation.");
            SynchronisationRun run;
            
            try {
                run = ScheduledJobManager.this.getConcurrencyService().startNewRun();
            }  catch (ConcurrencyService.SynchronisationAlreadyInProgressException ex) {
                // This is expected under normal circumstances, due to more than one
                // possible server trying to run the job. Ignore.
                run = null;
            } catch(SQLException e) {
                ScheduledJobManager.this.logService.logError("Database error while starting new synchronisation run.", e);
                run = null;
            }
            
            if (null != run) {
                final SynchronisationService service = ScheduledJobManager.this.getSynchronisationService();
                try {
                    doSynchronisation(run, service);
                } catch(PersistenceException e) {
                    ScheduledJobManager.this.logService.logError("Error while persisting/loading entities in Learn.", e);
                } catch(SQLException e) {
                    ScheduledJobManager.this.logService.logError("Database error while synchronising groups from Timetabling.", e);
                } catch(ValidationException e) {
                    ScheduledJobManager.this.logService.logError("Error validating entities to be persisted in Learn.", e);
                }
            }
            
            if (!ScheduledJobManager.this.cancelled) {
                ScheduledJobManager.this.scheduleRun();
            }
        }

        private void doSynchronisation(final SynchronisationRun run,
                final SynchronisationService synchronisationService)
            throws PersistenceException, SQLException, ValidationException {
            synchronisationService.synchroniseTimetablingData();
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
                    
            synchronisationService.generateDiff(run);
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            synchronisationService.updateGroupDescriptions();
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            synchronisationService.mapModulesToCourses();
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            synchronisationService.createGroupsForActivities(run);
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            synchronisationService.mapStudentSetsToUsers();
            if (ScheduledJobManager.this.cancelled) {
                return;
            }
            synchronisationService.applyEnrolmentChanges(run);
            
            run.setEndTime(new Timestamp(System.currentTimeMillis()));
            run.setResult(SynchronisationRun.Result.SUCCESS);
        }
    }
}
