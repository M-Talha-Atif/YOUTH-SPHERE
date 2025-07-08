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
}

