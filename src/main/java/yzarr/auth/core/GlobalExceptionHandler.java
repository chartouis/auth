package yzarr.auth.core;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import yzarr.auth.model.AuthException;
import yzarr.auth.model.enums.ErrorCode;
import yzarr.auth.model.response.ErrorMessage;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorMessage> handleAuth(AuthException ex, HttpServletRequest req) {
        return fromCode(ex.getCode(), req);
    }

    private ResponseEntity<ErrorMessage> fromCode(ErrorCode code, HttpServletRequest req) {
        switch (code) {
            case INVALID_CREDENTIALS:
                return ResponseEntity.status(401).body(
                        ErrorMessage.builder()
                                .status(401)
                                .code(code.toString())
                                .message("Email or Password are invalid")
                                .path(req.getRequestURI())
                                .build());

            case PASSWORD_IS_TOO_SHORT:
                return ResponseEntity.status(400).body(
                        ErrorMessage.builder()
                                .status(400)
                                .code(code.toString())
                                .message("Password is too short")
                                .path(req.getRequestURI())
                                .build());

            case EMAIL_ALREADY_EXISTS:
                return ResponseEntity.status(409).body(
                        ErrorMessage.builder()
                                .status(409)
                                .code(code.toString())
                                .message("Email already exists")
                                .path(req.getRequestURI())
                                .build());

            case INVALID_EMAIL_FORMAT:
                return ResponseEntity.status(400).body(
                        ErrorMessage.builder()
                                .status(400)
                                .code(code.toString())
                                .message("Invalid email format")
                                .path(req.getRequestURI())
                                .build());

            default:
                return ResponseEntity.status(500).body(
                        ErrorMessage.builder()
                                .status(500)
                                .code("UNKNOWN_ERROR")
                                .message("Unexpected error")
                                .path(req.getRequestURI())
                                .build());
        }
    }
}
