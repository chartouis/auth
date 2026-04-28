package yzarr.auth.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.AuthProperties;
import yzarr.auth.model.TokenException;
import yzarr.auth.model.enums.TokenFailureReason;
import yzarr.auth.model.enums.TokenType;

@Service
@Slf4j
public class CookieService {

    private final AuthProperties props;

    public CookieService(AuthProperties props) {
        this.props = props;
    }

    public void setRefreshTokenCookie(String token, HttpServletResponse response, boolean rememberMe) {
        Long expiry = rememberMe ? props.getRefreshTokenExpiryMs() / 1000 : props.getShortAbsoluteExpiryMs() / 1000;
        setCookie(token, response, TokenType.REFRESH_TOKEN.toString(), "/auth/refresh", expiry);
    }

    public void setAccessTokenCookie(String token, HttpServletResponse response) {
        setCookie(token, response, TokenType.ACCESS_TOKEN.toString(), "/api", props.getAccessTokenExpiryMs() / 1000);
    }

    public void setChallengeCookie(String token, HttpServletResponse response) {
        setCookie(token, response, TokenType.CHALLENGE.toString(), "/auth/verify/2fa/status",
                props.getChallengeTokenExpiryMs() / 1000);
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
        return requireToken(request, TokenType.ACCESS_TOKEN);
    }

    public String getRefreshToken(HttpServletRequest request) {
        return requireToken(request, TokenType.REFRESH_TOKEN);
    }

    public String getChallenge(HttpServletRequest request) {
        return requireToken(request, TokenType.CHALLENGE);
    }

    private String requireToken(HttpServletRequest request, TokenType tokenType) {
        String token = getToken(request, tokenType.toString());

        if (token == null) {
            throw new TokenException(tokenType, TokenFailureReason.MISSING);
        }

        return token;
    }

    private String getToken(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private String clearCookieString(TokenType type) {
        return ResponseCookie.from(type.toString(), "")
                .httpOnly(props.isHttpOnly())
                .secure(props.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite(props.getSameSite())
                .build()
                .toString();
    }

    public void clearCookie(TokenType type, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookieString(type));
    }
}