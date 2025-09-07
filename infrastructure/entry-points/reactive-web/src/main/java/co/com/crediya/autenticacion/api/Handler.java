package co.com.crediya.autenticacion.api;

import co.com.crediya.autenticacion.api.dto.CreateUserDTO;
import co.com.crediya.autenticacion.api.dto.CreateUserResponse;
import co.com.crediya.autenticacion.api.dto.ErrorResponse;
import co.com.crediya.autenticacion.api.dto.LoginDTO;
import co.com.crediya.autenticacion.api.exception.ContrasenaVaciaException;
import co.com.crediya.autenticacion.api.exception.CredencialesInvalidasException;
import co.com.crediya.autenticacion.api.exception.RolNoPermitidoException;
import co.com.crediya.autenticacion.api.exception.UsuarioNoEncontradoException;
import co.com.crediya.autenticacion.api.mapper.UsuarioMapper;
import co.com.crediya.autenticacion.model.excepciones.*;
import co.com.crediya.autenticacion.usecase.usuario.UsuarioUseCase;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
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
                System.out.println("Solicitud para crear un nuevo usuario recibida");

                return request.bodyToMono(CreateUserDTO.class)
                                .doOnNext(dto -> System.out.println("Payload recibido: " + dto))
                                .map(UsuarioMapper::toDomain)
                                .doOnNext(u -> System.out.println("Usuario mapeado al dominio: " + u.getEmail()))
                                .flatMap(usuarioUseCase::save)
                                .doOnSuccess(saved -> System.out.println(
                                                "Usuario guardado exitosamente con email: " + saved.getEmail()))
                                .flatMap(saved -> {
                                        URI location = URI.create("/api/v1/usuarios" + saved.getEmail());
                                        return ServerResponse.created(location)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(CreateUserResponse.from(saved));
                                })
                                .onErrorResume(NombreVacioException.class, ex -> ServerResponse.badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("NOMBRE_VACIO", ex.getMessage())))
                                .onErrorResume(NombreLongitudInvalidaException.class, ex -> ServerResponse.badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("NOMBRE_LONGITUD_INVALIDA",
                                                                ex.getMessage())))
                                .onErrorResume(ApellidoVacioException.class, ex -> ServerResponse.badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("APELLIDO_VACIO", ex.getMessage())))
                                .onErrorResume(ApellidoLongitudInvalidaException.class, ex -> ServerResponse
                                                .badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("APELLIDO_LONGITUD_INVALIDA",
                                                                ex.getMessage())))
                                .onErrorResume(EmailVacioException.class, ex -> ServerResponse.badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("EMAIL_VACIO", ex.getMessage())))
                                .onErrorResume(EmailDuplicadoException.class, ex -> ServerResponse.status(409)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("EMAIL_DUPLICADO", ex.getMessage())))
                                .onErrorResume(EmailInvalidoException.class, ex -> ServerResponse.badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("EMAIL_INVALIDO", ex.getMessage())))
                                .onErrorResume(DocumentoDuplicadoException.class, ex -> ServerResponse.status(409)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("DOCUMENTO_DUPLICADO", ex.getMessage())))
                                .onErrorResume(SalarioBaseException.class, ex -> ServerResponse.badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("SALARIO_BASE_INVALIDO", ex.getMessage())))
                                .onErrorResume(PhoneNotvalidException.class, ex -> ServerResponse.badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("TELEFONO_INVALIDO", ex.getMessage())))
                                .onErrorResume(RoleNotEmptyException.class, ex -> ServerResponse.badRequest()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("ROL_OBLIGATORIO", ex.getMessage())))
                                .onErrorResume(UsuarioMenorEdadException.class, ex -> ServerResponse.status(409)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(ErrorResponse.of("USUARIO_MENOR_EDAD", ex.getMessage())))
                                // --- Fallback genérico (500) ---
                                .onErrorResume(ex -> {
                                        System.out.println("Error al guardar usuario: " + ex.getMessage());
                                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(ErrorResponse.of("ERROR_INTERNO",
                                                                        "Ocurrió un error inesperado. Contacte con el administrador."));
                                });
        }

        public Mono<ServerResponse> login(ServerRequest request) {
                return request.bodyToMono(LoginDTO.class)
                                .switchIfEmpty(Mono.error(
                                                new ContrasenaVaciaException("El cuerpo de la petición es requerido")))
                                .flatMap(dto -> loginUseCase.execute(dto.getCorreo(), dto.getContrasena()))
                                .flatMap(res -> jwtService.issue(res.email(), res.rol(), res.accesos())
                                                .flatMap(tokens -> ServerResponse.ok()
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .bodyValue(new LoginResponseDTO(
                                                                                "Bearer",
                                                                                tokens.accessToken(),
                                                                                tokens.refreshToken(),
                                                                                tokens.expiresIn(),
                                                                                res.email(),
                                                                                res.rol(),
                                                                                String.join(" ", res.accesos())
                                                                ))))
                                // --------- Manejo de errores conocidos ----------
                                .onErrorResume(EmailVacioException.class,
                                                ex -> badRequest("EMAIL_VACIO", ex.getMessage()))
                                .onErrorResume(EmailInvalidoException.class,
                                                ex -> badRequest("EMAIL_INVALIDO", ex.getMessage()))
                                .onErrorResume(ContrasenaVaciaException.class,
                                                ex -> badRequest("CONTRASENA_VACIA", ex.getMessage()))
                                .onErrorResume(UsuarioNoEncontradoException.class,
                                                ex -> status(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO",
                                                                ex.getMessage()))
                                .onErrorResume(CredencialesInvalidasException.class,
                                                ex -> status(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS",
                                                                ex.getMessage()))
                                .onErrorResume(RoleNotFoundException.class,
                                                ex -> status(HttpStatus.NOT_FOUND, "ROL_NO_ENCONTRADO",
                                                                ex.getMessage()))
                                .onErrorResume(RolNoPermitidoException.class,
                                                ex -> status(HttpStatus.FORBIDDEN, "ROL_NO_PERMITIDO", ex.getMessage()))
                                // Fallback genérico
                                .onErrorResume(ex -> status(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                                                "Ocurrió un error inesperado. Contacte con el administrador."));
        }

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
