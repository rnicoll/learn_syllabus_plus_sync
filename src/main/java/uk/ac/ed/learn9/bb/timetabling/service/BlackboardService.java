package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import blackboard.base.FormattedText;
import blackboard.data.ValidationException;
import blackboard.data.course.Course;
import blackboard.data.course.CourseCourse;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.Group;
import blackboard.data.course.GroupMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseCourseDbLoader;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.GroupDbPersister;
import blackboard.persist.course.GroupMembershipDbLoader;
import blackboard.persist.course.GroupMembershipDbPersister;
import blackboard.persist.user.UserDbLoader;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;

/**
 * Service for interacting with Blackboard Learn.
 */
@Service
public class BlackboardService {
    private static final Logger log = Logger.getLogger(BlackboardService.class);
    
    @Autowired
    private MailSender mailSender;
    @Autowired
    private VelocityEngine velocityEngine;
    @Autowired
    private SimpleMailMessage templateMessage;
    
    // Optional property for overriding where notification mails are sent
    private String forceAllMailTo = null;

    /**
     * Applies a set of enrolment changes to Learn.
     *
     * @param queryStatement a prepared statement ready to be executed. Results
     * must be ordered by course ID first, then by change part ID.
     * @param stagingDatabase a connection to the staging database.
     * @throws PersistenceException if there was a problem loading or saving
     * data from/to Learn.
     * @throws SQLException if there was a problem with the staging database.
     * @throws ValidationException if a newly generated group enrolment was
     * invalid.
     */
    public void applyEnrolmentChanges(final Connection stagingDatabase)
            throws PersistenceException, SQLException, ValidationException {
        final CourseMembershipDbLoader courseMembershipDbLoader = this.getCourseMembershipDbLoader();
        
        // Stores group memberships that should have been removed, but are
        // unsafe to do so. These are then e-mailed out at the end of the process.
        final UnsafeGroupMembershipManager unsafeGroups
                = new UnsafeGroupMembershipManager(courseMembershipDbLoader, this.getUserDbLoader(),
                this.getVelocityEngine(), this.getMailSender(), this.getTemplateMessage());
        
        if (null != this.getForceAllMailTo()) {
            unsafeGroups.setMailToOverride(this.forceAllMailTo);
        }

        final PreparedStatement queryStatement = stagingDatabase.prepareStatement(
            "SELECT p.part_id, mc.learn_course_id, ag.learn_group_id, s.learn_user_id, c.change_type "
                + "FROM enrolment_change c "
                    + "JOIN enrolment_change_part p ON p.change_id=c.change_id "
                    + "LEFT JOIN change_result r ON r.result_code=p.result_code "
                    + "JOIN module_course mc ON mc.module_course_id=p.module_course_id "
                    + "JOIN activity_group ag on ag.tt_activity_id=c.tt_activity_id AND ag.module_course_id=mc.module_course_id "
                    + "JOIN student_set s ON s.tt_student_set_id=c.tt_student_set_id "
                + "WHERE p.update_completed IS NULL "
                    + "AND (p.result_code IS NULL OR r.retry='1') "
                + "ORDER BY mc.learn_course_id, p.part_id");
        try {
            final ChangeOutcomeUpdateStatement outcome = new ChangeOutcomeUpdateStatement(stagingDatabase);
            
            try {
                applyEnrolmentChanges(queryStatement, outcome, courseMembershipDbLoader, unsafeGroups);
            } finally {
                outcome.close();
            }
        } finally {
            queryStatement.close();
        }
        
        for (MailException mailError: unsafeGroups.emailMemberships()) {
            log.warn("Error sending notification to instructors of problems with removing student(s) from group.",
                    mailError);
        }
    }

