package com.hunghv.inmobitestjava.utils;

import com.hunghv.inmobitestjava.constant.ResponseMessage;
import com.hunghv.inmobitestjava.exception.UnauthorizedException;
import com.hunghv.inmobitestjava.security.UserPrincipal;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw unauthenticated();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }

        throw unauthenticated();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private static UnauthorizedException unauthenticated() {
        return new UnauthorizedException(ResponseMessage.AUTHENTICATION_REQUIRED);
    }
}
