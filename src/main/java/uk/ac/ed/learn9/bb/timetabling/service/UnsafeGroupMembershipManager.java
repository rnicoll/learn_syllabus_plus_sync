package uk.ac.ed.learn9.bb.timetabling.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.apache.velocity.app.VelocityEngine;

import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.GroupMembership;
import blackboard.data.user.User;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.user.UserDbLoader;

/**
 * Helper class to track group memberships 
 */
class UnsafeGroupMembershipManager {
    private final Map<Course, Collection<GroupMembership>> unsafeMemberships
        = new HashMap<Course, Collection<GroupMembership>>();
    private final UserDbLoader userLoader;
    private final VelocityEngine velocityEngine;
    private final CourseMembershipDbLoader courseMembershipLoader;
    private final MailSender mailSender;
    private final SimpleMailMessage messageTemplate;
    
    private Logger log = Logger.getLogger(UnsafeGroupMembershipManager.class);
   
    protected           UnsafeGroupMembershipManager(final CourseMembershipDbLoader setCourseMembershipLoader,
            final UserDbLoader setUserLoader, final VelocityEngine setVelocityEngine, 
            final MailSender setMailSender, final SimpleMailMessage setMessageTemplate) {
        this.courseMembershipLoader = setCourseMembershipLoader;
        this.velocityEngine = setVelocityEngine;
        this.userLoader = setUserLoader;
        this.mailSender = setMailSender;
        this.messageTemplate = setMessageTemplate;
    }
    
    public void addMembership(final Course course, final GroupMembership membership) {
        Collection<GroupMembership> memberships = this.unsafeMemberships.get(course);
        
        if (null == memberships) {
            memberships = new ArrayList<GroupMembership>();
            this.unsafeMemberships.put(course, memberships);
        }
        
        memberships.add(membership);
    }
    
    public void emailMemberships()
        throws KeyNotFoundException, MailException, PersistenceException {
        if (this.unsafeMemberships.isEmpty()) {
            return;
        }
        
        for (Course course: this.unsafeMemberships.keySet()) {
            final Collection<GroupMembership> memberships = this.unsafeMemberships.get(course);
            final SimpleMailMessage message = this.buildMessage(course, memberships);
            
            if (null != message) {
                this.mailSender.send(message);
            }
        }
    }

    private SimpleMailMessage buildMessage(final Course course, final Collection<GroupMembership> memberships)
            throws KeyNotFoundException, PersistenceException {
        final Collection<String> to = getInstructorAddresses(course);
        
        if (to.isEmpty()) {
            return null;
        }
        
        final SimpleMailMessage message = new SimpleMailMessage(this.messageTemplate);
        
        log.debug("Instructors: "
            + to.toString());
        // For live:
        // message.setTo(to.toArray(new String[to.size()]));
        message.setTo("Ross.Nicoll@ed.ac.uk");
        message.setSubject("Learn Timetabling sync requires manual intervention");
        message.setText("Students on "
            + course.getCourseId() + " have moved groups within the Timetabling system. "
            + "The students have been added to their new groups in Learn, but have not "
            + "been removed from their existing groups because group tools have been "
            + "enabled and there may be important content that would be lost if they "
            + "were removed automatically.\r\n\r\n"
            + "You should review the list of students affected by logging into Learn "
            + "and selecting Timetabling Sync from the Course Tools menu."
            + "You can then choose whether to remove the student from their existing "
            + "groups or not, and whether any information needs to be copied before "
            + "this happens.");
        
        return message;
    }

    private Collection<String> getInstructorAddresses(final Course course)
            throws KeyNotFoundException, PersistenceException {
        final Set<String> emailAddresses = new HashSet<String>();
        
        for (CourseMembership membership: this.courseMembershipLoader.loadByCourseIdAndInstructorFlag(course.getId())) {
            final User user = membership.getUser();
            
            if (null == user.getEmailAddress()) {
                continue;
            }
            
            emailAddresses.add(user.getEmailAddress());
        }
        
        return emailAddresses;
    }
}
