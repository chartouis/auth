package yzarr.auth.core.controller;

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

@RestController
@RequestMapping("/auth")
@Slf4j
public class MainController {

    private final AuthOrchestrator authOrchestrator;

    public MainController(AuthOrchestrator authOrchestrator) {
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

}
