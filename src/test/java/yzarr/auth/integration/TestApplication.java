package yzarr.auth.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import yzarr.auth.StarterConfiguration;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(StarterConfiguration.class)
public class TestApplication {
}

