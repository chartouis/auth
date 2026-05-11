package yzarr.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.Cookie;
import yzarr.auth.AuthProperties;
import yzarr.auth.model.TokenException;
import yzarr.auth.model.enums.TokenFailureReason;
import yzarr.auth.model.enums.TokenType;

class CookieServiceTest {

    AuthProperties props;
    CookieService cookieService;

    @BeforeEach
    void setUp() {
        props = new AuthProperties();
        props.setRefreshTokenExpiryMs(1000L * 60L * 60L * 24L * 30L);
        props.setShortAbsoluteExpiryMs(1000L * 60L * 60L);
        props.setAccessTokenExpiryMs(1000L * 60L * 15L);
        props.setSameSite("Strict");
        props.setHttpOnly(true);
        props.setSecure(false);
        cookieService = new CookieService(props);
    }

    @Test
    void setRefreshTokenCookie_rememberMeTrue_setsMaxAge_refreshTokenExpiryMs() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        cookieService.setRefreshTokenCookie("rt", resp, true);
        String setCookie = resp.getHeader("Set-Cookie");
        assertThat(setCookie).contains("Max-Age=" + (props.getRefreshTokenExpiryMs() / 1000));
        assertThat(setCookie).contains(TokenType.REFRESH_TOKEN.toString() + "=");
    }

    @Test
    void setRefreshTokenCookie_rememberMeFalse_setsMaxAge_shortAbsoluteExpiryMs() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        cookieService.setRefreshTokenCookie("rt", resp, false);
        String setCookie = resp.getHeader("Set-Cookie");
        assertThat(setCookie).contains("Max-Age=" + (props.getShortAbsoluteExpiryMs() / 1000));
    }

    @Test
    void getAccessToken_throwsMissing_whenCookieAbsent() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertThatThrownBy(() -> cookieService.getAccessToken(req))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.MISSING);
    }

    @Test
    void getRefreshToken_throwsMissing_whenCookieAbsent() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertThatThrownBy(() -> cookieService.getRefreshToken(req))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.MISSING);
    }

    @Test
    void clearCookie_setsMaxAge0_onResponse() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        cookieService.clearCookie(TokenType.ACCESS_TOKEN, resp);
        String setCookie = resp.getHeader("Set-Cookie");
        assertThat(setCookie).contains("Max-Age=0");
    }

    @Test
    void getToken_returnsCorrectValue_whenMatchingCookiePresent() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie(TokenType.ACCESS_TOKEN.toString(), "abc"));

        assertThat(cookieService.getAccessToken(req)).isEqualTo("abc");
    }

    @Test
    void getToken_returnsNull_whenCookiesArrayNull() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        // cookies is null by default
        assertThatThrownBy(() -> cookieService.getAccessToken(req))
                .isInstanceOf(TokenException.class)
                .extracting(e -> ((TokenException) e).getReason())
                .isEqualTo(TokenFailureReason.MISSING);
    }
}

