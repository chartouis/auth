package yzarr.auth.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorMessage {
    private int status;
    private String code;
    private String message;
    private String path;
}
