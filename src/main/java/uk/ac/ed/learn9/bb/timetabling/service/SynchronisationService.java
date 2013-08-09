package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import blackboard.data.ValidationException;
import blackboard.data.course.Group;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import uk.ac.ed.learn9.bb.timetabling.dao.ConfigurationDao;
import uk.ac.ed.learn9.bb.timetabling.data.Configuration;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationResult;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

/**
 * Service for synchronising activities and enrolments from Timetabling
 * to groups in Learn. This mostly wraps around other services to provide
 * a higher level API.
 * 
 * {@link #runSynchronisation(uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun)}
 * is an excellent place to start with using this service. The synchronisation
 * run to pass to it can be generated by {@link ConcurrencyService}.
 */
@Service
public class SynchronisationService extends Object {
    /**
     * The string to prefix all generated group names with, to identify them
     * as imported from Timetabling.
     */
    public static final String GROUP_NAME_PREFIX = "TT_";
    /**
     * Number of days to keep cached details of enrolments on activities,
     * after a difference has been generated.
     */
    public static final int DAYS_KEEP_ENROLMENT_CACHE = 3;
    
    private Logger log = Logger.getLogger(SynchronisationService.class);
    
    @Autowired
    private ConfigurationDao configurationDao;
    
    @Autowired
    private DataSource stagingDataSource;
    
    @Autowired
    private BlackboardService blackboardService;
    @Autowired
    private SynchronisationRunService concurrencyService;
    @Autowired
    private EugexSynchroniseService eugexSynchroniseService;
    @Autowired
    private MergedCoursesService mergedCoursesService;
    @Autowired
    private TimetablingSynchroniseService timetablingSynchroniseService;
    
