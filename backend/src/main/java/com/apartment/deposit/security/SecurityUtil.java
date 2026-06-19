package com.apartment.deposit.security;

import com.apartment.deposit.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser) {
            return (SysUser) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        SysUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUserRole() {
        SysUser user = getCurrentUser();
        return user != null ? user.getRoleCode() : null;
    }

    public static boolean hasRole(String roleCode) {
        String currentRole = getCurrentUserRole();
        return roleCode.equals(currentRole);
    }
}
