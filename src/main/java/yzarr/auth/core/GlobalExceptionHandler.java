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
        return build(ex.getCode(), req);
    }

    private ResponseEntity<ErrorMessage> build(ErrorCode code, HttpServletRequest req) {
        int status = code.getStatus();
        return ResponseEntity.status(status).body(
                ErrorMessage.builder()
                        .status(status)
                        .code(code.name())
                        .message(code.getMessage())
                        .path(req.getRequestURI())
                        .build());
    }
}