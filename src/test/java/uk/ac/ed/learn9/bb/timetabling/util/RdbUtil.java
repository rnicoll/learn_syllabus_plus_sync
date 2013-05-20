package uk.ac.ed.learn9.bb.timetabling.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import uk.ac.ed.learn9.bb.timetabling.RdbIdSource;
import uk.ac.ed.learn9.bb.timetabling.data.AcademicYearCode;
import uk.ac.ed.learn9.bb.timetabling.data.Module;

/**
 * Class for setting up/tearing down test data in the RDB.
 */
public class RdbUtil {
    public static final String TEST_COURSE_CODE = "PGHC11335";
    public static final String TEST_SEMESTER = "SEM1";
    public static final String TEST_OCCURRENCE = "SV1";
    public static final String DEPARTMENT_ID = "BF20A0ADF91117B06331C6ED3F9FC187";
    public static final String WEEK_PATTERN = "11111111111111111111111111111111111111111111111111111111111111111";
    
    /**
     * Constructs a test module in the reporting database, and returns minimal
     * data about it in.
     * 
     * @param rdb a connection to the reporting database.
     * @param idSource an ID generator.
     * @return the new module;
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
