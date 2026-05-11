package yzarr.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.util.DefaultUriBuilderFactory;

import yzarr.auth.model.requests.LoginRequest;
import yzarr.auth.model.requests.RegisterRequest;

class AuthFlowIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    int port;

    private RestTemplate rest() {
        RestTemplate rt = new RestTemplate();
        rt.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + port));
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
        return rt;
    }

    private static String cookieHeaderValue(String setCookieHeader) {
        int semi = setCookieHeader.indexOf(';');
        return (semi >= 0) ? setCookieHeader.substring(0, semi) : setCookieHeader;
    }

    @Test
    void register_happyPath_200() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user1@test.com");
        req.setPassword("Password123!");

        ResponseEntity<Void> resp = rest().postForEntity("/auth/register", req, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void register_duplicateEmail_errors() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@test.com");
        req.setPassword("Password123!");
        rest().postForEntity("/auth/register", req, Void.class);

        ResponseEntity<String> resp2 = rest().postForEntity("/auth/register", req, String.class);
        assertThat(resp2.getStatusCode().isError()).isTrue();
    }

    @Test
    void register_shortPassword_errors() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("shortpw@test.com");
        req.setPassword("short");

        ResponseEntity<String> resp = rest().postForEntity("/auth/register", req, String.class);
        assertThat(resp.getStatusCode().isError()).isTrue();
    }

    @Test
    void register_invalidEmail_errors() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("not-an-email");
        req.setPassword("Password123!");

        ResponseEntity<String> resp = rest().postForEntity("/auth/register", req, String.class);
        assertThat(resp.getStatusCode().isError()).isTrue();
    }

    @Test
    void login_happyPath_setsAccessAndRefreshCookies() {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("login@test.com");
        reg.setPassword("Password123!");
        rest().postForEntity("/auth/register", reg, Void.class);

        LoginRequest login = new LoginRequest();
        login.setEmail("login@test.com");
        login.setPassword("Password123!");
        login.setRememberMe(true);

        ResponseEntity<Void> resp = rest().postForEntity("/auth/login", login, Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull();
        assertThat(resp.getHeaders().get(HttpHeaders.SET_COOKIE).toString())
                .contains("ACCESS_TOKEN")
                .contains("REFRESH_TOKEN");
    }

    @Test
    void login_wrongPassword_errors() {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("wrongpw@test.com");
        reg.setPassword("Password123!");
        rest().postForEntity("/auth/register", reg, Void.class);

        LoginRequest login = new LoginRequest();
        login.setEmail("wrongpw@test.com");
        login.setPassword("wrong");

        ResponseEntity<String> resp = rest().postForEntity("/auth/login", login, String.class);
        assertThat(resp.getStatusCode().isError()).isTrue();
    }

    @Test
    void login_unknownEmail_errors() {
        LoginRequest login = new LoginRequest();
        login.setEmail("unknown@test.com");
        login.setPassword("Password123!");

        ResponseEntity<String> resp = rest().postForEntity("/auth/login", login, String.class);
        assertThat(resp.getStatusCode().isError()).isTrue();
    }

    @Test
    void refresh_happyPath_newAccessCookieSet() {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("refresh@test.com");
        reg.setPassword("Password123!");
        rest().postForEntity("/auth/register", reg, Void.class);

        LoginRequest login = new LoginRequest();
        login.setEmail("refresh@test.com");
        login.setPassword("Password123!");
        login.setRememberMe(true);

        ResponseEntity<Void> loginResp = rest().postForEntity("/auth/login", login, Void.class);
        String refreshCookie = loginResp.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .filter(h -> h.contains("REFRESH_TOKEN"))
                .findFirst()
                .orElseThrow();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeaderValue(refreshCookie));
        ResponseEntity<Void> refreshResp = rest().exchange("/auth/refresh", HttpMethod.POST, new HttpEntity<>(headers),
                Void.class);

        assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResp.getHeaders().get(HttpHeaders.SET_COOKIE).toString()).contains("ACCESS_TOKEN");
    }

    @Test
    void refresh_noCookie_errors() {
        ResponseEntity<String> resp = rest().postForEntity("/auth/refresh", null, String.class);
        assertThat(resp.getStatusCode().isError()).isTrue();
    }

    @Test
    void logout_clearsCookies_maxAge0() {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("logout@test.com");
        reg.setPassword("Password123!");
        rest().postForEntity("/auth/register", reg, Void.class);

        LoginRequest login = new LoginRequest();
        login.setEmail("logout@test.com");
        login.setPassword("Password123!");
        login.setRememberMe(true);

        ResponseEntity<Void> loginResp = rest().postForEntity("/auth/login", login, Void.class);
        String refreshCookie = loginResp.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .filter(h -> h.contains("REFRESH_TOKEN"))
                .findFirst()
                .orElseThrow();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeaderValue(refreshCookie));
        ResponseEntity<Void> logoutResp = rest().exchange("/auth/refresh/logout", HttpMethod.POST, new HttpEntity<>(headers),
                Void.class);

        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logoutResp.getHeaders().get(HttpHeaders.SET_COOKIE).toString()).contains("Max-Age=0");
    }

    @Test
    void protectedEndpoint_unauthorized_withoutAccessCookie() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = rest().exchange("/api/test", HttpMethod.POST, new HttpEntity<>("{}", headers),
                String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_ok_withAccessCookie() {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("protected@test.com");
        reg.setPassword("Password123!");
        rest().postForEntity("/auth/register", reg, Void.class);

        LoginRequest login = new LoginRequest();
        login.setEmail("protected@test.com");
        login.setPassword("Password123!");
        login.setRememberMe(true);
        ResponseEntity<Void> loginResp = rest().postForEntity("/auth/login", login, Void.class);

        String accessCookie = loginResp.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .filter(h -> h.contains("ACCESS_TOKEN"))
                .findFirst()
                .orElseThrow();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeaderValue(accessCookie));
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Void> resp = rest().exchange("/api/test", HttpMethod.POST, new HttpEntity<>("{}", headers),
                Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void fullAuthChain_register_login_refresh_logout_refreshFails() {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("chain@test.com");
        reg.setPassword("Password123!");
        assertThat(rest().postForEntity("/auth/register", reg, Void.class).getStatusCode()).isEqualTo(HttpStatus.OK);

        LoginRequest login = new LoginRequest();
        login.setEmail("chain@test.com");
        login.setPassword("Password123!");
        login.setRememberMe(true);
        ResponseEntity<Void> loginResp = rest().postForEntity("/auth/login", login, Void.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        String refreshCookie = loginResp.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .filter(h -> h.contains("REFRESH_TOKEN"))
                .findFirst()
                .orElseThrow();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeaderValue(refreshCookie));

        ResponseEntity<Void> refreshResp = rest().exchange("/auth/refresh", HttpMethod.POST, new HttpEntity<>(headers),
                Void.class);
        assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        String rotatedRefreshCookie = refreshResp.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .filter(h -> h.contains("REFRESH_TOKEN"))
                .findFirst()
                .orElse(refreshCookie);
        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.add(HttpHeaders.COOKIE, cookieHeaderValue(rotatedRefreshCookie));

        ResponseEntity<Void> logoutResp = rest().exchange("/auth/refresh/logout", HttpMethod.POST,
                new HttpEntity<>(logoutHeaders),
                Void.class);
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> refreshAfterLogout = rest().exchange("/auth/refresh", HttpMethod.POST,
                new HttpEntity<>(logoutHeaders),
                String.class);
        assertThat(refreshAfterLogout.getStatusCode().isError()).isTrue();
    }
}

