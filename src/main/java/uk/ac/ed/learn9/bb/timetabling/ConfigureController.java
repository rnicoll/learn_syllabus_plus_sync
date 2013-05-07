package uk.ac.ed.learn9.bb.timetabling;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ConfigureController {    
    /**
     * Displays an audit log of when students were added/removed to/from groups
     * for a single course.
     */
    @RequestMapping("/configure")
    public ModelAndView getConfigure(final HttpServletRequest request, final HttpServletResponse response) {
        final ModelAndView modelAndView = new ModelAndView("configure");
        
        return modelAndView;
    }
}
