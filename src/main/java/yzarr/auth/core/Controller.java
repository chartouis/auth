package yzarr.auth.core;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.requests.LoginRequest;
import yzarr.auth.model.requests.RegisterRequest;
import yzarr.auth.service.AuthOrchestrator;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/auth")
@Slf4j
public class Controller {

    private final AuthOrchestrator authOrchestrator;

    public Controller(AuthOrchestrator authOrchestrator) {
        this.authOrchestrator = authOrchestrator;
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req) {
        authOrchestrator.register(req.getEmail(), req.getPassword());
    }

    @PostMapping("/login")
    public void login(@RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        authOrchestrator.login(req.getEmail(), req.getPassword(), req.isRememberMe(), request, response);
    }

    @PostMapping("/refresh")
    public void refresh(HttpServletResponse response, HttpServletRequest request) {
        authOrchestrator.refresh(response, request);
    }

    @PostMapping("/refresh/logout")
    public void logout(HttpServletResponse response, HttpServletRequest request) {
        authOrchestrator.logout(response, request);
    }

    @GetMapping("/verify/email")
    public void verifyEmail(@RequestParam("token") String token) {
        authOrchestrator.verifyEmail(token);
    }

    @GetMapping("/verify/2fa")
    public void verify2FA(@RequestParam("token") String token) {
        authOrchestrator.verify2fa(token);
    }

    @PostMapping("/verify/2fa/status")
    public void check2faStatus(HttpServletRequest request, HttpServletResponse response) {
        authOrchestrator.check2faStatus(request, response);
    }

}
