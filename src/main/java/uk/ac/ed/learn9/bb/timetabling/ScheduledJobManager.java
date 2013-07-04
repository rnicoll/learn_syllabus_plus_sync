package uk.ac.ed.learn9.bb.timetabling;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import blackboard.data.ValidationException;
import blackboard.persist.PersistenceException;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationRunService;
import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationService;

/**
 * Application life-cycle listener used to start the scheduled synchronisation
 * tasks when the application context is created.
 */
public class ScheduledJobManager extends Object implements ApplicationListener<ApplicationContextEvent> {
    /**
     * The hour of the day (24-hour clock, counting from 0) that the synchronisation
     * is scheduled for.
     */    
    public static final int START_HOUR_OF_DAY = 6;
    /**
     * The minute offset within the hour, that the synchronisation
     * is scheduled for.
     */
    public static final int START_MINUTE = 0;
    /**
     * The length of time to add randomly to the scheduled time, to help reduce
     * risk of race conditions where multiple servers can run the synchronisation
     * tasks.
     */
    public static final int MAX_FUZZ_MILLIS = 60 * 1000;
    
    /**
     * The length of time to wait after cancelling the timer, to give it time
     * to clean up its thread.
     */
    public static final long DELAY_WAIT_TIMER_EXIT = 100L;
    
    /**
     * Interval between running the job, in milliseconds.
     */
    public static long INTERVAL_IN_MILLIS = 24 * 60 * 60 * 1000L;
    
    private ApplicationContext applicationContext;
    
    private AtomicReference<Timer> timerRef = new AtomicReference<Timer>();
    private Logger log = Logger.getLogger(ScheduledJobManager.class);
            
    /**
     * Default constructor.
     */
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
        if (e instanceof ContextRefreshedEvent) {
            this.applicationContext = e.getApplicationContext();
            this.startTimer();
        } else if (e instanceof ContextClosedEvent) {
            this.cancel();
            this.applicationContext = null;
        }
    }

    /**
     * Cancels any scheduled synchronisation. This is handled
     * atomically, so only one timer can be running at a time.
     * 
     * @return true if a timer was running and has now been cancelled,
     * false otherwise.
     */
    public boolean cancel() {
        final Timer existingTimer = this.timerRef.getAndSet(null);
        if (null == existingTimer) {
            return false;
        }
        
        existingTimer.cancel();
        try {
            Thread.sleep(DELAY_WAIT_TIMER_EXIT);
        } catch (InterruptedException ex) {
            log.warn("Interrupted while waiting for Timer thread to exit.");
        }
        
        return true;
    }

    /**
     * Generates the time at which to start the next synchronisation run.
     * 
     * @param baseTimeMillis the time after which the run should be scheduled
     * for.
     * @return the time of the next synchronisation run.
     */
    private static Calendar getNextSynchronisationStartTime(final long baseTimeMillis) {
        final Calendar calendar = Calendar.getInstance();
        // Strip seconds & milliseconds
        final long thisMinuteMillis = baseTimeMillis - (baseTimeMillis % (60 * 1000L));
        calendar.setTimeInMillis(thisMinuteMillis);
        // Set the time of day
        calendar.set(Calendar.MINUTE, START_MINUTE);
        calendar.set(Calendar.HOUR_OF_DAY, START_HOUR_OF_DAY);
        calendar.add(Calendar.DATE, 1);
        return calendar;
    }
    

    /**
     * Starts the timer running on the synchronisation task. This is handled
     * atomically, so only one timer can be running at a time.
     */
    private void startTimer() {
        final Timer timer = new Timer("Timetabling Group Sync", true);
        
        if (this.timerRef.compareAndSet(null, timer)) {
            // We apply a small fuzz value to the delay to help avoid risk of race
            // conditions if two jobs start simultaneously.
            final int fuzz = (int)Math.round(Math.random() * MAX_FUZZ_MILLIS);
            final Calendar startTime = getNextSynchronisationStartTime(System.currentTimeMillis());
            
            // Add a fuzz delay to the start time to avoid possible issues with
            // multiple threads being scheduled at the same time.
            startTime.add(Calendar.MILLISECOND, fuzz);

            timer.scheduleAtFixedRate(new Task(), startTime.getTime(), INTERVAL_IN_MILLIS);
        } else {
            // Timer is already running, so we don't need the timer we just
            // created.
            timer.cancel();
        }
    }

    /**
     * Gets the concurrency service for the scheduled tasks to use to ensure
     * only one task is running at  time.
     * 
     * @return the concurrency service.
     */
    public SynchronisationRunService getConcurrencyService() {
        return this.applicationContext.getBean(SynchronisationRunService.class);
    }

    /**
     * Gets the synchronisation service for this task.
     * 
     * @return the synchronisation service. 
     */
    public SynchronisationService getSynchronisationService() {
        return this.applicationContext.getBean(SynchronisationService.class);
    }
    
    /**
     * Synchronisation task as a timer-compatible class.
     */
    public class Task extends TimerTask {
        private final Logger log = Logger.getLogger(ScheduledJobManager.Task.class);
        
        /**
         * Default constructor.
         */
        public Task() {
        }
    
        /**
         * Runs the synchronisation task, then schedules in the next run.
         */
        @Override
        public void run() {
            log.info("Running Learn/Timetabling synchronisation.");
            
            try {
                final SynchronisationRunService concurrencyService = ScheduledJobManager.this.getConcurrencyService();
                
                if (null == concurrencyService) {
                    throw new IllegalStateException("Concurrency service has not been wired in.");
                }
            
                final SynchronisationRun run = concurrencyService.startNewRun();
                final SynchronisationService synchronisationService = ScheduledJobManager.this.getSynchronisationService();
                
                try {
                    synchronisationService.runSynchronisation(run);
                } catch(PersistenceException e) {
                    concurrencyService.handleErrorOutcome(run, e);
                    log.error("Error while persisting/loading entities in Learn.", e);
                } catch(RuntimeException e) {
                    concurrencyService.handleErrorOutcome(run, e);
                    log.error("Error while persisting/loading entities in Learn.", e);
                } catch(SQLException e) {
                    concurrencyService.handleErrorOutcome(run, e);
                    log.error("Database error while synchronising groups from Timetabling.", e);
                } catch(ValidationException e) {
                    concurrencyService.handleErrorOutcome(run, e);
                    log.error("Error validating entities to be persisted in Learn.", e);
                }
            }  catch (SynchronisationRunService.SynchronisationAlreadyInProgressException ex) {
                // This is expected under normal circumstances, due to more than one
                // possible server trying to run the job. Ignore.
            } catch(SQLException e) {
                log.error("Database error while starting new synchronisation run.", e);
            }
        }
    }
}
