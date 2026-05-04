package yzarr.auth.core.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yzarr.auth.service.AuthOrchestrator;

@RestController
@RequestMapping("/auth/verify/2fa")
@ConditionalOnProperty(name = "yzarr.auth.two-fa")
public class TwoFactorVerificationController {

    private final AuthOrchestrator authOrchestrator;

    public TwoFactorVerificationController(AuthOrchestrator authOrchestrator) {
        this.authOrchestrator = authOrchestrator;
    }

    @GetMapping
    public void verify2FA(@RequestParam("token") String token) {
        authOrchestrator.verify2fa(token);
    }

    @PostMapping("/status")
    public void check2faStatus(HttpServletRequest request, HttpServletResponse response) {
        authOrchestrator.check2faStatus(request, response);
    }
}