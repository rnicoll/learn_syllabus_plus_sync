package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import blackboard.data.ValidationException;
import blackboard.data.course.Group;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.platform.log.LogService;
import blackboard.platform.log.LogServiceFactory;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

/**
 * Service for synchronising activities and enrolments from Timetabling
 * to groups in Learn. This mostly wraps around other services to provide
 * a higher level API.
 */
@Service
public class SynchronisationService extends Object {
    /**
     * The string to prefix all generated group names with, to identify them
     * as imported from Timetabling.
     */
    public static final String GROUP_NAME_PREFIX = "TT_";
    
    @Autowired
    private DataSource stagingDataSource;
    @Autowired
    private DataSource rdbDataSource;
    @Autowired
    private DataSource eugexDataSource;
    @Autowired
    private BlackboardService blackboardService;
    @Autowired
    private EugexService eugexService;
    @Autowired
    private MergedCoursesService mergedCoursesService;
    @Autowired
    private TimetablingCloneService cloneService;
    
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
        final Connection connection = this.getStagingDataSource().getConnection();
        
        try {
            this.getBlackboardService().applyPreviouslyFailedEnrolmentChanges(connection);
            this.getBlackboardService().applyEnrolmentChanges(connection, run);
        } finally {
            connection.close();
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
     */
    public void generateDiff(final SynchronisationRun run)
            throws SQLException {
        final Connection source = this.getRdbDataSource().getConnection();

        try {
            final Connection destination = this.getStagingDataSource().getConnection();

            try {                
                this.copyStudentSetActivities(run, source, destination);
                this.doGenerateDiff(run, destination);
            } finally {
                destination.close();
            }
        } finally {
            source.close();
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
     * Creates groups in Learn to match activities which have student enrolments
     * to be synchronised.
     * 
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing one of the databases.
     * @throws ValidationException if a newly generated group fails validation
     * by Learn prior to persistence.
     */
    public void createGroupsForActivities()
            throws PersistenceException, SQLException, ValidationException {
        final Connection destination = this.getStagingDataSource().getConnection();
        try {
            generateGroupNames(destination);
            this.getBlackboardService().generateGroupsForActivities(destination);
        } finally {
            destination.close();
        }
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
        final LogService logService = LogServiceFactory.getInstance();
        
        final Map<String, String> activityGroupNames = new HashMap<String, String>();
        // Find groups that need their names completed.
        final PreparedStatement queryStatement = stagingDatabase.prepareStatement(
                "SELECT a.tt_activity_id, a.tt_activity_name, a.learn_group_id, a.learn_group_name, m.tt_module_name, t.tt_type_name "
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
                        logService.logWarning("Could not create group name for activity "
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
     * Resolves the modules that activities belong to, to the courses they
     * represent in Learn, where applicable. This also includes importing
     * details of joint taught activities (so that the child activities can be
     * mapped to the correct module and then onwards to the correct course).
     * 
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing one of the databases.
     */
    public void mapModulesToCourses()
            throws PersistenceException, SQLException {
        final Connection destination = this.getStagingDataSource().getConnection();

        try {
            final Connection source = this.getRdbDataSource().getConnection();

            try {
                // this.getMergedCoursesService().resolveMergedCourses(destination);
            } finally {
                source.close();
            }
        } finally {
            destination.close();
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
        final Connection source = this.getRdbDataSource().getConnection();

        try {
            final Connection destination = this.getStagingDataSource().getConnection();

            try {
                this.cloneService.cloneModules(source, destination);
                this.cloneService.cloneActivityTypes(source, destination);
                this.cloneService.cloneActivityTemplates(source, destination);
                this.cloneService.cloneActivities(source, destination);
                this.cloneService.cloneActivityParents(source, destination);
                this.cloneService.cloneVariantJointTaughtActivities(source, destination);
                this.cloneService.cloneStudentSets(source, destination);
            } finally {
                destination.close();
            }
        } finally {
            source.close();
        }
    }

    /**
     * Copies student set/activity relationships to be synchronised to Learn,
     * from the reporting database. This filters out variant activities as
     * well as whole-course student sets, but does not provide filtering on
     * courses to be mapped to Learn (from SITS).
     */
    private void copyStudentSetActivities(final SynchronisationRun run,
        final Connection source, final Connection destination)
        throws SQLException {
        // Check the condition on this, I haven't had an opportunity to check
        // it with real data.
        final PreparedStatement sourceStatement = source.prepareStatement(
            "SELECT DISTINCT A.ID ACTIVITY_ID, S.ID STUDENT_SET_ID "
            + "FROM ACTIVITY A "
                + "JOIN ACTIVITIES_STUDENTSET REL ON REL.ID=A.ID "
                + "JOIN STUDENT_SET S ON REL.STUDENT_SET=S.ID "
                + "LEFT JOIN VARIANTJTAACTS V ON V.ID=A.ID "
            + "WHERE (V.ISVARIANTCHILD IS NULL OR V.ISVARIANTCHILD='0')"  // BRD requirement #1.3
        );
        try {
            final PreparedStatement destinationStatement = destination.prepareStatement("INSERT INTO cache_enrolment "
                + "(run_id, tt_student_set_id, tt_activity_id) "
                + "VALUES (?, ?, ?)");
            try {
                
                final ResultSet rs = sourceStatement.executeQuery();
                try {
                    while (rs.next()) {
                        destinationStatement.setInt(1, run.getRunId());
                        destinationStatement.setString(2, rs.getString("STUDENT_SET_ID"));
                        destinationStatement.setString(3, rs.getString("ACTIVITY_ID"));
                        destinationStatement.executeUpdate();
                    }
                } finally {
                    rs.close();
                }
            } finally {
                destinationStatement.close();
            }
        } finally {
            sourceStatement.close();
        }
    }

    /**
     * Does the actual production of differences between the last time the process
     * ran, and this time. This process handles excluding whole-class activities.
     * 
     * @param run the synchronisation run to attribute changes to.
     * @param stagingDatabase a connection to the staging database.
     * @return the number of changes entered into the database.
     * 
     * @throws SQLException if there was a problem accessing the database.
     */
    private int doGenerateDiff(final SynchronisationRun run, final Connection stagingDatabase)
        throws SQLException {
        // Two views exist in the database to determine added and removed enrolments,
        // we only have to insert a copy into the relevant table so we can track
        // outcomes.
        
        final PreparedStatement insertStatement = stagingDatabase.prepareStatement(
            "INSERT INTO enrolment_change "
                + "(run_id, change_type, tt_student_set_id, tt_activity_id) "
                + "(SELECT a.run_id, a.change_type, a.tt_student_set_id, a.tt_activity_id "
                    + "FROM added_enrolment_vw a WHERE a.run_id=?) "
                + "UNION (SELECT r.run_id, r.tt_student_set_id, r.tt_activity_id, r.change_type "
		+ "FROM removed_enrolment_vw r  WHERE r.run_id=?)"
        );
        try {
            insertStatement.setInt(1, run.getRunId());
            insertStatement.setInt(2, run.getRunId());
            return insertStatement.executeUpdate();
        } finally {
            insertStatement.close();
        }
    }

    /**
     * Identifies students sets with group enrolments to be copied to Learn,
     * and maps them to their IDs in Learn.
     * 
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing the database.
     */
    public void mapStudentSetsToUsers()
        throws PersistenceException, SQLException {
        final Connection connection = this.getStagingDataSource().getConnection();
        try {
            this.getBlackboardService().mapStudentSetsToUsers(connection);
        } finally {
            connection.close();
        }
    }

    /**
     * Copies data from EUGEX. In this case, this is just the "WEBCT_ACTIVE"
     * field, but if there was more data to be imported later it would go
     * here.
     * 
     * @throws SQLException if there was a problem accessing the databases.
     */
    public void synchroniseEugexData() throws SQLException {        
        this.getEugexService().synchroniseVleActiveCourses();
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
        final PreparedStatement updateStatement = stagingDatabase.prepareStatement(
                "UPDATE activity SET description=? WHERE tt_activity_id=?"
        );
        try {
            final PreparedStatement selectStatement = stagingDatabase.prepareStatement(
                "SELECT a.tt_activity_id, a.tt_activity_name, a.learn_group_id, t.tt_type_name, a.description, a.set_size "
                    + "FROM sync_activity_vw a "
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
     * Gets the timetabling data cloning service.
     * 
     * @return the timetabling data cloning service.
     */
    public TimetablingCloneService getCloneService() {
        return cloneService;
    }

    /**
     * Gets the data source for the EUGEX database.
     * 
     * @return the data source for the EUGEX database.
     */
    public DataSource getEugexDataSource() {
        return eugexDataSource;
    }

    /**
     * Gets the EUGEX service.
     * 
     * @return the EUGEX service.
     */
    public EugexService getEugexService() {
        return eugexService;
    }

    /**
     * @return the mergedCoursesService
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
        return rdbDataSource;
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
    public void setCloneService(TimetablingCloneService cloneService) {
        this.cloneService = cloneService;
    }

    /**
     * Sets the EUGEX database data source.
     * 
     * @param eugexDataSource the EUGEX database data source to set.
     */
    public void setEugexDataSource(DataSource eugexDataSource) {
        this.eugexDataSource = eugexDataSource;
    }

    /**
     * @param eugexService the eugexService to set
     */
    public void setEugexService(EugexService eugexService) {
        this.eugexService = eugexService;
    }

    /**
     * Sets the reporting database data source.
     * 
     * @param rdbDataSource the reporting database data source to set.
     */
    public void setRdbDataSource(DataSource rdbDataSource) {
        this.rdbDataSource = rdbDataSource;
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
}
