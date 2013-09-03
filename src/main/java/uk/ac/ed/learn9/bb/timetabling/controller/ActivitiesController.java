package uk.ac.ed.learn9.bb.timetabling.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
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
import uk.ac.ed.learn9.bb.timetabling.dao.ActivityDao;
import uk.ac.ed.learn9.bb.timetabling.data.Activity;

/**
 * Controller for rendering activities related to a course in Learn.
 */
@Controller
public class ActivitiesController extends AbstractController {
    @Autowired
    private ActivityDao activityDao;
    
    /**
     * Displays a list of activities related to a course in Learn.
     * 
     * @param request the request from the remote client.
     * @param response the response to be returned to the remote client.
     * @return the data model and view of it to be rendered.
     * @throws UnsupportedEncodingException if the US-ASCII character set is
     * not supported, and as such URL elements cannot be encoded.
     */
    @RequestMapping("/activities")
    public ModelAndView getActivities(final HttpServletRequest request, final HttpServletResponse response)
            throws UnsupportedEncodingException {
        final Context context = ContextManagerFactory.getInstance().getContext();
        final ModelAndView modelAndView = new ModelAndView("activities");
        final Course course = context.getCourse();
        final List<Activity> activities = getActivityDao().getByCourseLearnId(course.getId());
        
        modelAndView.addObject("activities", activities);
        modelAndView.addObject("auditLog", PlugInUtil.getUri(PLUGIN_VENDOR_ID,
                PLUGIN_ID, "index?course_id="
                + URLEncoder.encode(course.getId().getExternalString(), US_ASCII)));
        modelAndView.addObject("mergedCourses", PlugInUtil.getUri(PLUGIN_VENDOR_ID,
                PLUGIN_ID, "mergedCourses?course_id="
                + URLEncoder.encode(course.getId().getExternalString(), US_ASCII)));
        
        return modelAndView;
    }

    /**
     * @return the activityDao
     */
    public ActivityDao getActivityDao() {
        return activityDao;
    }

    /**
     * @param activityDao the activityDao to set
     */
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }
}
