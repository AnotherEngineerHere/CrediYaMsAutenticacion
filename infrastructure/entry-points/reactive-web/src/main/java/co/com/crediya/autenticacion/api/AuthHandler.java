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

    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    // POST /api/v1/usuarios/login
    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginDTO.class)
                .switchIfEmpty(Mono.error(new ContrasenaVaciaException("El cuerpo de la petición es requerido")))
                // Usa los getters REALES de tu LoginDTO (según dijiste: correo y contrasena)
                .flatMap(dto -> loginUseCase.execute(dto.getCorreoElectronico(), dto.getContrasena()))
                .flatMap(res -> {
                    // scope como string con espacios
                    StringJoiner joiner = new StringJoiner(" ");
                    res.accesos().forEach(joiner::add);
                    String scope = joiner.toString();

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

                // --------- Manejo de errores conocidos ----------
                .onErrorResume(EmailVacioException.class,
                        ex -> badRequest("EMAIL_VACIO", ex.getMessage()))
                .onErrorResume(EmailInvalidoException.class,
                        ex -> badRequest("EMAIL_INVALIDO", ex.getMessage()))
                .onErrorResume(ContrasenaVaciaException.class,
                        ex -> badRequest("CONTRASENA_VACIA", ex.getMessage()))
                .onErrorResume(UsuarioNoEncontradoException.class,
                        ex -> status(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO", ex.getMessage()))
                .onErrorResume(CredencialesInvalidasException.class,
                        ex -> status(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", ex.getMessage()))
                .onErrorResume(RoleNotFoundException.class,
                        ex -> status(HttpStatus.NOT_FOUND, "ROL_NO_ENCONTRADO", ex.getMessage()))
                .onErrorResume(RolNoPermitidoException.class,
                        ex -> status(HttpStatus.FORBIDDEN, "ROL_NO_PERMITIDO", ex.getMessage()))
                .onErrorResume(BadSqlGrammarException.class,
                        ex -> status(HttpStatus.INTERNAL_SERVER_ERROR, "SQL_GRAMMAR",
                                "Error consultando la tabla de roles (verifica schema/columnas)."))
                .onErrorResume(DataAccessException.class,
                        ex -> status(HttpStatus.INTERNAL_SERVER_ERROR, "DATA_ACCESS",
                                "Error de acceso a datos al consultar rol."))
                // Fallback genérico
                .onErrorResume(ex -> status(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                        "Ocurrió un error inesperado. Contacte con el administrador."));

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
