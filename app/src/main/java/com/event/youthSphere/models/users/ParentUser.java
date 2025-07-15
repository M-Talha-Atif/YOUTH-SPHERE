package com.event.youthSphere.models.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentUser extends User {

    private List<String> childIds;

    public ParentUser() {
        super();
    }

    public ParentUser(String uid, String email, String fullName, List<String> childIds) {
        super(uid, email, fullName, "PARENT");
        this.childIds = childIds;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("fullName", fullName);
        map.put("role", role);
        map.put("childIds", childIds);
        return map;
    }

    public List<String> getChildIds() {
        return childIds;
    }

    public void setChildIds(List<String> childIds) {
        this.childIds = childIds;
    }
}
