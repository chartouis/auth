package yzarr.auth.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenFailureReason {
    MISSING(401, "Token is missing"),
    INVALID(401, "Token is invalid"),
    EXPIRED(401, "Token is expired"),
    PENDING(202, "Try again later"),
    ALREADY_CONSUMED(409, "Token already consumed"),
    ALREADY_REVOKED(409, "Token already revoked");

    private final int status;
    private final String message;
}