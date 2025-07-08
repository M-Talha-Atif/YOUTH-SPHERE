package com.event.youthSphere.models.users;


import java.util.HashMap;
import java.util.Map;

public class CoordinatorUser extends User {

    public CoordinatorUser(String uid, String email, String fullName) {
        super(uid, email, fullName, "COORDINATOR");
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("fullName", fullName);
        map.put("role", role);
        return map;
    }
}
