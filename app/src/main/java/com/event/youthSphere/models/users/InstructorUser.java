package com.event.youthSphere.models.users;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorUser extends User {

    private List<String> activityIds;
    private String fieldOfSpecialization;
    private String employmentStartDate;

    public InstructorUser() {
        super(null, null, null, "INSTRUCTOR");
    }



    public InstructorUser(String uid, String email, String fullName, List<String> activityIds,
                          String fieldOfSpecialization, String employmentStartDate) {
        super(uid, email, fullName, "INSTRUCTOR");
        this.activityIds = activityIds;
        this.fieldOfSpecialization = fieldOfSpecialization;
        this.employmentStartDate = employmentStartDate;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("fullName", fullName);
        map.put("role", role);
        map.put("activityIds", activityIds);
        map.put("fieldOfSpecialization", fieldOfSpecialization);
        map.put("employmentStartDate", employmentStartDate);
        return map;
    }



    @Override
    public String getEmail() {
        return super.getEmail();
    }

    @Override
    public String getFullName() {
        return super.getFullName();
    }

    public String getFieldOfSpecialization() {
        return fieldOfSpecialization;
    }

    public String getEmploymentStartDate() {
        return employmentStartDate;
    }

    public void setFieldOfSpecialization(String fieldOfSpecialization) {
        this.fieldOfSpecialization = fieldOfSpecialization;
    }

    public void setEmploymentStartDate(String employmentStartDate) {
        this.employmentStartDate = employmentStartDate;
    }

    public void setActivityIds(List<String> activityIds) {
        this.activityIds = activityIds;
    }

    public List<String> getActivityIds() {
        return activityIds;
    }

    public String getActivityIdsAsCommaString() {
        return activityIds != null ? String.join(",", activityIds) : "";
    }

}


