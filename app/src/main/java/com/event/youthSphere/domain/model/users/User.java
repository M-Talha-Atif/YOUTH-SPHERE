package com.event.youthSphere.domain.model.users;

public abstract class User implements RoleUser {
    protected String uid;
    protected String email;
    protected String fullName;
    protected String role;

    public User(String uid, String email, String fullName, String role) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
