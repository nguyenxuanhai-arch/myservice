package com.example.myservice.security;

import com.example.myservice.security.details.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.stereotype.Component;
import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication auth, Object target, Object permission) {
        if (auth == null) return false;

        String need = String.valueOf(permission);           // ví dụ "USER_WRITE"
        var principal = (CustomUserDetails) auth.getPrincipal();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        boolean hasPerm = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(need));

        // rule owner: chấp nhận Long id, String email, hoặc entity User
        boolean isOwner = false;
        if (target instanceof Long id) {
            isOwner = principal.getUserId().equals(id);
        } else if (target instanceof String email) {
            isOwner = principal.getUsername().equalsIgnoreCase(email);
        } else if (target instanceof com.example.myservice.modules.users.entities.User u) {
            isOwner = principal.getUserId().equals(u.getId());
        }

        // Chính sách: ADMIN bypass; còn lại cần có quyền + đúng owner
        return isAdmin || (hasPerm && isOwner);
    }

    @Override
    public boolean hasPermission(Authentication a, Serializable id, String type, Object permission) {
        // Chưa dùng đến
        return false;
    }
}
