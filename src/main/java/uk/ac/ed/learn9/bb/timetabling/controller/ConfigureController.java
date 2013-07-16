package uk.ac.ed.learn9.bb.timetabling.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import blackboard.data.ValidationException;
import blackboard.persist.PersistenceException;
import blackboard.platform.plugin.PlugInUtil;
import uk.ac.ed.learn9.bb.timetabling.dao.ConfigurationDao;
import uk.ac.ed.learn9.bb.timetabling.dao.SynchronisationRunDao;
import uk.ac.ed.learn9.bb.timetabling.data.Configuration;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;
import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationRunService;
import uk.ac.ed.learn9.bb.timetabling.service.SynchronisationService;

/**
 * Unused controller that could be used for a settings page if needed later.
 */
@Controller
public class ConfigureController extends AbstractController {
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private SynchronisationRunService synchronisationRunService;
    @Autowired
    private SynchronisationService synchronisationService;
    @Autowired
    private SynchronisationRunDao synchronisationRunDao;
    
    private Logger log = Logger.getLogger(ConfigureController.class);
    
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
        final Configuration configuration = this.getConfigurationDao().getDefault();
        final List<SynchronisationRun> runs = new ArrayList<SynchronisationRun>();
        
        runs.addAll(this.getSynchronisationRunDao().getAll());
        Collections.sort(runs);
        
        modelAndView.addObject("configuration", configuration);
        modelAndView.addObject("runSynchronisation", PlugInUtil.getUri(PLUGIN_VENDOR_ID,
                PLUGIN_ID, "run"));
        modelAndView.addObject("runs", runs);
        
        return modelAndView;
    }

    /**
     * Runs the synchronisation process manually. Note that this frequently
     * creates a request timed out page - possibly should start the synchronisation
     * in the background instead?
     * 
     * @param request the request from the remote client.
     * @param response the response to be returned to the remote client.
     * @return the data model and view of it to be rendered.
     * @throws SQLException if there was a problem accessing the database.
     * @throws PersistenceException if there was a problem saving changes to
     * Learn.
     * @throws ValidationException if there was a problem validating data to be
     * written back to Learn.
     */
    @RequestMapping(value="/run")
    public ModelAndView doRun(final HttpServletRequest request, final HttpServletResponse response)
        throws SQLException, PersistenceException, ValidationException {
        final SynchronisationRun run;
        
        try {
            run = this.getSynchronisationRunService().startNewRun();
        } catch (SynchronisationRunService.SynchronisationAlreadyInProgressException ex) {
            // This is expected under normal circumstances, due to more than one
            // possible server trying to run the job.
            // XXX: Return a useful error
            return this.getConfigure(request, response);
        }
        
        final SynchronisationService service = this.getSynchronisationService();
        
        final Thread thread = new Thread("Manual timetabling sync") {
            @Override
            public void run() {
                try {
                    service.runSynchronisation(run);
                } catch(Exception e) {
                    try {
                        synchronisationRunService.handleErrorOutcome(run, e);
                    } catch(SQLException logError) {
                        // Give up
                    }
                    log.error("Error while running synchronisation.", e);
                }
            }
        };
        thread.start();
                
        return this.getConfigure(request, response);
    }

    /**
     * Get the synchronisation run data access object.
     * 
     * @return the synchronisation run data access object.
     */
    public SynchronisationRunDao getSynchronisationRunDao() {
        return synchronisationRunDao;
    }

    /**
     * @return the configurationDao
     */
    public ConfigurationDao getConfigurationDao() {
        return configurationDao;
    }

    /**
     * Get the synchronisation service.
     * 
     * @return the synchronisation service.
     */
    public SynchronisationService getSynchronisationService() {
        return synchronisationService;
    }

    /**
     * Set the synchronisation service.
     * 
     * @param synchronisationService the synchronisation service to set.
     */
    public void setSynchronisationService(SynchronisationService synchronisationService) {
        this.synchronisationService = synchronisationService;
    }

    /**
     * Get the concurrency service.
     * 
     * @return the concurrency service.
     */
    public SynchronisationRunService getSynchronisationRunService() {
        return synchronisationRunService;
    }

    /**
     * @param configurationDao the configurationDao to set
     */
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    /**
     * Set the synchronisation run data access object.
     * 
     * @param synchronisationRunDao the synchronisation run data access object to set.
     */
    public void setSynchronisationRunDao(SynchronisationRunDao synchronisationRunDao) {
        this.synchronisationRunDao = synchronisationRunDao;
    }

    /**
     * @param synchronisationRunService the synchronisationRunService to set
     */
    public void setSynchronisationRunService(SynchronisationRunService synchronisationRunService) {
        this.synchronisationRunService = synchronisationRunService;
    }
}
