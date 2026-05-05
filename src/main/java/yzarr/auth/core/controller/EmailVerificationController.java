package yzarr.auth.core.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import yzarr.auth.model.requests.PasswordResetRequest;
import yzarr.auth.service.AuthOrchestrator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth/verify")
@ConditionalOnProperty(name = "yzarr.auth.email-verification")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final AuthOrchestrator authOrchestrator;

    @GetMapping("/email")
    public void verifyEmail(@RequestParam("token") String token) {
        authOrchestrator.verifyEmail(token);
    }

    @PostMapping("/reset/request")
    public void requestPasswordReset(@RequestParam("email") String email) {
        authOrchestrator.requestPasswordReset(email);
    }

    @PostMapping("/reset/confirm")
    public void postMethodName(@RequestBody PasswordResetRequest req) {
        authOrchestrator.confirmPasswordReset(req.getToken(), req.getNewPassword());
    }

}