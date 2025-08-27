package co.com.crediya.autenticacion.api.dto;

import java.time.OffsetDateTime;

public record ErrorResponse(String code, String message, String timestamp) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, OffsetDateTime.now().toString());
    }
}
