package com.smart.parking.security;

import com.smart.parking.domain.User;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthUser au)) {
            throw new IllegalStateException("No authenticated user");
        }
        return au.getUser();
    }

    public static Long currentUserId() { return currentUser().getId(); }
}
