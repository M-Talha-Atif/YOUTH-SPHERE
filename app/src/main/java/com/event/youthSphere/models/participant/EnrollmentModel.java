package com.event.youthSphere.models.participant;

public class EnrollmentModel {
    private String enrollmentId;
    private String participantId;
    private String activityId;

    private String name;
    private String domain;

    private String description;
    private long enrolledAt;

    public EnrollmentModel() {} // Required for Firebase

    public EnrollmentModel(String enrollmentId, String participantId, String activityId, long enrolledAt, String name, String domain, String description) {
        this.enrollmentId = enrollmentId;
        this.participantId = participantId;
        this.activityId = activityId;
        this.enrolledAt = enrolledAt;
        this.name = name;
        this.domain = domain;
        this.description = description;
    }


    @Override
    public String toString() {
        return getName();
    }


    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public long getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(long enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }
}

