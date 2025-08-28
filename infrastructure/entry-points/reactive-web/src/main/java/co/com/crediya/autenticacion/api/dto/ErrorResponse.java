package co.com.crediya.autenticacion.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Respuesta de error")
public record ErrorResponse(
    @Schema(description = "Código de error", example = "EMAIL_DUPLICADO")
    String code, 
    
    @Schema(description = "Mensaje descriptivo del error", example = "El email ya se encuentra registrado")
    String message, 
    
    @Schema(description = "Timestamp del error", example = "2024-01-15T10:30:00Z")
    String timestamp
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, OffsetDateTime.now().toString());
    }
}
