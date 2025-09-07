package co.com.crediya.autenticacion.model.usuario.gateways;

import reactor.core.publisher.Mono;

/**
 * Puerto de dominio para manejo de contraseñas.
 * Permite cambiar de algoritmo (BCrypt, Argon2, PBKDF2) sin tocar los casos de uso.
 */
public interface PasswordService {
    Mono<Boolean> matches(String raw, String encoded);
    Mono<String> encode(String raw);
}