    /**
     * Apply student/course group enrolment changes in the staging database,
     * into Learn. This re-attempts any previously failed changes first
     * (for example to fix problems where a student-enrolment on a course was
     * missing during an earlier attempt), then applies the new changes from
     * the given run.
     * 
     * @param run the synchronisation run to apply changes for. Previously
     * failed changes will also be re-attempted.
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing one of the databases.
     * @throws ValidationException if there was a problem validating data to be
     * written back to Learn.
     */
    public void applyEnrolmentChanges(final SynchronisationRun run)
        throws PersistenceException, SQLException, ValidationException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        
        try {
            this.applyEnrolmentChanges(run, stagingDatabase);
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Apply student/course group enrolment changes in the staging database,
     * into Learn. This re-attempts any previously failed changes first
     * (for example to fix problems where a student-enrolment on a course was
     * missing during an earlier attempt), then applies the new changes from
     * the given run.
     * 
     * @param run the synchronisation run to apply changes for. Previously
     * failed changes will also be re-attempted.
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing one of the databases.
     * @throws ValidationException if there was a problem validating data to be
     * written back to Learn.
     */
    public void applyEnrolmentChanges(final SynchronisationRun run, final Connection stagingDatabase)
        throws PersistenceException, SQLException, ValidationException {
        this.getBlackboardService().applyPreviouslyFailedEnrolmentChanges(stagingDatabase);
        this.getBlackboardService().applyNewEnrolmentChanges(stagingDatabase, run);
    }

    /**
     * Asserts that the given number of remove changes does not exceed the configured
     * maximum.
     * 
     * @param configuration the configuration, from which to take the threshold.
     * @param removeChanges the number of remove changes waiting to be performed.
     * @throws ThresholdException if the threshold is exceeded.
     */
    protected void assertBelowRemoveThreshold(final Configuration configuration,
            final int removeChanges)
            throws ThresholdException {
        if (null != configuration.getRemoveThresholdCount()
            && removeChanges > configuration.getRemoveThresholdCount()) {
            throw new ThresholdException("Threshold for number of removed enrolments (as an absolute number) exceeded.");
        }
    }

    /**
     * Asserts that the given number of remove changes as a percentage of the
     * previous run does not exceed the configured maximum.
     * 
     * @param configuration the configuration, from which to take the threshold.
     * @param removeChanges the number of remove changes waiting to be performed.
     * @throws ThresholdException if the threshold is exceeded.
     */
    protected void assertBelowRemovePercentageThreshold(final SynchronisationRun run, 
            final Connection stagingDatabase, final Configuration configuration,
            final int removeChanges)
            throws SQLException, ThresholdException {
        final Integer changeThresholdFromPercent;

        if (null != configuration.getRemoveThresholdPercent()) {
            final double existingEnrolmentCount
                = this.getEnrolmentCount(stagingDatabase, run.getPreviousRunId());
            double temp = existingEnrolmentCount * configuration.getRemoveThresholdPercent() / 100.0;

            changeThresholdFromPercent = (int)Math.round(temp);
            assert changeThresholdFromPercent >= 0;
        } else {
            changeThresholdFromPercent = null;
        }
        if (null != changeThresholdFromPercent
            && removeChanges > changeThresholdFromPercent) {
            throw new ThresholdException("Threshold for number of removed enrolments (by percentage) exceeded.");
        }
    }
    
    /**
     * Clears out records for old abandoned runs, to stop them from cluttering
     * the database unnecessarily. In this case "old" is defined as occurring
     * over a day ago.
     * 
     * @return the number of records deleted.
     * @throws SQLException if there was a problem communicating with the database.
     */
    public int clearEnrolmentCache()
            throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();

        try {
            final Calendar calendar = Calendar.getInstance();

            calendar.add(Calendar.DATE, -DAYS_KEEP_ENROLMENT_CACHE);

            return clearEnrolmentCache(stagingDatabase, new Timestamp(calendar.getTimeInMillis()));
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Clears out records for old abandoned runs, to stop them from cluttering
     * the database unnecessarily.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param before the earliest run end time to select.
     * @return the number of records deleted.
     * @throws SQLException if there was a problem communicating with the database.
     */
    public int clearEnrolmentCache(final Connection stagingDatabase, final Timestamp before)
            throws SQLException {
        final PreparedStatement statement = stagingDatabase.prepareStatement(
            "DELETE FROM cache_enrolment "
            + "WHERE run_id IN ("
                + "SELECT r.run_id "
                    + "FROM synchronisation_run r "
                        + "JOIN synchronisation_run_prev p ON r.run_id=p.previous_run_id "
                        + "JOIN synchronisation_run pr ON p.run_id=pr.run_id "
                    + "WHERE r.end_time<? AND pr.result_code=?"
            + ")");
        try {
            int paramIdx = 1;
            statement.setTimestamp(paramIdx++, before);
            statement.setString(paramIdx++, SynchronisationResult.SUCCESS.name());
            return statement.executeUpdate();
        } finally {
            statement.close();
        }
    }

    /**
     * Generates an up to date difference set between the last time the
     * synchronisation service ran, and now.
     * 
     * @param run the synchronisation run that we're generating a difference set
     * for.
     * 
     * @throws SQLException if there was a problem accessing one of the databases.
     * @throws ThesholdException if the number of changes exceeds the safety
     * threshold values.
     */
    public void generateDiff(final SynchronisationRun run)
            throws SQLException, ThresholdException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();

        try {        
            this.getTimetablingSynchroniseService().copyStudentSetActivities(run, stagingDatabase);
            
            stagingDatabase.setAutoCommit(false);
            try {
                this.doGenerateDiff(run, stagingDatabase);
                stagingDatabase.commit();
            } finally {
                stagingDatabase.rollback();
                stagingDatabase.setAutoCommit(true);
            }
        } finally {
            stagingDatabase.close();
        }
    }

    /**
     * Builds a description for a group based on the activity name, group type
     * and the number of activities in the set.
     * 
     * @param activityName the name of the activity for which to derive a group
     * description.
     * @param groupType the type of group, for example "Lecture", "Tutorial",
     * "Lab".
     * @param activitiesInSet the number of activities in the set.
     * @return the human readable description for the group.
     */
    public String buildGroupDescription(final String activityName, final String groupType,
            final Integer activitiesInSet) {
        Integer groupOrdinalNumber;
        
        // First see if the activity name ends in a number, preceeded by a '/'
        // character, for example "/3". This can frequently indicate the number
        // in a set, and gives us a more meaningful description.
        
        if (activityName.contains("/")) {
            final String[] nameParts = activityName.split("/");
            final String lastNamePart = nameParts[nameParts.length - 1];
            
            try {
                groupOrdinalNumber = Integer.valueOf(lastNamePart);
            } catch(NumberFormatException e) {
                groupOrdinalNumber = null;
            }
        } else {
            groupOrdinalNumber = null;
        }
        
        final StringBuilder groupDescription = new StringBuilder(groupType);
        
        if (null != groupOrdinalNumber) {
            groupDescription.append(" ")
                .append(Integer.toString(groupOrdinalNumber));
            if (null != activitiesInSet) {
                groupDescription.append(" of ")
                    .append(activitiesInSet.toString());
            }
        } else {
            if (null != activitiesInSet) {
                groupDescription.append(" in a set of ")
                    .append(activitiesInSet.toString());
            }
        }
        
        return groupDescription.toString();
    }

    /**
     * Builds a group name based the name of the activity and module, and the
     * activity type.
     * 
     * @param activityName the name of the activity in timetabling.
     * @param moduleName the name of the module the activity belongs to, in timetabling.
     * @param activityType the name of the type of activity (lecture, tutorial, etc).
     * @return a group name, or null if no name could be generated (for example
     * missing data).
     */
    private String buildGroupName(final String activityName, final String moduleName, final String activityType) {
        if (null == activityName) {
            return null;
        }
                
        // The ID of a group, for example a number to identify it within its
        // set.
        final String groupId;
        
        // In many cases, the activity name will have the module name at the
        // start. In which case, we strip the module name to give us a group
        // name.
        if (null != moduleName
            && activityName.startsWith(moduleName)) {
            final String temp = activityName.substring(moduleName.length()).trim();
            
            // If the remainder of the activity name starts with a "/",
            // it's likely to be something like "/1", and need more context.
            // Otherwise we presume it's safe to use by itself.
            if (!temp.startsWith("/")) {
                return GROUP_NAME_PREFIX + temp;
            } else {
                groupId = temp.substring(1);
            }
        } else {
            // If the activity name has a '/' in, split on the last '/'
            // character.
            if (activityName.lastIndexOf("/") >= 0) {
                groupId = activityName.substring(activityName.lastIndexOf("/") + 1);
            } else if (activityName.lastIndexOf(" ") >= 0) {
                groupId = activityName.substring(activityName.lastIndexOf(" ") + 1);
            } else {
                groupId = activityName;
            }
        }
        
        final StringBuilder groupName = new StringBuilder(GROUP_NAME_PREFIX);
        
        if (null != activityType) {
            groupName.append(activityType.trim())
                .append("_");
        }
        
        groupName.append(groupId.trim());
        
        return groupName.toString();
    }

    /**
     * Generate activity-group relationships in the staging database, based on
     * the courses that each activity maps to via their module.
     * 
     * @throws SQLException if there was a problem accessing one the staging
     * database.
     */
    public void generateActivityGroups()
            throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        
        try {
            final Statement statement = stagingDatabase.createStatement();
            try {
                statement.executeUpdate("INSERT INTO activity_group "
                        + "(tt_activity_id, module_course_id) "
                        + "(SELECT a.tt_activity_id, mc.module_course_id "
                            + "FROM module_course mc "
                                + "JOIN activity a ON mc.tt_module_id=a.tt_module_id "
                            + "WHERE tt_activity_id NOT IN (SELECT tt_activity_id FROM activity_group)"
                        + ")"
                );
            } finally {
                statement.close();
            }
        } finally {
            stagingDatabase.close();
        }
    }

