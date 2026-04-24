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
    NO_REFRESH_TOKEN(401, "No refresh token provided"),
    INVALID_REFRESH_TOKEN(401, "Invalid refresh token"),
    NO_EMAIL_VERIFICATION_TOKEN(400, "No email verification token provided"),
    INVALID_EMAIL_VERIFICATION_TOKEN(400, "Invalid email verification token"),
    EMAIL_IS_ALREADY_VERIFIED(409, "Email is already verified"),
    NO_ACCESS_TOKEN(401, "No access token provided"),
    INVALID_ACCESS_TOKEN(401, "Invalid access token"),
    PENDING(202, "Try again later"),
    EXPIRED(400, "Token is expired"),
    NO_TOKEN(401, "No needed token was provided"),
    INVALID_TOKEN(401, "Provided token is invalid"),
    TODO(500, "TODO CODE");

    private final int status;
    private final String message;
}