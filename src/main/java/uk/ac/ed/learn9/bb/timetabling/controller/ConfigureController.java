package uk.ac.ed.learn9.bb.timetabling.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unused controller that could be used for a settings page if needed later.
 */
@Controller
public class ConfigureController extends Object {
    /**
     * Displays an audit log of when students were added/removed to/from groups
     * for a single course.
     * 
     * @param request the request from the remote client.
     * @param response the response to be returned to the remote client.
     * @return the data model and view of it to be rendered.
     */
    @RequestMapping(value="/configure", method=RequestMethod.GET)
    public ModelAndView getConfigure(final HttpServletRequest request, final HttpServletResponse response) {
        final ModelAndView modelAndView = new ModelAndView("configure");
        
        return modelAndView;
    }
}
