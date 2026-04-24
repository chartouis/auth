package yzarr.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yzarr.auth.model.enums.TokenFailureReason;
import yzarr.auth.model.enums.TokenType;

@Getter
@AllArgsConstructor
public class TokenException extends RuntimeException {
    private final TokenType type;
    private final TokenFailureReason reason;
}