    private void applyEnrolmentChanges(final PreparedStatement queryStatement,
            final ChangeOutcomeUpdateStatement outcome, final CourseMembershipDbLoader courseMembershipDbLoader,
            final UnsafeGroupMembershipManager unsafeGroups)
            throws SQLException, ValidationException, PersistenceException, RuntimeException {
        final CourseDbLoader courseDbLoader = this.getCourseDbLoader();
        final GroupDbLoader groupDbLoader = this.getGroupDbLoader();
        final GroupMembershipDbLoader groupMembershipDbLoader = this.getGroupMembershipDbLoader();
        final GroupMembershipDbPersister groupMembershipDbPersister = this.getGroupMembershipDbPersister();
        
        final ResultSet rs = queryStatement.executeQuery();
        try {
            Course currentCourse = null;
            Map<Id, Group> courseGroups = null;

            while (rs.next()) {
                final EnrolmentChange.Type changeType = EnrolmentChange.Type.valueOf(rs.getString("change_type"));
                final int partId = rs.getInt("part_id");
                final Id courseId = this.buildCourseId(rs.getString("learn_course_id"));
                final Id groupId = this.buildGroupId(rs.getString("learn_group_id"));
                final Id userId = this.buildUserId(rs.getString("learn_user_id"));

                if (null == courseId) {
                    outcome.markCourseMissing(partId);
                    continue;
                }

                if (null == groupId) {
                    outcome.markGroupMissing(partId);
                    continue;
                }

                if (null == userId) {
                    outcome.markStudentMissing(partId);
                    continue;
                }

                try {
                    if (null == currentCourse
                            || !currentCourse.getId().equals(courseId)) {
                        // Load student memberships on the current course
                        try {
                            currentCourse = courseDbLoader.loadById(courseId);
                        } catch(KeyNotFoundException e) {
                            // XXX: Wipe the stored association?
                            outcome.markCourseMissing(partId);
                            continue;
                        }
                        courseGroups = getCourseGroups(groupDbLoader, courseId);
                    }

                    final Group group = courseGroups.get(groupId);

                    if (null == group) {
                        // Shouldn't happen as we've just re-created these, but
                        // handle anyway.
                        outcome.markGroupMissing(partId);
                        continue;
                    }

                    // Load the group membership if possible.
                    GroupMembership groupMembership;
                    try {
                        groupMembership = groupMembershipDbLoader.loadByGroupAndUserId(groupId, userId);
                    } catch(KeyNotFoundException e) {
                        groupMembership = null;
                    }
                    switch (changeType) {
                        case ADD:
                            // final CourseMembership courseMembership = studentCourseMemberships.get(studentId);
                            final CourseMembership courseMembership;

                            try {
                                courseMembership = courseMembershipDbLoader.loadByCourseAndUserId(courseId, userId);
                            } catch (KeyNotFoundException e) {
                                // Student is not on this course - probably a delay
                                // bringing in data from Learn, but we can ignore
                                outcome.markNotOnCourse(partId);
                                continue;
                            }

                            if (null == groupMembership) {
                                groupMembershipDbPersister.persist(buildGroupMembership(courseMembership, groupId));
                                outcome.markSuccess(partId);
                            } else {
                                outcome.markAlreadyInGroup(partId);
                            }
                            break;
                        case REMOVE:
                            if (null != groupMembership) {
                                if (this.isGroupMembershipRemovalUnsafe(group, groupMembership)) {
                                    unsafeGroups.addMembership(currentCourse, groupMembership);
                                    outcome.markRemoveUnsafe(partId);
                                } else {
                                    groupMembershipDbPersister.deleteById(groupMembership.getId());
                                    outcome.markSuccess(partId);
                                }
                            } else {
                                outcome.markAlreadyRemoved(partId);
                            }
                            break;
                        default:
                            throw new RuntimeException("Unexpected change type \""
                                    + changeType + "\".");
                    }
                } catch(PersistenceException e) {
                    log.warn("Error while writing group membership changes back into Learn.", e);
                }
            }
        } finally {
            rs.close();
        }
    }
    
    /**
     * Get the set of valid groups within a course.
     * 
     * @param groupDbLoader the group loader to use.
     * @param courseId the ID of the course to retrieve groups for.
     * @return a mapping from group ID to group.
     * @throws KeyNotFoundExeption if no matching course could be found.
     * @throws PersistenceException if there was a problem loading group details.
     */
    private Map<Id, Group> getCourseGroups(final GroupDbLoader groupDbLoader, final Id courseId)
        throws KeyNotFoundException, PersistenceException {
        final Map<Id, Group> groups = new HashMap<Id, Group>();
        
        for (Group group: groupDbLoader.loadByCourseId(courseId)) {
            groups.put(group.getId(), group);
        }
        
        return groups;
    }

