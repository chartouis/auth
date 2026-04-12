package yzarr.auth;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;

import yzarr.auth.model.UserPrincipal;

public class Util {
    UUID getCurrentUserUUID() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
