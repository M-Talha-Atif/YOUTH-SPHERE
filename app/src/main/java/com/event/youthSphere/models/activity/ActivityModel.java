package com.event.youthSphere.models.activity;


import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ActivityModel {
    private String id;
    private String name;
    private String domain;
    private int minAge;
    private int maxAge;
    private String description;
    private List<String> days;
    private int maxParticipants;
    private String month;
    private String instructorId;
    private int registeredCount;

    private Date startDate;
    private Date endDate;


    public ActivityModel() {} // Firebase requires empty constructor

    public ActivityModel(String id, String name, String domain, int minAge, int maxAge, String description,
                         List<String> days, int maxParticipants, String month, String instructorId,
                         int registeredCount, Date startDate, Date endDate) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.description = description;
        this.days = days;
        this.maxParticipants = maxParticipants;
        this.month = month;
        this.instructorId = instructorId;
        this.registeredCount = registeredCount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // equals and hashcode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityModel that = (ActivityModel) o;

        return minAge == that.minAge &&
                maxAge == that.maxAge &&
                maxParticipants == that.maxParticipants &&
                registeredCount == that.registeredCount &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(description, that.description) &&
                Objects.equals(days, that.days) &&
                Objects.equals(month, that.month) &&
                Objects.equals(instructorId, that.instructorId);
    }

    @Override
    public int hashCode() {
        return id.hashCode(); // optional but recommended for collections
    }


    // Getters and setters

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getDays() {
        return days;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public String getMonth() {
        return month;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}

