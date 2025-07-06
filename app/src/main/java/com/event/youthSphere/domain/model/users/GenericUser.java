package com.event.youthSphere.domain.model.users;


import java.util.HashMap;
import java.util.Map;

public class GenericUser extends User {

    public GenericUser(String uid, String email, String fullName, String role) {
        super(uid, email, fullName, role.toUpperCase());
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

