### Currently WiP

# spring-boot-starter-auth

A plug-and-play authentication starter for Spring Boot applications. Drop it into your project, implement one interface, and get JWT-based auth, OAuth2 login, and optional 2FA out of the box — with near-zero boilerplate.

---

## What it does

This starter auto-configures a full authentication layer into your Spring Boot app the moment it lands on your classpath. It follows the same convention-over-configuration philosophy as the rest of the Spring Boot ecosystem: sensible defaults that work immediately, with clean extension points when you need to customize.

Out of the box you get:

- JWT-based stateless authentication (access + refresh token flow)
- OAuth2 login support
- Optional two-factor authentication
- Auto-configured security filter chain
- A simple interface to plug in your own user persistence layer
