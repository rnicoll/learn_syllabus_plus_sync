package uk.ac.ed.learn9.bb.timetabling.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import uk.ac.ed.learn9.bb.timetabling.RdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.data.AcademicYearCode;
import uk.ac.ed.learn9.bb.timetabling.data.Activity;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityTemplate;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityType;
import uk.ac.ed.learn9.bb.timetabling.data.Module;

/**
 * Class for setting up/tearing down test data in the RDB.
 */
public class RdbUtil {
    /**
     * How to mark a test activity as scheduled, in the RDB. Although an actual
     * RDB contains more detail than this, for our purposes we only need to know
     * if it's scheduled (and therefore included in the synchronisation) or not
     * (and therefore not).
     */
    public enum SchedulingMethod {
        NOT_SCHEDULED,
        SCHEDULED;
    }
    
    /**
     * How to flag an activity template in relation to synchronisation to the VLE.
     * {@link TemplateForVle#NOT_SPECIFIED} is currently considered equivalent
     * to {@link TemplateForVle#FOR_VLE}.
     */
    public enum TemplateForVle {
        NOT_SPECIFIED,
        NOT_FOR_VLE,
        FOR_VLE;
    }
    
    /**
     * A random EUCLID course code for generating test activities.
     */
    public static final String TEST_COURSE_CODE = "PGHC11335";
    /**
     * A random EUCLID semester (period) code for generating test activities.
     */
    public static final String TEST_SEMESTER = "SEM1";
    /**
     * A random EUCLID occurrence code for generating test activities.
     */
    public static final String TEST_OCCURRENCE = "SV1";
    
    /**
     * A random timetabling department ID for use when generating test RDB
     * data. As of the time of writing the test RDB does not contain departments,
     * so this merely needs to be present, however if departments are later
     * used in scheduling this would need to be replaced with a real value.
     */
    public static final String DEPARTMENT_ID = "BF20A0ADF91117B06331C6ED3F9FC187";
    
    /**
     * An example week pattern for use when creating test RDB data.
     */
    public static final String WEEK_PATTERN = "11111111111111111111111111111111111111111111111111111111111111111";
    
