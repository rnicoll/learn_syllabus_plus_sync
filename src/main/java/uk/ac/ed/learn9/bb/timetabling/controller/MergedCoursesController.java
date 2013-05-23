package uk.ac.ed.learn9.bb.timetabling.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import blackboard.data.course.Course;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ed.learn9.bb.timetabling.data.BlackboardCourseCode;
import uk.ac.ed.learn9.bb.timetabling.service.MergedCoursesService;

/**
 * Controller for displaying the list of courses that are composed to form
 * a course in Learn (such as child courses, merged courses, etc.)
 */
@Controller
public class MergedCoursesController {
    @Autowired
    private MergedCoursesService mergedCoursesService;
    
    /**
     * Displays an audit log of when students were added/removed to/from groups
     * for a single course.
     */
    @RequestMapping("/mergedCourses")
    public ModelAndView getMergedCourses(final HttpServletRequest request, final HttpServletResponse response) {
        final Context context = ContextManagerFactory.getInstance().getContext();
        final ModelAndView modelAndView = new ModelAndView("mergedCourses");
        final Course course = context.getCourse();
        
        final BlackboardCourseCode courseCode = new BlackboardCourseCode(course.getCourseId());
        final List<BlackboardCourseCode> mergedCourseCodes = new ArrayList<BlackboardCourseCode>();
        
        mergedCourseCodes.addAll(this.getMergedCoursesService().getMergedCourses(courseCode));
        
        // XXX: Resolve JTA activities here.
        
        modelAndView.addObject("mergedCourseCodes", mergedCourseCodes);
        
        return modelAndView;
    }

    /**
     * @return the mergedCoursesService
     */
    public MergedCoursesService getMergedCoursesService() {
        return mergedCoursesService;
    }

    /**
     * @param mergedCoursesService the mergedCoursesService to set
     */
    public void setMergedCoursesService(MergedCoursesService mergedCoursesService) {
        this.mergedCoursesService = mergedCoursesService;
    }
}
