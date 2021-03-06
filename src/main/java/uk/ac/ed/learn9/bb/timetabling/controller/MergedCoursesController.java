package uk.ac.ed.learn9.bb.timetabling.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import blackboard.data.course.Course;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManagerFactory;
import blackboard.platform.plugin.PlugInUtil;

import uk.ac.ed.learn9.bb.timetabling.data.LearnCourseCode;
import uk.ac.ed.learn9.bb.timetabling.service.MergedCoursesService;

/**
 * Controller for displaying the list of courses that are composed to form
 * a course in Learn (such as child courses, merged courses, etc.)
 */
@Controller
public class MergedCoursesController extends AbstractController {
    @Autowired
    private MergedCoursesService mergedCoursesService;
    
    /**
     * Displays details of merged courses that are brought into this course.
     * 
     * @param request the request from the remote client.
     * @param response the response to be returned to the remote client.
     * @return the data model and view of it to be rendered.
     * @throws SQLException if there was a problem accessing the database.
     * @throws UnsupportedEncodingException if the US-ASCII character set is
     * not supported, and as such URL elements cannot be encoded.
     */
    @RequestMapping("/mergedCourses")
    public ModelAndView getMergedCourses(final HttpServletRequest request, final HttpServletResponse response)
            throws UnsupportedEncodingException, SQLException {
        final Context context = ContextManagerFactory.getInstance().getContext();
        final ModelAndView modelAndView = new ModelAndView("mergedCourses");
        final Course course = context.getCourse();
        
        final LearnCourseCode courseCode = new LearnCourseCode(course.getCourseId());
        final List<LearnCourseCode> mergedCourseCodes = new ArrayList<LearnCourseCode>();
        
        mergedCourseCodes.addAll(this.getMergedCoursesService().getMergedCourses(courseCode));
        
        // XXX: Resolve JTA activities here.
        
        modelAndView.addObject("activities", PlugInUtil.getUri(PLUGIN_VENDOR_ID,
                PLUGIN_ID, "activities?course_id="
                + URLEncoder.encode(course.getId().getExternalString(), US_ASCII)));
        modelAndView.addObject("auditLog", PlugInUtil.getUri(PLUGIN_VENDOR_ID,
                PLUGIN_ID, "index?course_id="
                + URLEncoder.encode(course.getId().getExternalString(), US_ASCII)));
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
