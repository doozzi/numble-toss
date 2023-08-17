package com.gangbean.stockservice.util;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

public class SecurityUtil {
    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    public static final String AUTHORIZATION_HEADER_VALUE_PREFIX = "Bearer ";
    public static final int AUTHORIZATION_HEADER_VALUE_PREFIX_LENGTH = 7;
    public static final String EMPTY_TOKEN = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtil.class);

    private SecurityUtil() {}

    public static String resolveToken(HttpServletRequest request) {
        return hasTextAndStartsWithValidPrefix(request.getHeader(AUTHORIZATION_HEADER_KEY)) ?
            prefixRemovedToken(request.getHeader(AUTHORIZATION_HEADER_KEY)) : EMPTY_TOKEN;
    }

    public static Optional<String> getCurrentUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            LOGGER.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        String username = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            username = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        return Optional.ofNullable(username);
    }

    private static String prefixRemovedToken(String authorizationHeaderValue) {
        return authorizationHeaderValue.substring(AUTHORIZATION_HEADER_VALUE_PREFIX_LENGTH);
    }

    private static boolean hasTextAndStartsWithValidPrefix(String bearerToken) {
        return StringUtils.hasText(bearerToken)
            && bearerToken.startsWith(AUTHORIZATION_HEADER_VALUE_PREFIX);
    }
}
