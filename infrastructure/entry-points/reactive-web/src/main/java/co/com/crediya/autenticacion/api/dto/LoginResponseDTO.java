package co.com.crediya.autenticacion.api.dto;

public record LoginResponseDTO(
        String token_type,
        String access_token,
        String refresh_token,
        long expires_in,
        String subject,
        String role,
        String scope
) {}
