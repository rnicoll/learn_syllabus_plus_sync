package uk.ac.ed.learn9.bb.timetabling.data;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Tests of the timetabling course code class, in particular the course code
 * validation.
 */
public class TimetablingCourseCodeTest {
    
    public TimetablingCourseCodeTest() {
    }

    /**
     * Test of buildCode method, of class TimetablingCourseCode.
     */
    @Test
    public void testBuildCode() {
        System.out.println("buildCode");
        String courseCode = "PGHC11154";
        String occurrence = "SV1";
        String semester = "SB5+";
        TimetablingCourseCode expResult = new TimetablingCourseCode("PGHC11154_SV1_SB5+");
        TimetablingCourseCode result = TimetablingCourseCode.buildCode(courseCode, occurrence, semester);
        assertEquals(expResult, result);
    }

    /**
     * Test attempting to build an invalid course code.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testBuildInvalidCode() {
        System.out.println("buildInvalidCode");
        String courseCode = "11154";
        String occurrence = "SV1";
        String semester = "SB5+";
        TimetablingCourseCode result = TimetablingCourseCode.buildCode(courseCode, occurrence, semester);
    }
}
