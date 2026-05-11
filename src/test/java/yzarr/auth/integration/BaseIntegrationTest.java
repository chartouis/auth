package yzarr.auth.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("yzarr.auth.jwt-secret",
                () -> "dGVzdHNlY3JldGtleXRlc3RzZWNyZXRrZXl0ZXN0c2Vj");

        registry.add("yzarr.auth.frontend-url",
                () -> "http://localhost:3000");

        registry.add("yzarr.auth.secure",
                () -> "false");

        registry.add("yzarr.auth.app-username",
                () -> "test@test.com");

        registry.add("yzarr.auth.app-password",
                () -> "testpassword");

        // avoid cooldowns interfering with auth flow tests
        registry.add("yzarr.auth.refresh-cooldown-ms", () -> "0");
        registry.add("yzarr.auth.token-send-cooldown-ms", () -> "0");
    }
}

