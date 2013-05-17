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
    public static final String TEST_MODULE_HOST_KEY = TEST_COURSE_CODE + "_SV1_SEM1";
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
        final Module module = new Module();
        
        module.setModuleId(idSource.getId());
        module.setTimetablingCourseCode(TEST_MODULE_HOST_KEY);
        module.setTimetablingModuleName("Test module "
            + module.getModuleId());
        module.setCacheCourseCode(TEST_COURSE_CODE);
        
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
            statement.setString(paramIdx++, TEST_MODULE_HOST_KEY);
            statement.setString(paramIdx++, TEST_MODULE_HOST_KEY);
            statement.setString(paramIdx++, DEPARTMENT_ID);
            statement.setString(paramIdx++, WEEK_PATTERN);
            statement.setString(paramIdx++, academicYear.toString());
            
            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        return module;
    }
}
