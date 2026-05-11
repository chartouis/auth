package yzarr.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import yzarr.auth.service.AuthOrchestrator;
import yzarr.auth.service.JwtService;
import yzarr.auth.service.TokenService;
import testinfra.TestJpaInfra;

class StarterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    StarterConfiguration.class))
            .withUserConfiguration(TestJpaInfra.class)
            .withInitializer((ApplicationContextInitializer<ConfigurableApplicationContext>) ctx -> ((GenericApplicationContext) ctx)
                    .getDefaultListableBeanFactory()
                    .setAllowBeanDefinitionOverriding(true))
            .withPropertyValues(
                    // required for JwtService usage at runtime (but bean creation doesn't decode)
                    "yzarr.auth.jwt-secret=dGVzdHNlY3JldGtleXRlc3RzZWNyZXRrZXl0ZXN0c2VjcmV0a2V5dGVzdA==",
                    "yzarr.auth.frontend-url=http://localhost:3000",
                    "yzarr.auth.secure=false");

    @Test
    void contextLoads_andCoreBeansPresent_andDefaultsOk() {
        contextRunner.run(ctx -> {
            assertThat(ctx.getStartupFailure()).as("startup failure").isNull();
            assertThat(ctx).hasSingleBean(AuthOrchestrator.class);
            assertThat(ctx).hasSingleBean(JwtService.class);
            assertThat(ctx).hasSingleBean(TokenService.class);

            AuthProperties props = ctx.getBean(AuthProperties.class);
            assertThat(props.getSmtpHost()).isEqualTo("smtp.gmail.com");
            assertThat(props.getSmtpPort()).isEqualTo(587);
            assertThat(props.isMailDebug()).isFalse();
        });
    }
}

