package yzarr.auth.model.requests;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String token;
    private String newPassword;
}
