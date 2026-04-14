package com.mes.config;

import com.mes.entity.User;

public class UserContext {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean hasPermission(String permissionName) {
        if (currentUser == null) return false;
        return currentUser.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> {
                    String permName = perm.getName();
                    if (permName.equals(permissionName)) return true;
                    if (permName.endsWith(":manage")) {
                        String prefix = permName.substring(0, permName.indexOf(":manage"));
                        if (permissionName.startsWith(prefix + ":")) return true;
                    }
                    return false;
                });
    }
}
