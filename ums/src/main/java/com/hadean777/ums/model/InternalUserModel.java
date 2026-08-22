package com.hadean777.ums.model;

import java.util.Set;

public class InternalUserModel {

    private Long userId;
    private boolean isAdmin = true;
    private Set<String> permissions;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(String permission) {
        if (permission != null && !permission.isBlank() && permissions != null && !permissions.isEmpty()) {
            return permissions.contains(permission);
        }
        return false;
    }
}
