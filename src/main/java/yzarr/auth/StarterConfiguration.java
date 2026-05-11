package yzarr.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableConfigurationProperties(AuthProperties.class)
@AutoConfigurationPackage(basePackages = "yzarr.auth")
@ComponentScan(basePackages = "yzarr.auth")
@EnableJpaRepositories(basePackages = "yzarr.auth.repo")
@EntityScan(basePackages = "yzarr.auth.model")
public class StarterConfiguration {

}
