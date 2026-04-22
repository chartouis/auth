package yzarr.auth.model.enums;

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
    INVALID_ACCESS_TOKEN(401, "Invalid access token");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}