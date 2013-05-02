package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="student_set")
public class StudentSet extends Object implements Serializable {
    private String studentSetId;
    private String learnUserId;

    /**
     * @return the student-set ID.
     */
    @Id
    @Column(name="tt_student_set_id", nullable=false, length=32)
    public String getStudentSetId() {
        return studentSetId;
    }

    /**
     * @return the ID of the User object in Learn.
     */
    @Column(name="learn_user_id", nullable=true, length=80)
    public String getLearnUserId() {
        return learnUserId;
    }

    /**
     * @param studentSetId the studentSetId to set
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
}
