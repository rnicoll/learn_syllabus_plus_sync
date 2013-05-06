package uk.ac.ed.learn9.bb.timetabling;

import java.util.ArrayList;
import java.util.Collections;
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
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentAddDao;
import uk.ac.ed.learn9.bb.timetabling.dao.EnrolmentRemoveDao;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;

@Controller
public class TimetablingController {
    @Autowired
    private EnrolmentAddDao enrolmentAddDao;
    @Autowired
    private EnrolmentRemoveDao enrolmentRemoveDao;
    
    /**
     * Displays an audit log of when students were added/removed to/from groups
     * for a single course.
     */
    @RequestMapping("/auditLog")
    public ModelAndView getAuditLog(final HttpServletRequest request, final HttpServletResponse response) {
        final Context context = ContextManagerFactory.getInstance().getContext();
        final ModelAndView modelAndView = new ModelAndView("auditLog");
        final Course course = context.getCourse();
        final List<EnrolmentChange> changes = new ArrayList<EnrolmentChange>();
        
        // Log group creation?
        
        changes.addAll(this.getEnrolmentAddDao().getByCourse(course));
        changes.addAll(this.getEnrolmentRemoveDao().getByCourse(course));
        
        Collections.sort(changes);
        
        modelAndView.addObject("audit_log", changes);
        
        return modelAndView;
    }

    /**
     * @return the enrolmentAddDao
     */
    public EnrolmentAddDao getEnrolmentAddDao() {
        return enrolmentAddDao;
    }

    /**
     * @return the enrolmentRemoveDao
     */
    public EnrolmentRemoveDao getEnrolmentRemoveDao() {
        return enrolmentRemoveDao;
    }

    /**
     * @param enrolmentAddDao the enrolmentAddDao to set
     */
    public void setEnrolmentAddDao(EnrolmentAddDao enrolmentAddDao) {
        this.enrolmentAddDao = enrolmentAddDao;
    }

    /**
     * @param enrolmentRemoveDao the enrolmentRemoveDao to set
     */
    public void setEnrolmentRemoveDao(EnrolmentRemoveDao enrolmentRemoveDao) {
        this.enrolmentRemoveDao = enrolmentRemoveDao;
    }
}
