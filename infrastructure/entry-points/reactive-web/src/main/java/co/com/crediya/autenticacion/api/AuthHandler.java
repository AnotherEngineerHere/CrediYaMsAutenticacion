package co.com.crediya.autenticacion.api;

import co.com.crediya.autenticacion.api.dto.ErrorResponse;
import co.com.crediya.autenticacion.api.dto.LoginDTO;
import co.com.crediya.autenticacion.api.dto.LoginResponseDTO;
import co.com.crediya.autenticacion.api.security.JwtService;
import co.com.crediya.autenticacion.model.excepciones.ContrasenaVaciaException;
import co.com.crediya.autenticacion.model.excepciones.EmailInvalidoException;
import co.com.crediya.autenticacion.model.excepciones.EmailVacioException;
import co.com.crediya.autenticacion.usecase.excepciones.*;
import co.com.crediya.autenticacion.usecase.usuario.LoginUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.StringJoiner;

@Component
@RequiredArgsConstructor
public class AuthHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);

    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    // POST /api/v1/usuarios/login
    public Mono<ServerResponse> login(ServerRequest request) {
        log.info("Received login request from IP: {}", request.getRemoteAddress());
        
        return request.bodyToMono(LoginDTO.class)
                .doOnNext(dto -> log.debug("Login attempt for email: {}", dto.getCorreoElectronico()))
                .switchIfEmpty(Mono.error(new ContrasenaVaciaException("El cuerpo de la petición es requerido")))
                // Usa los getters REALES de tu LoginDTO (según dijiste: correo y contrasena)
                .flatMap(dto -> loginUseCase.execute(dto.getCorreoElectronico(), dto.getContrasena()))
                .doOnNext(res -> log.debug("User authenticated successfully: {}", res.email()))
                .flatMap(res -> {
                    // scope como string con espacios
                    StringJoiner joiner = new StringJoiner(" ");
                    res.accesos().forEach(joiner::add);
                    String scope = joiner.toString();

                    log.debug("Generating JWT for user: {} with role: {} and scopes: {}", res.email(), res.rol(), scope);

                    return jwtService.issue(res.email(), res.rol(), res.accesos())
                            .map(tokens -> new LoginResponseDTO(
                                    tokens.tokenType(),      // token_type
                                    tokens.accessToken(),    // access_token
                                    tokens.refreshToken(),   // refresh_token
                                    tokens.expiresIn(),      // expires_in
                                    res.email(),             // subject
                                    res.rol(),               // role
                                    scope                    // scope
                            ));
                })
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .doOnSuccess(response -> log.info("Login successful for user: {}", ((LoginResponseDTO) response.body()).subject()))

                // --------- Manejo de errores conocidos ----------
                .onErrorResume(EmailVacioException.class,
                        ex -> handleException(ex, "EMAIL_VACIO", log))
                .onErrorResume(EmailInvalidoException.class,
                        ex -> handleException(ex, "EMAIL_INVALIDO", log))
                .onErrorResume(ContrasenaVaciaException.class,
                        ex -> handleException(ex, "CONTRASENA_VACIA", log))
                .onErrorResume(UsuarioNoEncontradoException.class,
                        ex -> handleException(ex, "USUARIO_NO_ENCONTRADO", HttpStatus.NOT_FOUND, log))
                .onErrorResume(CredencialesInvalidasException.class,
                        ex -> handleException(ex, "CREDENCIALES_INVALIDAS", HttpStatus.UNAUTHORIZED, log))
                .onErrorResume(RoleNotFoundException.class,
                        ex -> handleException(ex, "ROL_NO_ENCONTRADO", HttpStatus.NOT_FOUND, log))
                .onErrorResume(RolNoPermitidoException.class,
                        ex -> handleException(ex, "ROL_NO_PERMITIDO", HttpStatus.FORBIDDEN, log))
                .onErrorResume(BadSqlGrammarException.class,
                        ex -> handleException(ex, "SQL_GRAMMAR", HttpStatus.INTERNAL_SERVER_ERROR, log))
                .onErrorResume(DataAccessException.class,
                        ex -> handleException(ex, "DATA_ACCESS", HttpStatus.INTERNAL_SERVER_ERROR, log))
                // Fallback genérico
                .onErrorResume(ex -> handleGenericException(ex, log));

    }

    // Enhanced error handling with logging
    private Mono<ServerResponse> handleException(Exception ex, String code, Logger log) {
        log.warn("Bad Request - {}: {}", code, ex.getMessage());
        return badRequest(code, ex.getMessage());
    }

    private Mono<ServerResponse> handleException(Exception ex, String code, HttpStatus status, Logger log) {
        log.warn("{} - {}: {}", status, code, ex.getMessage());
        return status(status, code, ex.getMessage());
    }

    private Mono<ServerResponse> handleGenericException(Throwable ex, Logger log) {
        log.error("Unexpected error during login", ex);
        return status(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Contacte con el administrador.");
    }

    // Helpers
    private Mono<ServerResponse> badRequest(String code, String msg) {
        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ErrorResponse.of(code, msg));
    }

    private Mono<ServerResponse> status(HttpStatus status, String code, String msg) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ErrorResponse.of(code, msg));
    }
}
