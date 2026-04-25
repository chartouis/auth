package yzarr.auth.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_CREDENTIALS(401, "Email or Password are invalid"),
    PASSWORD_IS_TOO_SHORT(400, "Password is too short"),
    EMAIL_ALREADY_EXISTS(409, "Email already exists"),
    INVALID_EMAIL_FORMAT(400, "Invalid email format"),
    EMAIL_IS_NOT_VERIFIED(403, "Email is not verified"),
    EMAIL_IS_ALREADY_VERIFIED(409, "Email is already verified"),
    PENDING(202, "Try again later"),
    EXPIRED(400, "Token is expired"),
    UNEXPECTED_ERROR(500, "This error means something bad happened on the backend"),
    TODO(500, "TODO CODE");

    private final int status;
    private final String message;
}