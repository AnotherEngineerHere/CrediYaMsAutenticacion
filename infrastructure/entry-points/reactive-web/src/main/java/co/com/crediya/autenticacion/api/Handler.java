package co.com.crediya.autenticacion.api;

import co.com.crediya.autenticacion.api.dto.*;
import co.com.crediya.autenticacion.api.exception.ContrasenaVaciaException;
import co.com.crediya.autenticacion.api.exception.CredencialesInvalidasException;
import co.com.crediya.autenticacion.api.exception.RolNoPermitidoException;
import co.com.crediya.autenticacion.api.exception.UsuarioNoEncontradoException;
import co.com.crediya.autenticacion.api.mapper.UsuarioMapper;
import co.com.crediya.autenticacion.api.security.JwtService;
import co.com.crediya.autenticacion.model.excepciones.*;
import co.com.crediya.autenticacion.usecase.usuario.LoginUseCase;
import co.com.crediya.autenticacion.usecase.usuario.UsuarioUseCase;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import javax.management.relation.RoleNotFoundException;

/**
 * Handler encargado de procesar las solicitudes HTTP relacionadas con la
 * entidad Usuario.
 * <p>
 * Actúa como capa de entrada (API) dentro de la arquitectura hexagonal,
 * delegando la lógica de negocio al {@link UsuarioUseCase}.
 * <br>
 * Implementado con el enfoque funcional de Spring WebFlux.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class Handler {

    private static final Logger log = LoggerFactory.getLogger(Handler.class);

    private final UsuarioUseCase usuarioUseCase;
    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    /**
     * Maneja la creación de un nuevo usuario en el sistema.
     *
     * @param request cuerpo de la petición con datos del usuario.
     * @return {@link ServerResponse} con estado 201 Created y datos del usuario
     *         creado.
     */
    public Mono<ServerResponse> saveUser(ServerRequest request) {
        log.info("Received user registration request from IP: {}", request.getRemoteAddress());
        
        return request.bodyToMono(CreateUserDTO.class)
                        .doOnNext(dto -> log.debug("User registration payload: {}", dto))
                        .map(UsuarioMapper::toDomain)
                        .doOnNext(u -> log.debug("Mapped to domain user: {}", u.getEmail()))
                        .flatMap(usuarioUseCase::save)
                        .doOnSuccess(saved -> log.info("User registered successfully: {}", saved.getEmail()))
                        .flatMap(saved -> {
                                URI location = URI.create("/api/v1/usuarios/" + saved.getEmail());
                                log.debug("User location header: {}", location);
                                return ServerResponse.created(location)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(CreateUserResponse.from(saved));
                        })
                        .onErrorResume(NombreVacioException.class, ex -> handleException(ex, "NOMBRE_VACIO", log))
                        .onErrorResume(NombreLongitudInvalidaException.class, ex -> handleException(ex, "NOMBRE_LONGITUD_INVALIDA", log))
                        .onErrorResume(ApellidoVacioException.class, ex -> handleException(ex, "APELLIDO_VACIO", log))
                        .onErrorResume(ApellidoLongitudInvalidaException.class, ex -> handleException(ex, "APELLIDO_LONGITUD_INVALIDA", log))
                        .onErrorResume(EmailVacioException.class, ex -> handleException(ex, "EMAIL_VACIO", log))
                        .onErrorResume(EmailDuplicadoException.class, ex -> handleException(ex, "EMAIL_DUPLICADO", HttpStatus.CONFLICT, log))
                        .onErrorResume(EmailInvalidoException.class, ex -> handleException(ex, "EMAIL_INVALIDO", log))
                        .onErrorResume(DocumentoDuplicadoException.class, ex -> handleException(ex, "DOCUMENTO_DUPLICADO", HttpStatus.CONFLICT, log))
                        .onErrorResume(SalarioBaseException.class, ex -> handleException(ex, "SALARIO_BASE_INVALIDO", log))
                        .onErrorResume(PhoneNotvalidException.class, ex -> handleException(ex, "TELEFONO_INVALIDO", log))
                        .onErrorResume(RoleNotEmptyException.class, ex -> handleException(ex, "ROL_OBLIGATORIO", log))
                        .onErrorResume(UsuarioMenorEdadException.class, ex -> handleException(ex, "USUARIO_MENOR_EDAD", HttpStatus.CONFLICT, log))
                        // --- Fallback genérico (500) ---
                        .onErrorResume(ex -> handleGenericException(ex, log));
    }

    private Mono<ServerResponse> handleException(Exception ex, String code, Logger log) {
        log.warn("Bad Request - {}: {}", code, ex.getMessage());
        return ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ErrorResponse.of(code, ex.getMessage()));
    }

    private Mono<ServerResponse> handleException(Exception ex, String code, HttpStatus status, Logger log) {
        log.warn("{} - {}: {}", status, code, ex.getMessage());
        return ServerResponse.status(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ErrorResponse.of(code, ex.getMessage()));
    }

    private Mono<ServerResponse> handleGenericException(Throwable ex, Logger log) {
        log.error("Unexpected error during user registration", ex);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ErrorResponse.of("ERROR_INTERNO",
                                        "Ocurrió un error inesperado. Contacte con el administrador."));
    }

}