    /**
     * Generate module-course relationships based on the modules from Timetabling,
     * and the merged courses data from the BBL Feeds database. This ensures
     * we have a consistent snapshot of how the mappings are done, so that
     * external database changes won't cause problems later.
     * 
     * @throws SQLException if there was a problem accessing one the staging
     * database.
     */
    public void generateModuleCourses()
            throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        
        try {
            final Statement statement = stagingDatabase.createStatement();
            try {
                statement.executeUpdate("INSERT INTO module_course "
                    + "(tt_module_id, merged_course, learn_course_code) "
                    + "(SELECT m.tt_module_id, 'N', learn_course_code "
                        + "FROM module m "
                        + "WHERE m.learn_course_code IS NOT NULL "
                            + "AND m.tt_module_id NOT IN (SELECT tt_module_id FROM module_course WHERE merged_course='N')"
                    + ")"
                );
                statement.executeUpdate("INSERT INTO module_course "
                    + "(tt_module_id, merged_course, learn_course_code) "
                    + "(SELECT m.tt_module_id, 'Y', merge.learn_target_course_code "
                        + "FROM module m "
                            + "JOIN learn_merged_course merge ON merge.learn_source_course_code = m.learn_course_code "
                            + "LEFT JOIN module_course exist ON exist.tt_module_id=m.tt_module_id "
                                + "AND exist.merged_course = 'Y' "
                                + "AND exist.learn_course_code=m.learn_course_code "
                        + "WHERE exist.tt_module_id IS NULL"
                    + ")"
                );
            } finally {
                statement.close();
            }
        } finally {
            stagingDatabase.close();
        }
    }

    /**
     * Get the number of enrolments from a synchronisation run.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param runId the ID of the run to get a count for. Null is allowed here,
     * in which case 0 is always returned.
     * @return the number of enrolments.
     * @throws SQLException
     */
    private int getEnrolmentCount(final Connection stagingDatabase,
            final Integer runId)
        throws SQLException {
        // Handle the null case (first run)
        if (null == runId) {
            return 0;
        }
        
        final PreparedStatement statement = stagingDatabase.prepareStatement(
            "SELECT COUNT(C.CHANGE_ID) CHANGES FROM ENROLMENT_CHANGE C WHERE C.RUN_ID=?"
        );
        try {
            statement.setInt(1, runId);
            
            final ResultSet rs = statement.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getInt("CHANGES");
                } else {
                    return 0;
                }
            } finally {
                rs.close();
            }
        } finally {
            statement.close();
        }
    }
    
    /**
     * Executes a synchronisation run.
     * 
     * @param run the synchronisation run to be executed. These can be generated
     * by {@link ConcurrencyService#startNewRun()}.
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing one of the databases.
     * @throws ValidationException if there was a problem validating data to be
     * written into Learn.
     */
    public void runSynchronisation(final SynchronisationRun run)
            throws SQLException, PersistenceException, ThresholdException, ValidationException {
        assert null != this.getMergedCoursesService();
        
        this.synchroniseTimetablingData();
        
        this.getEugexSynchroniseService().synchroniseVleActiveCourses();
        this.getMergedCoursesService().synchroniseMergedCourses();
        
        this.getConcurrencyService().markCacheCopyCompleted(run);
        this.generateModuleCourses();
        this.generateActivityGroups();
        this.generateDiff(run);
        this.getConcurrencyService().markDiffCompleted(run);
        
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            final BlackboardService bbService = this.getBlackboardService();
            
            bbService.mapModulesToCourses(stagingDatabase);
            this.updateGroupDescriptions(stagingDatabase);
        
            this.generateGroupNames(stagingDatabase);
            bbService.createGroupsForActivities(stagingDatabase, run);
            bbService.mapStudentSetsToUsers(stagingDatabase);
            this.applyEnrolmentChanges(run, stagingDatabase);
        } finally {
            stagingDatabase.close();
        }

        this.getConcurrencyService().handleSuccessOutcome(run);
        this.getConcurrencyService().clearAbandonedRuns();
        this.clearEnrolmentCache();
    }
    
    /**
     * Generates names of groups, to be used in Learn where needed. These are
     * written into the database so they can be inspected later if needed.
     * 
     * @param stagingDatabase a connection to the cache database.
     * @throws SQLException if there was a problem accessing the database.
     */
    public void generateGroupNames(final Connection stagingDatabase)
            throws SQLException {
        final Map<String, String> activityGroupNames = new HashMap<String, String>();
        // Find groups that need their names completed.
        final PreparedStatement queryStatement = stagingDatabase.prepareStatement(
                "SELECT a.tt_activity_id, a.tt_activity_name, a.learn_group_name, m.tt_module_name, t.tt_type_name "
                    + "FROM sync_activity_vw a "
                        + "JOIN sync_module_vw m ON m.tt_module_id=a.tt_module_id "
                        + "JOIN activity_type t ON t.tt_type_id=a.tt_type_id "
                    + "WHERE a.learn_group_name IS NULL "
                        + "AND m.learn_course_code IS NOT NULL "
        );
        try {
            final ResultSet rs = queryStatement.executeQuery();
            try {
                while (rs.next()) {
                    final String activityId = rs.getString("tt_activity_id");
                    final String activityName = rs.getString("tt_activity_name");
                    final String moduleName = rs.getString("tt_module_name");
                    final String activityType = rs.getString("tt_type_name");
                    final String groupName = buildGroupName(activityName, moduleName, activityType);
                    
                    if (null == groupName) {
                        log.warn("Could not create group name for activity "
                            + activityId + " due to missing data (module name, type, etc.)");
                        continue;
                    }
                    
                    activityGroupNames.put(rs.getString("tt_activity_id"), groupName);
                }
            } finally {
                rs.close();
            }
        } finally {
            queryStatement.close();
        }
        
        // Write out the group names
        final PreparedStatement updateStatement = stagingDatabase.prepareStatement(
                "UPDATE activity SET learn_group_name=? WHERE tt_activity_id=?"
        );
        try {
            for (String activityId: activityGroupNames.keySet()) {
                updateStatement.setString(1, activityGroupNames.get(activityId));
                updateStatement.setString(2, activityId);
                updateStatement.executeUpdate();
            }
        } finally {
            updateStatement.close();
        }
    }
    
    /**
     * Clones data from Timetabling into the staging database. These provide a
     * cached copy of the data to use without resorting to trying to perform
     * in-memory joins across two distinct databases.
     * 
     * @throws SQLException if there was a problem accessing one of the databases.
     */
    public void synchroniseTimetablingData()
            throws SQLException {
        final Connection destination = this.getStagingDataSource().getConnection();

        try {
            this.getTimetablingSynchroniseService().synchroniseTimetablingData(destination);
        } finally {
            destination.close();
        }
    }

    /**
     * Does the actual production of differences between the last time the process
     * ran, and this time. This process handles excluding whole-class activities.
     * 
     * @param run the synchronisation run to attribute changes to.
     * @param stagingDatabase a connection to the staging database.
     * 
     * @throws SQLException if there was a problem accessing the database.
     * @throws ThesholdException if the number of changes exceeds the safety
     * threshold values.
     */
    protected void doGenerateDiff(final SynchronisationRun run, final Connection stagingDatabase)
        throws SQLException, ThresholdException {
        final Configuration configuration = this.getConfigurationDao().getDefault();        
        final int removeChanges = doGenerateDiffRemove(stagingDatabase, run);
        
        log.info("Remove count: "
            + removeChanges);
        assertBelowRemoveThreshold(configuration, removeChanges);
        assertBelowRemovePercentageThreshold(run, stagingDatabase, configuration, removeChanges);
        
        final int addChanges = doGenerateDiffAdd(stagingDatabase, run);
        log.info("Add count: "
            + addChanges);
        doGenerateDiffParts(stagingDatabase, run);
    }

    private int doGenerateDiffAdd(final Connection stagingDatabase, final SynchronisationRun run)
            throws SQLException {
        final PreparedStatement insertStatement = stagingDatabase.prepareStatement(
            "INSERT INTO enrolment_change "
                + "(run_id, change_type, tt_student_set_id, tt_activity_id) "
                + "(SELECT a.run_id, a.change_type, a.tt_student_set_id, a.tt_activity_id "
                    + "FROM added_enrolment_vw a WHERE a.run_id=?)"
        );
        try {
            insertStatement.setInt(1, run.getRunId());
            return insertStatement.executeUpdate();
        } finally {
            insertStatement.close();
        }
    }

    private int doGenerateDiffRemove(final Connection stagingDatabase, final SynchronisationRun run)
        throws SQLException {
        final PreparedStatement insertStatement = stagingDatabase.prepareStatement(
            "INSERT INTO enrolment_change "
                + "(run_id, change_type, tt_student_set_id, tt_activity_id) "
                + "(SELECT r.run_id, r.change_type, r.tt_student_set_id, r.tt_activity_id "
                    + "FROM removed_enrolment_vw r WHERE r.run_id=?)"
        );
        try {
            insertStatement.setInt(1, run.getRunId());
            return insertStatement.executeUpdate();
        } finally {
            insertStatement.close();
        }
    }
    
    /**
     * Updates the descriptions of activities in the database and in Learn.
     * 
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing the staging database.
     * @throws ValidationException if there was a problem validating the updated
     * group.
     */
    public void updateGroupDescriptions() 
            throws SQLException, PersistenceException, ValidationException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            this.updateGroupDescriptions(stagingDatabase);
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Updates the descriptions of activities in the database and in Learn.
     * 
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing the staging database.
     * @throws ValidationException if there was a problem validating the updated
     * group.
     */
    public void updateGroupDescriptions(final Connection stagingDatabase) 
            throws SQLException, PersistenceException, ValidationException {
        final PreparedStatement updateStatement = stagingDatabase.prepareStatement(
                "UPDATE activity SET description=? WHERE tt_activity_id=?"
        );
        try {
            final PreparedStatement selectStatement = stagingDatabase.prepareStatement(
                "SELECT a.tt_activity_id, a.tt_activity_name, ag.learn_group_id, t.tt_type_name, a.description, a.set_size "
                    + "FROM sync_activity_vw a "
                        + "JOIN activity_group ag ON ag.tt_activity_id=a.tt_activity_id "
                        + "JOIN activity_type t ON t.tt_type_id=a.tt_type_id");
            try {
                final ResultSet rs = selectStatement.executeQuery();
                while (rs.next()) {
                    final String description = buildGroupDescription(rs.getString("tt_activity_name"),
                            rs.getString("tt_type_name"), rs.getInt("set_size"));
                    final String previousDescription = rs.getString("description");
                    
                    if (null == previousDescription
                        || !description.equals(previousDescription)) {
                        updateStatement.setString(1, rs.getString("tt_activity_id"));
                        updateStatement.setString(2, description);
                        updateStatement.executeUpdate();
                        
                        final String learnGroupId = rs.getString("learn_group_id");
                        
                        if (null != learnGroupId) {
                            try {
                                this.getBlackboardService().updateGroupDescription(Id.generateId(Group.DATA_TYPE, learnGroupId),
                                    description);
                            } catch(KeyNotFoundException e) {
                                // Remove the ID from the database?
                            }
                        }
                    }
                }
            } finally {
                selectStatement.close();
            }
        } finally {
            updateStatement.close();
        }
    }

    /**
     * Gets the timetabling data synchronisation service.
     * 
     * @return the timetabling data synchronisation service.
     */
    public TimetablingSynchroniseService getTimetablingSynchroniseService() {
        return timetablingSynchroniseService;
    }

    /**
     * @return the configurationDao
     */
    public ConfigurationDao getConfigurationDao() {
        return configurationDao;
    }

    /**
     * Get the concurrency management service.
     * 
     * @return the concurrency management service.
     */
    public SynchronisationRunService getConcurrencyService() {
        return concurrencyService;
    }

    /**
     * Gets the EUGEX data synchronisation service.
     * 
     * @return the EUGEX data synchronisation service.
     */
    public EugexSynchroniseService getEugexSynchroniseService() {
        return eugexSynchroniseService;
    }

    /**
     * Get the merged courses service.
     * 
     * @return the merged courses service.
     */
    public MergedCoursesService getMergedCoursesService() {
        return mergedCoursesService;
    }
    
    /**
     * Returns the reporting database data source.
     *
     * @return the reporting database data source.
     */
    public DataSource getRdbDataSource() {
        return this.getTimetablingSynchroniseService().getRdbDataSource();
    }

    /**
     * Gets the data source for the staging database.
     * 
     * @return the staging database data source.
     */
    public DataSource getStagingDataSource() {
        return stagingDataSource;
    }

    /**
     * @param cloneService the cloneService to set
     */
    public void setTimetablingSynchroniseService(TimetablingSynchroniseService cloneService) {
        this.timetablingSynchroniseService = cloneService;
    }

    /**
     * @param configurationDao the configurationDao to set
     */
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    /**
     * @param concurrencyService the concurrencyService to set
     */
    public void setConcurrencyService(SynchronisationRunService concurrencyService) {
        this.concurrencyService = concurrencyService;
    }

    /**
     * @param eugexService the eugexService to set
     */
    public void setEugexSynchroniseService(final EugexSynchroniseService newEugexSynchroniseService) {
        this.eugexSynchroniseService = newEugexSynchroniseService;
    }

    /**
     * Sets the staging database data source.
     * 
     * @param dataSource the staging database data source to set.
     */
    public void setStagingDataSource(DataSource dataSource) {
        this.stagingDataSource = dataSource;
    }

    /**
     * @param mergedCoursesService the mergedCoursesService to set
     */
    public void setMergedCoursesService(MergedCoursesService mergedCoursesService) {
        this.mergedCoursesService = mergedCoursesService;
    }

    /**
     * @return the blackboardService
     */
    public BlackboardService getBlackboardService() {
        return blackboardService;
    }

    /**
     * @param blackboardService the blackboardService to set
     */
    public void setBlackboardService(BlackboardService blackboardService) {
        this.blackboardService = blackboardService;
    }

    public void doGenerateDiffParts(final Connection stagingDatabase, final SynchronisationRun run) throws SQLException {
        // Generate the individual change parts
        final PreparedStatement insertStatement = stagingDatabase.prepareStatement(
            "INSERT INTO enrolment_change_part "
                + "(change_id, module_course_id) "
                + "(SELECT c.change_id, mc.module_course_id "
                    + "FROM enrolment_change c "
                        + "JOIN activity a ON a.tt_activity_id=c.tt_activity_id "
                        + "JOIN module m ON m.tt_module_id=a.tt_module_id "
                        + "JOIN module_course mc ON mc.tt_module_id=m.tt_module_id AND mc.learn_course_available='Y' "
                    + "WHERE c.run_id=?) "
        );
        try {
            insertStatement.setInt(1, run.getRunId());
            insertStatement.executeUpdate();
        } finally {
            insertStatement.close();
        }
    }
}
