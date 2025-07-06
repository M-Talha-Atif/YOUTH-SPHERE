package com.event.youthSphere.domain.model.users;

import java.util.Map;

public interface RoleUser {
    Map<String, Object> toMap();
    String getUid();
}

