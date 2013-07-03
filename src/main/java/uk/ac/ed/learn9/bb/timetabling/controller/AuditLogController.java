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
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentChangePartDao;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChangePart;

/**
 * Controller for rendering audit logs of changes made to a course in Learn,
 * over time.
 */
@Controller
public class AuditLogController extends AbstractController {
    @Autowired
    private EnrolmentChangePartDao enrolmentChangePartDao;
    
    /**
     * Displays an audit log of when students were added/removed to/from groups
     * for a single course.
     * 
     * @param request the request from the remote client.
     * @param response the response to be returned to the remote client.
     * @return the data model and view of it to be rendered.
     * @throws UnsupportedEncodingException if the US-ASCII character set is
     * not supported, and as such URL elements cannot be encoded.
     */
    @RequestMapping("/index")
    public ModelAndView getAuditLog(final HttpServletRequest request, final HttpServletResponse response)
            throws UnsupportedEncodingException {
        final Context context = ContextManagerFactory.getInstance().getContext();
        final ModelAndView modelAndView = new ModelAndView("auditLog");
        final Course course = context.getCourse();
        final List<EnrolmentChangePart> pendingChange = new ArrayList<EnrolmentChangePart>();
        
        pendingChange.addAll(this.getEnrolmentChangePartDao().getByCourse(course));
        Collections.sort(pendingChange);
        
        modelAndView.addObject("mergedCourses", PlugInUtil.getUri(PLUGIN_VENDOR_ID,
                PLUGIN_ID, "mergedCourses?course_id="
                + URLEncoder.encode(course.getId().getExternalString(), US_ASCII)));
        modelAndView.addObject("changes", pendingChange);
        
        return modelAndView;
    }

    /**
     * Get the enrolment change part DAO.
     * 
     * @return the enrolment change part DAO.
     */
    public EnrolmentChangePartDao getEnrolmentChangePartDao() {
        return enrolmentChangePartDao;
    }

    /**
     * Set the enrolment change part DAO.
     * 
     * @param enrolmentChangePartDao the enrolment change part DAO to set.
     */
    public void setEnrolmentChangePartDao(EnrolmentChangePartDao enrolmentChangePartDao) {
        this.enrolmentChangePartDao = enrolmentChangePartDao;
    }
}
