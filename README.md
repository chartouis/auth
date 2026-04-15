### Currently WiP

# spring-boot-starter-auth

A plug-and-play authentication starter for Spring Boot applications. Drop it into your project and get JWT-based auth, OAuth2 login, and 2FA out of the box. 

---

## What it does

This starter auto-configures a full authentication layer into your Spring Boot app the moment it lands on your classpath. If you don't need OAuth or 2FA or even the auth itself, you can just turn it off by configuring application.properties file  

Out of the box you get:

- JWT-based stateless authentication (access + refresh token flow)
- OAuth2 login support
- Two-factor authentication
- Auto-configured security filter chain