    public static Activity createTestActivity(final Connection rdb,
        final ActivityTemplate activityTemplate, final Module module,
        final SchedulingMethod schedulingMethod, final int activityId,
        final RdbIdSource idSource)
        throws SQLException {
        final Activity activity = new Activity();
        
        activity.setActivityId(idSource.getId());
        activity.setActivityName(activityTemplate.getTemplateName() + "/"
            + activityId);
        activity.setModule(module);
        
        final PreparedStatement statement = rdb.prepareStatement(
            "INSERT INTO ACTIVITY (ID, NAME, HOST_KEY, DESCRIPTION, MODUL, "
                    + "SCHEDULING_METHOD, DEPARTMENT, ACTIVITY_TYPE, ACTIVITY_TMPL, "
                    + "ZONE, FACTOR, DURATION, LINK_SIZE, PLANNED_SIZE, SUGGESTED_DAYS, "
                    + "SUGGESTED_PERIOD, POOLED_RESOURCES, "
                    + "DAYS_FOR_MINIMUM, MINIMUM_TIME, MINIMUM_DAYS, DAYS_FOR_MAXIMUM, "
                    + "MAXIMUM_TIME, MAXIMUM_DAYS, NAMED_AVAILABILITY, NAMED_USAGE_PREF, NAMED_STARTS_PREF, DICT, "
                    + "WEEK_PATTERN, STARTS_PREFS, USAGE_PREFS, BASE_AVAILABILITY) "
            + "(SELECT ? ID, ? NAME, ? HOST_KEY, ? DESCRIPTION, ? MODUL, "
                    + "? SCHEDULING_METHOD, DEPARTMENT, ACTIVITY_TYPE, ID ACTIVITY_TMPL, "
                    + "ZONE, FACTOR, DURATION, LINK_SIZE, PLANNED_SIZE, SUGGESTED_DAYS, "
                    + "SUGGESTED_PERIOD, POOLED_RESOURCES, "
                    + "DAYS_FOR_MINIMUM, MINIMUM_TIME, MINIMUM_DAYS, DAYS_FOR_MAXIMUM, "
                    + "MAXIMUM_TIME, MAXIMUM_DAYS, NAMED_AVAILABILITY, NAMED_USAGE_PREF, NAMED_STARTS_PREF, DICT, "
                    + "WEEK_PATTERN, STARTS_PREFS, USAGE_PREFS, BASE_AVAILABILITY "
                + "FROM TEMPLATE "
                + "WHERE ID=?)"
        );
        try {
            int paramIdx = 1;
            
            statement.setString(paramIdx++, activity.getActivityId());
            statement.setString(paramIdx++, activity.getActivityName());
            statement.setString(paramIdx++, RdbUtil.generateHostKey(activity.getActivityId()));
            statement.setString(paramIdx++, activity.getActivityName());
            statement.setString(paramIdx++, module.getModuleId());
            switch (schedulingMethod) {
                case SCHEDULED:
                    statement.setInt(paramIdx++, 1);
                    break;
                default:
                    statement.setInt(paramIdx++, 0);
                    break;
            }
            statement.setString(paramIdx++, activityTemplate.getTemplateId());
            
            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        return activity;
    }
    
    public static ActivityType createTestActivityType(final Connection rdb, final String typeName,
            final RdbIdSource idSource)
        throws SQLException {
        final ActivityType activityType = new ActivityType();
        
        activityType.setTypeId(idSource.getId());
        activityType.setTypeName(typeName);
        
        final PreparedStatement statement = rdb.prepareStatement(
            "Insert into ACTIVITYTYPES (ID,NAME,HOST_KEY,DEPARTMENT) "
                + "VALUES (?,?,?,?)");
        try {
            int paramIdx = 1;
            
            statement.setString(paramIdx++, activityType.getTypeId());
            statement.setString(paramIdx++, activityType.getTypeName());
            statement.setString(paramIdx++, RdbUtil.generateHostKey(activityType.getTypeId()));
            statement.setString(paramIdx++, DEPARTMENT_ID);
            
            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        return activityType;
    }
    
    public static ActivityTemplate createTestActivityTemplate(final Connection rdb, final Module module,
            final ActivityType activityType, final String templateName,
            final TemplateForVle forVle, final RdbIdSource idSource)
        throws SQLException {
        final ActivityTemplate template = new ActivityTemplate();
        
        template.setTemplateId(idSource.getId());
        template.setTemplateName(templateName);
        switch (forVle) {
            case NOT_FOR_VLE:
                template.setUserText5("Not for VLE");
                break;
            case FOR_VLE:
            default:
                template.setUserText5(null);
                break;
        }
        
        final PreparedStatement statement = rdb.prepareStatement(
            "Insert into TEMPLATE (ID,NAME,HOST_KEY,DEPARTMENT,ACTIVITY_TYPE,"
                + "MODUL,USER_TEXT_5,WEEK_PATTERN) "
                + "VALUES (?,?,?,?,?,?,?,?)");
        try {
            int paramIdx = 1;
            
            statement.setString(paramIdx++, template.getTemplateId());
            statement.setString(paramIdx++, template.getTemplateName());
            statement.setString(paramIdx++, RdbUtil.generateHostKey(template.getTemplateId()));
            statement.setString(paramIdx++, DEPARTMENT_ID);
            statement.setString(paramIdx++, activityType.getTypeId());
            statement.setString(paramIdx++, module.getModuleId());
            statement.setString(paramIdx++, template.getUserText5());
            statement.setString(paramIdx++, WEEK_PATTERN);
            
            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        return template;
    }
    
    /**
     * Constructs a test module in the reporting database, and returns minimal
     * data about it in.
     * 
     * @param rdb a connection to the reporting database.
     * @param academicYear the academic year the module will belong to.
     * @param idSource an ID generator.
     * @return the new module.
     * @throws SQLException 
     */
    public static Module createTestModule(final Connection rdb, final AcademicYearCode academicYear,
            final RdbIdSource idSource)
        throws SQLException {
        return RdbUtil.createTestModule(rdb, academicYear, TEST_COURSE_CODE,
                TEST_OCCURRENCE, TEST_SEMESTER, idSource);
    }
    
    /**
     * Constructs a test module in the reporting database, and returns minimal
     * data about it in.
     * 
     * @param rdb a connection to the reporting database.
     * @param academicYear the academic year the module will belong to.
     * @param idSource an ID generator.
     * @return the new module;
     * @throws SQLException 
     */
    public static Module createTestModule(final Connection rdb, final AcademicYearCode academicYear,
            final String courseCode, final String occurrence, final String semester, final RdbIdSource idSource)
        throws SQLException {
        final String hostKey = courseCode + "_"
            + occurrence + "_"
            + semester;
        final String learnAcademicYear = academicYear.toString().replace("/", "-");
        final Module module = new Module();
        
        module.setModuleId(idSource.getId());
        module.setTimetablingCourseCode(hostKey);
        module.setTimetablingModuleName("Test module "
            + module.getModuleId());
        module.setCacheCourseCode(courseCode);
        module.setCacheSemesterCode(occurrence);
        module.setCacheOccurrenceCode(semester);
        module.setLearnAcademicYear(learnAcademicYear);
        module.setLearnCourseCode(courseCode + learnAcademicYear + occurrence + semester);
        
        final PreparedStatement statement = rdb.prepareStatement(
            "Insert into MODULE (ID,NAME,HOST_KEY,DESCRIPTION,DEPARTMENT,LINK_SIZE,"
                + "PLANNED_SIZE,CREDIT_PROVIDED,RESERVED_SIZE,WEEK_PATTERN,STARTS_PREFS,"
                + "USAGE_PREFS,BASE_AVAILABILITY,USER_TEXT_1,USER_TEXT_2,USER_TEXT_3,"
                + "USER_TEXT_4,USER_TEXT_5,OBSOLETEFROM,LATESTTRANSACTION,WEEKPATTERNLABEL) "
                + "VALUES (?,?,?,?,?,0,0,20,0, "
                + "?,'81A890B186AA85CF1C90FD83AA4D3360',"
                + "'81A890B186AA85CF1C90FD83AA4D3360','81A890B186AA85CF1C90FD83AA4D5D02',"
                + "null,?,null,null,null,-2147483646,240,'N/A - Unbookable 13/14-N/A - Unbookable 13/14')");
        try {
            int paramIdx = 1;
            
            statement.setString(paramIdx++, module.getModuleId());
            statement.setString(paramIdx++, module.getTimetablingModuleName());
            statement.setString(paramIdx++, hostKey);
            statement.setString(paramIdx++, hostKey);
            statement.setString(paramIdx++, DEPARTMENT_ID);
            statement.setString(paramIdx++, WEEK_PATTERN);
            statement.setString(paramIdx++, academicYear.toString());
            
            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        return module;
    }

    /**
     * Really ugly kludge to generate something a bit like a host key.
     * 
     * @param entityId an S+ 32 character ID.
     * @return a host key.
     */
    private static String generateHostKey(final String entityId) {
        final String subpart = entityId.substring(entityId.length() - 6);
        return "#SPLUS" + subpart;
    }
    
    public static int updateModuleAyr(final Connection rdb, final AcademicYearCode academicYearCode,
            final Module testModule)
        throws SQLException {
        testModule.setTimetablingAcademicYear(academicYearCode.toString());
        
        final PreparedStatement statement = rdb.prepareStatement(
            "UPDATE MODULE SET USER_TEXT_2=? "
                + "WHERE ID=?");
        try {
            statement.setString(1, academicYearCode.toString());
            statement.setString(2, testModule.getModuleId());
            return statement.executeUpdate();
        } finally {
            statement.close();
        }
    }
}
