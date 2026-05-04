package yzarr.auth.core.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import yzarr.auth.service.AuthOrchestrator;

@RestController
@RequestMapping("/auth/verify/email")
@ConditionalOnProperty(name = "yzarr.auth.email-verification")
public class EmailVerificationController {

    private final AuthOrchestrator authOrchestrator;

    public EmailVerificationController(AuthOrchestrator authOrchestrator) {
        this.authOrchestrator = authOrchestrator;
    }

    @GetMapping
    public void verifyEmail(@RequestParam("token") String token) {
        authOrchestrator.verifyEmail(token);
    }
}