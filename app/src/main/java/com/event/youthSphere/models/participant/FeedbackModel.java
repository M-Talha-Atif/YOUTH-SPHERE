package com.event.youthSphere.models.participant;

import java.util.Date;
public class FeedbackModel {
    private String activityId;
    private String participantId;
    private String participantName;
    private String instructorId;
    private String parentId;
    private int rating;
    private String comment;
    private Date timestamp;

    public FeedbackModel() {}

    public FeedbackModel(String activityId, String participantId, String participantName,
                         String instructorId, String parentId,
                         int rating, String comment, Date timestamp) {
        this.activityId = activityId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.instructorId = instructorId;
        this.parentId = parentId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
