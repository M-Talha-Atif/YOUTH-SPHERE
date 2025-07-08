package com.event.youthSphere.models.users;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticipantUser extends User {

    private List<String> activityIds;

    public ParticipantUser(String uid, String email, String fullName, List<String> activityIds) {
        super(uid, email, fullName, "PARTICIPANT");
        this.activityIds = activityIds;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("fullName", fullName);
        map.put("role", role);
        map.put("activityIds", activityIds);
        return map;
    }
}

