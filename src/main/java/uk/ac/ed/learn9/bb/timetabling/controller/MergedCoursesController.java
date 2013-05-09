package uk.ac.ed.learn9.bb.timetabling.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import blackboard.data.course.Course;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentChangeDao;

@Controller
public class MergedCoursesController {
    @Autowired
    private EnrolmentChangeDao enrolmentChangeDao;
    
    /**
     * Displays an audit log of when students were added/removed to/from groups
     * for a single course.
     */
    @RequestMapping("/mergedCourses")
    public ModelAndView getMergedCourses(final HttpServletRequest request, final HttpServletResponse response) {
        final Context context = ContextManagerFactory.getInstance().getContext();
        final ModelAndView modelAndView = new ModelAndView("mergedCourses");
        final Course course = context.getCourse();
        
        // Load and add the merged courses list here.
        
        return modelAndView;
    }

    /**
     * @return the enrolmentChangeDao
     */
    public EnrolmentChangeDao getEnrolmentChangeDao() {
        return enrolmentChangeDao;
    }

    /**
     * @param enrolmentChangeDao the enrolmentChangeDao to set
     */
    public void setEnrolmentChangeDao(EnrolmentChangeDao enrolmentChangeDao) {
        this.enrolmentChangeDao = enrolmentChangeDao;
    }
}
