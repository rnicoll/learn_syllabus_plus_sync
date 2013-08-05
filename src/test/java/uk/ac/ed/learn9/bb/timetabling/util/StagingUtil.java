package uk.ac.ed.learn9.bb.timetabling.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import blackboard.persist.Id;

import uk.ac.ed.learn9.bb.timetabling.RdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleCourseDao;
import uk.ac.ed.learn9.bb.timetabling.dao.ModuleDao;
import uk.ac.ed.learn9.bb.timetabling.data.AcademicYearCode;
import uk.ac.ed.learn9.bb.timetabling.data.Activity;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityTemplate;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityType;
import uk.ac.ed.learn9.bb.timetabling.data.Module;
import uk.ac.ed.learn9.bb.timetabling.data.ModuleCourse;

/**
 * Utility class for creating data into the staging database.
 */
public class StagingUtil {

    /**
     * Creates an activity in the test staging database.
     *
     * @param stagingDatabase a connection to the TEST staging database.
     * @param activityTemplate the activity template to base this activity on.
     * @param activityType the activity type.
     * @param module the module the activity will belong to.
     * @param schedulingMethod whether the activity has been scheduled.
     * @param activityOrdinality a number for this activity within the
     * activities under the same template.
     * @param idSource the RDB ID generator to use for ID values.
     * @return the newly generated activity, for reference.
     * @throws SQLException if there was a problem communicating with the
     * reporting database.
     */
    public static Activity createTestActivity(final Connection stagingDatabase,
            final ActivityDao activityDao,
            final ActivityTemplate activityTemplate, final ActivityType activityType,
            final Module module,
            final RdbUtil.SchedulingMethod schedulingMethod, final int activityOrdinality,
            final RdbIdSource idSource)
            throws SQLException {
        final String activityId = idSource.getId();
        final String activityName;
        
        if (null != activityTemplate) {
            activityName = activityTemplate.getTemplateName() + "/"
                + activityOrdinality;
        } else {
            activityName = "Group/" + activityOrdinality;
        }

        final PreparedStatement statement = stagingDatabase.prepareStatement(
                "INSERT INTO ACTIVITY (TT_ACTIVITY_ID, TT_ACTIVITY_NAME, "
                    + "TT_MODULE_ID, TT_TEMPLATE_ID, TT_TYPE_ID, "
                    + "TT_SCHEDULING_METHOD) "
                + "VALUES (?, ?, ?, ?, ?, ?)");
        try {
            int paramIdx = 1;

            statement.setString(paramIdx++, activityId);
            statement.setString(paramIdx++, activityName);
            if (null != module) {
                statement.setString(paramIdx++, module.getModuleId());
            } else {
                statement.setNull(paramIdx++, Types.VARCHAR);
            }
            if (null != activityTemplate) {
                statement.setString(paramIdx++, activityTemplate.getTemplateId());
            } else {
                statement.setNull(paramIdx++, Types.VARCHAR);
            }
            if (null != activityType) {
                statement.setString(paramIdx++, activityType.getTypeId());
            } else {
                statement.setNull(paramIdx++, Types.VARCHAR);
            }
            switch (schedulingMethod) {
                case SCHEDULED:
                    statement.setInt(paramIdx++, 1);
                    break;
                default:
                    statement.setInt(paramIdx++, 0);
                    break;
            }

            statement.executeUpdate();
        } finally {
            statement.close();
        }

        return activityDao.getById(activityId);
    }

    public static Module createTestModule(final Connection stagingDatabase,
            final ModuleDao moduleDao, final String courseCode,
            final String moduleName, final AcademicYearCode academicYear,
            final boolean webCtActive, final RdbIdSource idSource)
        throws SQLException {
        final Module module = new Module();
        
        module.setModuleId(idSource.getId());
        module.setTimetablingModuleName(moduleName);
        module.setTimetablingAcademicYear(academicYear.toString());
        module.setTimetablingCourseCode(courseCode);
        final PreparedStatement statement = stagingDatabase.prepareStatement(
                "INSERT INTO MODULE (TT_MODULE_ID, TT_COURSE_CODE, "
                    + "TT_MODULE_NAME, TT_ACADEMIC_YEAR, WEBCT_ACTIVE) "
                + "VALUES (?, ?, ?, ?, ?)");
        try {
            int paramIdx = 1;

            statement.setString(paramIdx++, module.getModuleId());
            statement.setString(paramIdx++, module.getTimetablingCourseCode());
            statement.setString(paramIdx++, module.getTimetablingModuleName());
            statement.setString(paramIdx++, module.getTimetablingAcademicYear());
            statement.setString(paramIdx++, webCtActive
                    ? "Y"
                    : "N");

            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        return moduleDao.getById(module.getModuleId());
    }

    public static ModuleCourse createModuleCourse(final Connection stagingDatabase,
            final ModuleCourseDao moduleCourseDao, final Module module,
            final Id learnCourseId)
        throws SQLException {
        final ModuleCourse moduleCourse = new ModuleCourse();
        
        moduleCourse.setModule(module);
        moduleCourse.setMergedCourse(false);
        
        final PreparedStatement statement = stagingDatabase.prepareStatement(
                "INSERT INTO MODULE_COURSE (TT_MODULE_ID, "
                    + "MERGED_COURSE, LEARN_COURSE_CODE, LEARN_COURSE_ID) "
                + "VALUES (?, ?, ?, ?)");
        try {
            int paramIdx = 1;

            statement.setString(paramIdx++, module.getModuleId());
            statement.setString(paramIdx++, "N");
            statement.setString(paramIdx++, module.getLearnCourseCode());
            if (null != learnCourseId) {
                statement.setString(paramIdx++, learnCourseId.getExternalString());
            } else {
                statement.setNull(paramIdx++, Types.VARCHAR);
            }
            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        return moduleCourseDao.getByTimetablingId(module.getModuleId()).iterator().next();
    }

    public static void createTestActivityGroup(final Connection stagingDatabase,
            final Activity activity, final ModuleCourse moduleCourse,
            final Id learnGroupId)
        throws SQLException {
        final PreparedStatement statement = stagingDatabase.prepareStatement(
                "INSERT INTO ACTIVITY_GROUP (TT_ACTIVITY_ID, "
                    + "MODULE_COURSE_ID, LEARN_GROUP_ID, LEARN_GROUP_CREATED) "
                + "VALUES (?, ?, ?, ?)");
        try {
            int paramIdx = 1;
            
            statement.setString(paramIdx++, activity.getActivityId());
            statement.setInt(paramIdx++, moduleCourse.getId());
            if (null != learnGroupId) {
                statement.setString(paramIdx++, learnGroupId.getExternalString());
                statement.setTimestamp(paramIdx++, new Timestamp(System.currentTimeMillis()));
            } else {
                statement.setNull(paramIdx++, Types.VARCHAR);
                statement.setNull(paramIdx++, Types.TIMESTAMP);
            }
            statement.executeUpdate();
        } finally {
            statement.close();
        }
    }
}
