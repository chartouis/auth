package yzarr.auth.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import yzarr.auth.model.requests.ChangePasswordRequest;
import yzarr.auth.service.AuthOrchestrator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final AuthOrchestrator authOrchestrator;

    @PostMapping("/password")
    public void changePassword(@RequestBody ChangePasswordRequest req, HttpServletRequest request) {
        authOrchestrator.changePassword(req.getEmail(), req.getPassword(), req.getNewPassword(), request);
    }

    @PostMapping("/test")
    public void test() {
    }

}
