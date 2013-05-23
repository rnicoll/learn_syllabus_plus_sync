package uk.ac.ed.learn9.bb.timetabling.data;



import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A student set in timetabling, which can contain zero or more students.
 * For the purposes of synchronisation to Learn, we only care about the
 * sets with exactly one student in.
 */
@Entity
@Table(name="student_set")
public class StudentSet extends Object implements Serializable {
    private String studentSetId;
    private String hostKey;
    private String learnUserId;

    /**
     * Gets the ID for the student set (a 32 character string).
     * 
     * @return the student-set ID.
     */
    @Id
    @Column(name="tt_student_set_id", nullable=false, length=32)
    public String getStudentSetId() {
        return studentSetId;
    }

    /**
     * Returns the host key for this student set. Where applicable,
     * this is the unique identifier used by the host institution,
     * in the case of the University of Edinburgh this is a student's
     * username.
     * 
     * @return the host key. Note that this should always be present, and
     * will not be a valid username if the student set doesn't reflect an
     * individual student.
     */
    @Column(name="tt_host_key", nullable=true, length=32)
    public String getHostKey() {
        return hostKey;
    }

    /**
     * Gets the ID of the User object in Learn that this student set relates
     * to, where applicable.
     * 
     * @return the ID of the User object in Learn that this student set relates
     * to, returns null if no matching user has been found (yet).
     */
    @Column(name="learn_user_id", nullable=true, length=80)
    public String getLearnUserId() {
        return learnUserId;
    }

    /**
     * Sets the ID of this student set.
     * 
     * @param studentSetId the ID of the student set.
     */
    public void setStudentSetId(String studentSetId) {
        this.studentSetId = studentSetId;
    }

    /**
     * @param learnUserId the learnUserId to set
     */
    public void setLearnUserId(String learnUserId) {
        this.learnUserId = learnUserId;
    }

    /**
     * Sets the host key of this student set.
     * 
     * @param hostKey the hostKey to set
     */
    public void setHostKey(String hostKey) {
        this.hostKey = hostKey;
    }
}
