package yzarr.auth.config;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import yzarr.auth.model.requests.LoginRequest;
import yzarr.auth.model.requests.RegisterRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    public void login(@RequestBody LoginRequest req, HttpServletResponse response) {
        userService.login(req.getEmail(), req.getPassword(), req.isRememberMe(), response);
    }

    @PostMapping("/refresh")
    public String refresh(@RequestBody String entity) {

        return entity;
    }

}
