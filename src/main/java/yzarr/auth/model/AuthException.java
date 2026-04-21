package yzarr.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yzarr.auth.model.enums.ErrorCode;

@Getter
@AllArgsConstructor
public class AuthException extends RuntimeException {
    private final ErrorCode code;
}