    /**
     * Resolves modules from the timetabling system into the relevant course in
     * Learn, where applicable.
     *
     * @param stagingDatabase a connection to the staging database.
     * @throws KeyNotFoundException if there was an inconsistency in the Learn
     * database.
     * @throws PersistenceException if there was a problem loading a course from
     * the Learn database.
     * @throws SQLException if there was a problem accessing the cache database.
     */
    public void mapModulesToCourses(final Connection stagingDatabase)
            throws PersistenceException, SQLException {
        final PreparedStatement updateStatement = stagingDatabase.prepareStatement(
            "UPDATE module_course "
                + "SET learn_course_id=?, "
                    + "learn_course_available=? "
                + "WHERE module_course_id=?");
        try {
            final PreparedStatement queryStatement = stagingDatabase.prepareStatement(
                "SELECT module_course_id, learn_course_code "
                    + "FROM module_course "
                    + "WHERE learn_course_id IS NULL");
            try {
                final CourseDbLoader courseDbLoader = this.getCourseDbLoader();
                final CourseCourseDbLoader courseCourseDbLoader = this.getCourseCourseDbLoader();
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        final String learnCourseCode = rs.getString("learn_course_code");

                        try {
                            if (!courseDbLoader.doesCourseIdExist(learnCourseCode)) {
                                continue;
                            }

                            Course course = courseDbLoader.loadByCourseId(learnCourseCode);

                            // If the course has a parent-child relationship with another
                            // course, use the parent
                            try {
                                final CourseCourse courseCourse = courseCourseDbLoader.loadParent(course.getId());

                                // Successfully found a parent course, replace the child with it.
                                course = courseDbLoader.loadById(courseCourse.getParentCourseId());
                            } catch (KeyNotFoundException e) {
                                // No parent course, ignore
                            }

                            int paramIdx = 1;
                            updateStatement.setString(paramIdx++, course.getId().getExternalString());
                            updateStatement.setString(paramIdx++, course.getIsAvailable()
                                ? "Y"
                                : "N");
                            updateStatement.setInt(paramIdx++, rs.getInt("module_course_id"));
                            updateStatement.executeUpdate();
                        } catch(PersistenceException e) {
                            log.warn("Error while looking up course \""
                                + learnCourseCode + "\" in Learn.", e);
                        }
                    }
                } finally {
                    rs.close();
                }
            } finally {
                queryStatement.close();
            }
        } finally {
            updateStatement.close();
        }
    }

    /**
     * Identifies students sets with group enrolments to be copied to Learn, and
     * maps them to their IDs in Learn.
     *
     * @param stagingDatabase a connection to the staging database.
     *
     * @throws PersistenceException if there was a problem loading or saving
     * data in Learn.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void mapStudentSetsToUsers(final Connection stagingDatabase)
            throws PersistenceException, SQLException {
        final UserDbLoader userDbLoader = getUserDbLoader();

        final PreparedStatement updateStatement = stagingDatabase.prepareStatement("UPDATE student_set "
                + "SET learn_user_id=? "
                + "WHERE tt_student_set_id=?");
        try {
            final PreparedStatement queryStatement = stagingDatabase.prepareStatement(
                    "SELECT s.tt_student_set_id, s.username, s.learn_user_id "
                    + "FROM sync_student_set_vw s "
                    + "WHERE s.learn_user_id IS NULL");
            try {
                final ResultSet rs = queryStatement.executeQuery();
                try {
                    while (rs.next()) {
                        final String username = rs.getString("username");
                        final User user;

                        try {
                            user = userDbLoader.loadByUserName(username);
                        } catch (KeyNotFoundException e) {
                            // User isn't in Learn, ignore
                            continue;
                        } catch (PersistenceException e) {
                            log.warn("Error while attempting to look up user \""
                                + username + "\" in Learn.", e);
                            continue;
                        }

                        updateStatement.setString(1, user.getId().getExternalString());
                        updateStatement.setString(2, rs.getString("tt_student_set_id"));
                        updateStatement.executeUpdate();
                    }
                } finally {
                    rs.close();
                }
            } finally {
                queryStatement.close();
            }
        } finally {
            updateStatement.close();
        }
    }

    /**
     * Constructs a new course group and returns it.
     *
     * @param course the course that the group will belong to.
     * @param groupName the name of the group.
     * @return the new group.
     */
    public Group buildCourseGroup(final Course course, final String groupName,
            final FormattedText description) {
        assert null != course;
        assert null != groupName;
        assert null != description;
        
        // Create the new group
        final Group group = new Group();
        group.setCourseId(course.getId());
        group.setTitle(groupName);
        group.setIsAvailable(false);
        group.setSelfEnrolledAllowed(false);
        group.setDescription(description);
        return group;
    }

    /**
     * Constructs a user's membership on a group and returns it.
     *
     * @param courseMembership the membership of the student onto the course the
     * group belongs to.
     * @param groupId the ID of the group to enrol the student onto.
     * @return the group membership.
     */
    public GroupMembership buildGroupMembership(final CourseMembership courseMembership,
            final Id groupId) {
        assert null != courseMembership;
        assert null != groupId;
        
        final GroupMembership groupMembership = new GroupMembership();

        groupMembership.setCourseMembershipId(courseMembership.getId());
        groupMembership.setGroupId(groupId);
        // groupMembership.setGroupRoleIdentifier(CourseMembership.Role.STUDENT.getIdentifier());

        return groupMembership;
    }

    /**
     * Get a mapping from user IDs to course memberships for students on a
     * single course in Learn.
     *
     * @param courseMembershipLoader the course membership loader to use.
     * @param courseId the course to load course memberships from.
     * @return a mapping from user IDs to course memberships for students on a
     * single course in Learn.
     * @throws PersistenceException if there was a problem loading the course
     * memberships.
     */
    private Map<Id, CourseMembership> getStudentCourseMemberships(final CourseMembershipDbLoader courseMembershipLoader, final Id courseId)
            throws PersistenceException {
        Map<Id, CourseMembership> studentCourseMemberships = new HashMap<Id, CourseMembership>();

        for (CourseMembership membership : courseMembershipLoader.loadByCourseIdAndRole(courseId, CourseMembership.Role.STUDENT)) {
            studentCourseMemberships.put(membership.getUserId(), membership);
        }

        return studentCourseMemberships;
    }

    /**
     * Determines whether it is safe to remove a group membership (as in, doing
     * so will not cause data loss from forums or similar).
     * 
     * @param group the group that membership is being removed from.
     * @param groupMembership the group membership to be removed.
     * @return true if the removal is unsafe, false if it is safe.
     * @throws PersistenceException if there was a problem determining whether
     * the change is safe.
     */
    protected boolean isGroupMembershipRemovalUnsafe(final Group group, final GroupMembership groupMembership)
        throws PersistenceException {
        return group.getIsAvailable()
                || group.hasGroupToolWithGradeableItem()
                || group.getIsDiscussionBoardAvailable();
    }

    /**
     * Loads a group from the Blackboard database, changes its description then
     * writes it back out again.
     *
     * @param groupId the Blackboard ID for the group.
     * @param description a plain-text description for the group.
     * @throws KeyNotFoundException if the group could not be found.
     * @throws PersistenceException if there was a problem loading/saving the
     * group.
     * @throws ValidationException if the change was invalid.
     */
    public void updateGroupDescription(final Id groupId, final String description)
            throws KeyNotFoundException, PersistenceException, ValidationException {
        final GroupDbLoader groupLoader = this.getGroupDbLoader();
        final GroupDbPersister groupPersister = this.getGroupDbPersister();
        final Group group = groupLoader.loadById(groupId);

        group.setDescription(new FormattedText(description, FormattedText.Type.PLAIN_TEXT));

        groupPersister.persist(group);
    }

    /**
     * Gets an instance of a {@link CourseMembershipDbLoader}. This method
     * exists to allow overriding for test-cases.
     *
     * @return a course membership loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected CourseMembershipDbLoader getCourseMembershipDbLoader() throws PersistenceException {
        return CourseMembershipDbLoader.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link CourseDbLoader}. This method exists to allow
     * overriding for test-cases.
     *
     * @return a course loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected CourseDbLoader getCourseDbLoader() throws PersistenceException {
        return CourseDbLoader.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link CourseCourseDbLoader}. This method exists to allow
     * overriding for test-cases.
     *
     * @return a course-course relationship loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected CourseCourseDbLoader getCourseCourseDbLoader() throws PersistenceException {
        return CourseCourseDbLoader.Default.getInstance();
    }

    /**
     * Gets a group membership loader. This method exists so subclasses can
     * override the process for generating group membership loaders, for use in
     * automated testing.
     *
     * @return a group membership loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected GroupMembershipDbLoader getGroupMembershipDbLoader() throws PersistenceException {
        return GroupMembershipDbLoader.Default.getInstance();
    }

    /**
     * Gets a group membership persister. This method exists so subclasses can
     * override the process for generating group membership persisters, for use in
     * automated testing.
     *
     * @return a group membership persister.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected GroupMembershipDbPersister getGroupMembershipDbPersister() throws PersistenceException {
        return GroupMembershipDbPersister.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link GroupDbLoader}. This method exists to allow
     * overriding for test-cases.
     *
     * @return a group loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected GroupDbLoader getGroupDbLoader() throws PersistenceException {
        return GroupDbLoader.Default.getInstance();
    }

    protected GroupDbPersister getGroupDbPersister() throws PersistenceException {
        return GroupDbPersister.Default.getInstance();
    }

    /**
     * Gets an instance of a {@link UserDbLoader}. This method exists to allow
     * overriding for test-cases.
     *
     * @return a user loader.
     * @throws PersistenceException if there was a problem constructing the
     * loader.
     */
    protected UserDbLoader getUserDbLoader() throws PersistenceException {
        return UserDbLoader.Default.getInstance();
    }

    /**
     * @return the forceAllMailTo
     */
    public String getForceAllMailTo() {
        return forceAllMailTo;
    }

    /**
     * @param forceAllMailTo the forceAllMailTo to set
     */
    public void setForceAllMailTo(String forceAllMailTo) {
        this.forceAllMailTo = forceAllMailTo;
    }

    /**
     * @return the mailSender
     */
    public MailSender getMailSender() {
        return mailSender;
    }

    /**
     * @return the velocityEngine
     */
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    /**
     * @return the templateMessage
     */
    public SimpleMailMessage getTemplateMessage() {
        return templateMessage;
    }

    /**
     * @param mailSender the mailSender to set
     */
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * @param velocityEngine the velocityEngine to set
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /**
     * @param templateMessage the templateMessage to set
     */
    public void setTemplateMessage(SimpleMailMessage templateMessage) {
        this.templateMessage = templateMessage;
    }

    /**
     * Constructs a Learn course ID based on the given string. This is provided as
     * a method so that it can be overriden for unit testing.
     * 
     * @param id the string to convert to a course ID.
     * @return a course ID. Returns null if input is null.
     */
    public Id buildCourseId(final String id) throws PersistenceException {
        if (null == id) {
            return null;
        }
        
        return Id.generateId(Course.DATA_TYPE, id);
    }

    /**
     * Constructs a Learn group ID based on the given string. This is provided as
     * a method so that it can be overriden for unit testing.
     * 
     * @param id the string to convert to a group ID.
     * @return a group ID. Returns null if input is null.
     */
    public Id buildGroupId(final String id) throws PersistenceException {
        if (null == id) {
            return null;
        }
        
        return Id.generateId(Group.DATA_TYPE, id);
    }

    /**
     * Constructs a Learn user ID based on the given string. This is provided as
     * a method so that it can be overriden for unit testing.
     * 
     * @param id the string to convert to a user ID.
     * @return a user ID. Returns null if input is null.
     */
    public Id buildUserId(final String id) throws PersistenceException {
        if (null == id) {
            return null;
        }
        
        return Id.generateId(User.DATA_TYPE, id);
    }
}
