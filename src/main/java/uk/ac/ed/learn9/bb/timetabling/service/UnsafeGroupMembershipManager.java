package uk.ac.ed.learn9.bb.timetabling.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.GroupMembership;
import blackboard.data.user.User;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.user.UserDbLoader;

/**
 * Helper class to track group memberships which could not be applied as they
 * are unsafe, and send an e-mail to instructors to notify them of these problem
 * cases.
 */
class UnsafeGroupMembershipManager {
    /** Standard e-mail signature for automatically generated mail. */
    public static final String EMAIL_SIGNATURE = SynchronisationRunService.EMAIL_SIGNATURE;
    
    private final Map<Course, Collection<GroupMembership>> unsafeMemberships
        = new HashMap<Course, Collection<GroupMembership>>();
    private final UserDbLoader userLoader;
    private final VelocityEngine velocityEngine;
    private final CourseMembershipDbLoader courseMembershipLoader;
    private final MailSender mailSender;
    private String mailToOverride = null;
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

    protected SimpleMailMessage buildMessage(final Course course, final Collection<GroupMembership> memberships)
            throws KeyNotFoundException, PersistenceException {
        final Collection<String> to = getInstructorAddresses(course);
        
        if (to.isEmpty()) {
            return null;
        }
        final StringBuilder body = new StringBuilder();
        
        body.append("Students on ").append(course.getCourseId())
            .append(" have moved groups within the Timetabling system. ")
            .append("These students have been added to their new groups in Learn, but have not ")
            .append("been removed from their existing groups because group tools have been ")
            .append("enabled and there may be important content that would be lost if they ")
            .append("were removed automatically. A list of affected students is provided below:\r\n");

        for (GroupMembership membership: memberships) {
            final CourseMembership courseMembership = this.courseMembershipLoader.loadById(membership.getCourseMembershipId());
            final User student = this.userLoader.loadById(courseMembership.getUserId());
            
            body.append(student.getGivenName()).append(" ")
                    .append(student.getFamilyName()).append(" (")
                    .append(student.getUserName()).append(")\r\n");
        }
        
        body.append("\r\n")
            .append("You can also review the list of students affected by logging into Learn ")
            .append("and selecting Timetabling Sync from the Course Tools menu.\r\n");
        
        body.append("You can then choose whether to remove the student from their existing ")
            .append("groups or not, and whether any information needs to be copied before ")
            .append("this happens.\r\n\r\n")
            .append(EMAIL_SIGNATURE);
        
        final SimpleMailMessage message = new SimpleMailMessage(this.messageTemplate);
        
        if (null != this.getMailToOverride()) {
            log.debug("Instructors: "
                + to.toString());
            message.setTo(this.getMailToOverride());
        } else {
            message.setTo(to.toArray(new String[to.size()]));
        }
        message.setSubject("Student groups on Learn Course "
            + course.getCourseId() + " - Instructor action required");
        message.setText(body.toString());
        
        return message;
    }

    private Collection<String> getInstructorAddresses(final Course course)
            throws KeyNotFoundException, PersistenceException {
        final Set<String> emailAddresses = new HashSet<String>();
        
        for (CourseMembership membership: this.courseMembershipLoader.loadByCourseIdAndInstructorFlag(course.getId())) {
            final User user = this.userLoader.loadById(membership.getUserId());
            
            assert null != user;
            if (null == user.getEmailAddress()) {
                continue;
            }
            
            emailAddresses.add(user.getEmailAddress());
        }
        
        return emailAddresses;
    }

    /**
     * Gets the address that all mail is sent to, where defined. Normally this
     * function is not used, and returns null, however this is supported for
     * test cases.
     * 
     * @return the address that all mail is sent to, or null if unset (the
     * common case).
     */
    public String getMailToOverride() {
        return mailToOverride;
    }

    /**
     * @param mailToOverride the mailToOverride to set
     */
    public void setMailToOverride(String mailToOverride) {
        this.mailToOverride = mailToOverride;
    }
}
