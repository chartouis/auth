package yzarr.auth.config;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController()
@RequestMapping("/auth")
public class Controller {
    @PostMapping("/register")
    public String register(@RequestBody String entity) {

        return entity;
    }

    @PostMapping("/login")
    public String login(@RequestBody String entity) {

        return entity;
    }

    @PostMapping("/refresh")
    public String refresh(@RequestBody String entity) {

        return entity;
    }

}
