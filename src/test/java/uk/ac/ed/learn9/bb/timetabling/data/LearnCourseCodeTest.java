package uk.ac.ed.learn9.bb.timetabling.data;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the Learn course code type.
 */
public class LearnCourseCodeTest {
    
    public LearnCourseCodeTest() {
    }

    /**
     * Test of buildCode method, of class LearnCourseCode.
     */
    @Test
    public void testBuildCode() {
        System.out.println("buildCode");
        String courseCode = "EDUA08064";
        AcademicYearCode ayrCode = new AcademicYearCode("2012/3");
        String occurrence = "SS1";
        String semester = "SEM1";
        LearnCourseCode expResult = new LearnCourseCode("EDUA080642012-3SS1SEM1");
        LearnCourseCode result = LearnCourseCode.buildCode(courseCode, ayrCode, occurrence, semester);
        assertEquals(expResult, result);
    }

    /**
     * Test of LearnCourseCode constructor, in the case of an invalid course code
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidCode() {
        System.out.println("invalidCode");
        
        // Learn course code without a '-' character
        LearnCourseCode instance = new LearnCourseCode("EDUA0806420123SS1SEM1");
    }

    /**
     * Test of getEuclidCourseId method, of class LearnCourseCode.
     */
    @Test
    public void testGetEuclidCourseId() {
        System.out.println("getEuclidCourseId");
        LearnCourseCode instance = new LearnCourseCode("EDUA080642012-3SS1SEM1");
        String expResult = "EDUA08064";
        String result = instance.getEuclidCourseId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getInstance method, of class LearnCourseCode.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        LearnCourseCode instance = new LearnCourseCode("EDUA080642012-3SS1SEM1");
        String expResult = "2012-3SS1SEM1";
        String result = instance.getInstance();
        assertEquals(expResult, result);
    }
}
