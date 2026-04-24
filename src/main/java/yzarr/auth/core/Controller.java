package yzarr.auth.core;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.requests.LoginRequest;
import yzarr.auth.model.requests.RegisterRequest;
import yzarr.auth.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/auth")
@Slf4j
public class Controller {

    private final UserService userService;

    public Controller(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req) {
        userService.register(req.getEmail(), req.getPassword());
    }

    @PostMapping("/login")
    public void login(@RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        userService.login(req.getEmail(), req.getPassword(), req.isRememberMe(), request, response);
    }

    @PostMapping("/refresh")
    public void refresh(HttpServletResponse response, HttpServletRequest request) {
        userService.refresh(response, request);
    }

    @GetMapping("/verify/email")
    public void verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
    }

    @GetMapping("/verify/2fa")
    public void verify2FA(@RequestParam("token") String token) {
        userService.verify2fa(token);
    }

    @PostMapping("/verify/2fa/status")
    public void check2faStatus(HttpServletRequest request, HttpServletResponse response) {
        userService.check2faStatus(request, response);
    }

}
