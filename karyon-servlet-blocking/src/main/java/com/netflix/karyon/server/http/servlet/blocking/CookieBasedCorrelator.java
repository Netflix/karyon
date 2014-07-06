package com.netflix.karyon.server.http.servlet.blocking;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

/**
 * @author Nitesh Kant
 */
public class CookieBasedCorrelator implements SessionCorrelator {

    public static final String DEFAULT_SESSION_ID_COOKIE_NAME = "JSESSIONID";

    private final String cookieName;

    public CookieBasedCorrelator() {
        this(DEFAULT_SESSION_ID_COOKIE_NAME);
    }

    public CookieBasedCorrelator(String cookieName) {
        this.cookieName = cookieName;
    }

    @Nullable
    @Override
    public String getSessionIdForRequest(HttpServletRequestImpl htpRequest) {
        Cookie[] cookies = htpRequest.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue(); // Not sure what should be the behavior if there are multiple JSESSIONID cookies
                }
            }
        }
        return null;
    }
}
