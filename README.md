# yzarr-spring-boot-starter-auth

A plug-and-play authentication starter for Spring Boot applications. Drop it into your project and get JWT-based auth, OAuth2 login, and 2FA out of the box.

> **Status:** Work in Progress

---

## What it does

This starter auto-configures a full authentication layer into your Spring Boot app the moment it lands on your classpath. If you don't need OAuth, 2FA, or even auth at all, you can turn any of it off via `application.properties`.

Out of the box you get:

- JWT-based stateless authentication (access + refresh token flow)
- Refresh token rotation
- Email verification flow
- Two-factor authentication via email
- Password reset via email
- Change password (authenticated)
- OAuth2 / OIDC login (Google, etc.)
- Auto-configured security filter chain
- Cookie-based token transport (HttpOnly, Secure, SameSite)

---

## Installation

### 1. Add to your consumer app's `build.gradle`

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.postgresql:postgresql' // or your DB driver of choice
    implementation 'yzarr:yzarr-spring-boot-starter-auth:0.0.1'
}
```

### 2. Exclude Spring Boot's default security auto-configuration

The starter owns the security filter chain. Boot's default one conflicts with it, so exclude it in your main class:

```java
@SpringBootApplication(exclude = {
    org.springframework.boot.security.autoconfigure.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration.class,
    org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration.class
})
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. Configure `application.properties`

At minimum, you need:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/yourdb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update

# Required auth properties
yzarr.auth.jwt-secret=your-base64-encoded-secret-here
yzarr.auth.frontend-url=http://localhost:5173
```

The starter creates its own tables (`users`, `refresh_tokens`, `verification_tokens`) via Hibernate on startup.

---

## Configuration reference

All properties are prefixed with `yzarr.auth`.

### Feature toggles

| Property                 | Default | Description                                                                    |
| ------------------------ | ------- | ------------------------------------------------------------------------------ |
| `enable-auth`            | `true`  | Master switch. Set to `false` to permit all requests (useful in dev)           |
| `email-verification`     | `false` | Require email verification before login                                        |
| `two-fa`                 | `false` | Enable two-factor authentication via email. Requires `email-verification=true` |
| `oauth`                  | `false` | Enable OAuth2 / OIDC login                                                     |
| `refresh-token-rotation` | `true`  | Issue a new refresh token on every `/auth/refresh` call                        |

### Token expiry

| Property                             | Default                | Description                                                       |
| ------------------------------------ | ---------------------- | ----------------------------------------------------------------- |
| `access-token-expiry-ms`             | `900000` (15 min)      | Access token lifetime                                             |
| `refresh-token-expiry-ms`            | `2592000000` (30 days) | Refresh token lifetime when rememberMe=true                       |
| `short-absolute-expiry-ms`           | `3600000` (1 hour)     | Refresh token lifetime when rememberMe=false                      |
| `absolute-expiry-ms`                 | `7776000000` (90 days) | Hard cap on refresh token lifetime regardless of rotation         |
| `email-verification-token-expiry-ms` | `3600000` (1 hour)     | Email verification link lifetime                                  |
| `password-reset-token-expiry-ms`     | `300000` (5 min)       | Password reset link lifetime                                      |
| `two-factor-token-expiry-ms`         | `600000` (10 min)      | 2FA token lifetime                                                |
| `challenge-token-expiry-ms`          | `600000` (10 min)      | 2FA challenge cookie lifetime                                     |
| `token-send-cooldown-ms`             | `60000` (1 min)        | Minimum time between sending the same token type to the same user |
| `refresh-cooldown-ms`                | `300000` (5 min)       | Minimum time between refresh token rotations                      |

### Security

| Property              | Default     | Description                                                                          |
| --------------------- | ----------- | ------------------------------------------------------------------------------------ |
| `jwt-secret`          | `"default"` | **Required.** Base64-encoded HMAC secret for signing JWTs. Must be at least 256 bits |
| `min-password-length` | `8`         | Minimum accepted password length                                                     |

### Cookies

| Property    | Default    | Description                                                                |
| ----------- | ---------- | -------------------------------------------------------------------------- |
| `http-only` | `true`     | Set HttpOnly flag on auth cookies                                          |
| `secure`    | `true`     | Set Secure flag on auth cookies. Set to `false` for local HTTP development |
| `same-site` | `"Strict"` | SameSite cookie policy (`Strict`, `Lax`, `None`)                           |

### CORS & URLs

| Property             | Default | Description                                                                           |
| -------------------- | ------- | ------------------------------------------------------------------------------------- |
| `frontend-url`       | `""`    | **Required.** Allowed CORS origin. Also used as OAuth redirect base                   |
| `oauth-redirect-url` | `""`    | Where to redirect after a successful OAuth login. Defaults to `frontend-url` if blank |

### SMTP (required when email-verification or two-fa is enabled)

| Property       | Default               | Description                                             |
| -------------- | --------------------- | ------------------------------------------------------- |
| `app-username` | `"default@gmail.com"` | SMTP sender address                                     |
| `app-password` | `"default"`           | SMTP password / app password                            |
| `smtp-host`    | `"smtp.gmail.com"`    | SMTP server host                                        |
| `smtp-port`    | `587`                 | SMTP server port                                        |
| `mail-debug`   | `false`               | Log full SMTP conversation (never enable in production) |

### OAuth2 (required when oauth=true)

Configure Spring's standard OAuth2 client properties alongside the starter's:

```properties
yzarr.auth.oauth=true
yzarr.auth.oauth-redirect-url=http://localhost:5173/dashboard

spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,email,profile
```

---

## Endpoints

All auth endpoints are under `/auth/**` and are public. The `/api/**` endpoints require a valid `ACCESS_TOKEN` cookie.

### Always available

| Method | Path                   | Body                                     | Cookies in      | Cookies out                                                | Description                                  |
| ------ | ---------------------- | ---------------------------------------- | --------------- | ---------------------------------------------------------- | -------------------------------------------- |
| `POST` | `/auth/register`       | `{ "email", "password" }`                | —               | —                                                          | Create a new account                         |
| `POST` | `/auth/login`          | `{ "email", "password", "rememberMe" }`  | —               | `ACCESS_TOKEN`, `REFRESH_TOKEN`                            | Log in and receive tokens                    |
| `POST` | `/auth/refresh`        | —                                        | `REFRESH_TOKEN` | `ACCESS_TOKEN` (+ new `REFRESH_TOKEN` if rotation enabled) | Refresh the access token                     |
| `POST` | `/auth/refresh/logout` | —                                        | `REFRESH_TOKEN` | Clears `ACCESS_TOKEN`, `REFRESH_TOKEN`                     | Log out and revoke refresh token             |
| `POST` | `/api/password`        | `{ "email", "password", "newPassword" }` | `REFRESH_TOKEN` | —                                                          | Change password (revokes all other sessions) |

### When `email-verification=true`

| Method | Path                           | Params / Body                | Description                                   |
| ------ | ------------------------------ | ---------------------------- | --------------------------------------------- |
| `GET`  | `/auth/verify/email`           | `?token=...`                 | Verify email using the link from the email    |
| `POST` | `/auth/verify/email`           | `?email=...`                 | Resend email verification link                |
| `POST` | `/auth/password/reset/request` | `?email=...`                 | Send a password reset email                   |
| `POST` | `/auth/password/reset/confirm` | `{ "token", "newPassword" }` | Confirm password reset using token from email |

### When `two-fa=true`

Login no longer returns tokens directly. Instead:

| Method | Path                      | Params / Body | Cookies in  | Cookies out                     | Description                                                                                                                           |
| ------ | ------------------------- | ------------- | ----------- | ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| `GET`  | `/auth/verify/2fa`        | `?token=...`  | —           | —                               | Verify 2FA using the token from the email (visited by the user in their email client)                                                 |
| `POST` | `/auth/verify/2fa/status` | —             | `CHALLENGE` | `ACCESS_TOKEN`, `REFRESH_TOKEN` | Poll this after login to check if the user has clicked the email link. Returns 200 with tokens when approved, 202 while still pending |

---

## How the pipeline works

Every auth operation is a chain of small, single-purpose stages. Each stage receives an `AuthContext` (carrying email, password, user, tokens, request/response, and props), does one thing, and passes it to the next stage. A stage can call `context.stop()` to halt the chain early.

This makes the flow easy to follow in `AuthPipelineFactory`. A few examples:

**Register**

```
ValidEmail → ValidPassword → CreateAccount
```

**Login (no extras)**

```
ValidEmail → ValidPassword → Authenticate → IssueRefreshToken → IssueAccessToken
```

**Login (with email verification + 2FA)**

```
ValidEmail → ValidPassword → Authenticate → CheckEmailVerified
         → CheckCooldown → SendChallengeEmail
         (tokens are NOT issued here — user must complete 2FA first)
```

**Refresh (with rotation)**

```
VerifyRefreshToken → IssueAccessToken → RotateRefreshToken
```

**Change password**

```
VerifyRefreshToken → ValidEmail → ValidPassword → VerifyCurrentPassword
                  → ChangePassword → RevokeAllRefreshTokens
```

**Password reset (email flow)**

```
Request:  ValidEmail → CheckCooldown → SendPasswordResetEmail
Confirm:  VerifyResetToken → ChangePassword → RevokeAllRefreshTokens
```

**2FA status check**

```
ConsumeChallenge → IssueAccessToken → IssueRefreshToken
```

---

## Error responses

All errors follow this structure:

```json
{
  "status": 401,
  "code": "INVALID_CREDENTIALS",
  "message": "Email or Password are invalid",
  "path": "/auth/login"
}
```

| Code                        | Status | Meaning                                               |
| --------------------------- | ------ | ----------------------------------------------------- |
| `INVALID_CREDENTIALS`       | 401    | Wrong email or password                               |
| `PASSWORD_IS_TOO_SHORT`     | 400    | Password below minimum length                         |
| `INVALID_CHARACTERS`        | 400    | Password contains disallowed characters               |
| `INVALID_EMAIL_FORMAT`      | 400    | Email doesn't look like an email                      |
| `EMAIL_ALREADY_EXISTS`      | 409    | Registration with an existing email                   |
| `EMAIL_IS_NOT_VERIFIED`     | 403    | Login attempted before verifying email                |
| `EMAIL_IS_ALREADY_VERIFIED` | 409    | Verification attempted on an already-verified account |
| `EMAIL_ALREADY_SENT`        | 429    | Token resend attempted within the cooldown window     |
| `TOKEN_ROTATION_COOLDOWN`   | 429    | Refresh rotation attempted too soon                   |
| `PENDING`                   | 202    | 2FA email not yet confirmed                           |
| `UNEXPECTED_ERROR`          | 500    | Something went wrong on the server                    |

Token errors additionally carry the token type in the message (e.g. `REFRESH_TOKEN token: EXPIRED`).

---

## Accessing the authenticated user

Inside any protected endpoint, get the current user from the security context the standard Spring Security way:

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import yzarr.auth.model.UserPrincipal;

@GetMapping("/me")
public String me(@AuthenticationPrincipal UserPrincipal principal) {
    return principal.getUsername(); // this is the user's UUID
}
```

Or via `SecurityContextHolder`:

```java
UserPrincipal principal = (UserPrincipal) SecurityContextHolder
        .getContext().getAuthentication().getPrincipal();
```

---

## Notes

- The JWT subject is the user's UUID, not their email. Email can change; UUID cannot.
- `rememberMe=false` at login gives a 1-hour session. `rememberMe=true` gives a 30-day sliding window with a 90-day absolute hard cap.
- All tokens are stored hashed (SHA-256) in the database. Raw token values never persist.
- Refresh token rotation inherits the original token's `absoluteExpiry`, so rotating doesn't extend the hard cap.
- When 2FA is enabled, `email-verification` must also be `true`.
