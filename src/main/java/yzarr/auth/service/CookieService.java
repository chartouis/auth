package yzarr.auth.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.AuthProperties;

@Service
@Slf4j
public class CookieService {

    private final AuthProperties props;

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    public CookieService(AuthProperties props) {
        this.props = props;
    }

    public void setRefreshTokenCookie(String token, HttpServletResponse response, boolean rememberMe) {
        Long expiry = rememberMe ? props.getRefreshTokenExpiryMs() / 1000 : props.getShortAbsoluteExpiryMs() / 1000;
        setCookie(token, response, REFRESH_TOKEN, "/auth/refresh", expiry);
    }

    public void setAccessTokenCookie(String token, HttpServletResponse response) {
        setCookie(token, response, ACCESS_TOKEN, "/api", props.getAccessTokenExpiryMs() / 1000);
    }

    public void setCookie(String jwtToken, HttpServletResponse response, String name, String path, long age) {
        ResponseCookie cookie = ResponseCookie.from(name, jwtToken)
                .path(path)
                .httpOnly(props.isHttpOnly())
                .secure(props.isSecure())
                .sameSite(props.getSameSite())
                .maxAge(age)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.debug("Set cookie: {}", name);
    }

    public String getAccessToken(HttpServletRequest request) {
        return getToken(request, ACCESS_TOKEN);
    }

    public String getRefreshToken(HttpServletRequest request) {
        return getToken(request, REFRESH_TOKEN);
    }

    private String getToken(HttpServletRequest request, String name) {
        if (request.getCookies() == null)
            return null;

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}