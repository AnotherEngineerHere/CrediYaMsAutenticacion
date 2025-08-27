package co.com.crediya.autenticacion.api;

import co.com.crediya.autenticacion.api.dto.CreateUserDTO;
import co.com.crediya.autenticacion.api.dto.CreateUserResponse;
import co.com.crediya.autenticacion.api.dto.ErrorResponse;
import co.com.crediya.autenticacion.api.exception.EmailDuplicadoException;
import co.com.crediya.autenticacion.api.mapper.UsuarioMapper;
import co.com.crediya.autenticacion.usecase.usuario.UsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Handler encargado de procesar las solicitudes HTTP relacionadas con la entidad Usuario.
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

    /**
     * Maneja la creación de un nuevo usuario en el sistema.
     *
     * @param request cuerpo de la petición con datos del usuario.
     * @return {@link ServerResponse} con estado 201 Created y datos del usuario creado.
     */
    public Mono<ServerResponse> saveUser(ServerRequest request) {
        System.out.println("Solicitud para crear un nuevo usuario recibida");

        return request.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> System.out.println("Payload recibido: " + dto))
                .map(UsuarioMapper::toDomain)
                .doOnNext(u -> System.out.println("Usuario mapeado al dominio: " + u.getEmail()))
                .flatMap(usuarioUseCase::save)
                .doOnSuccess(saved -> System.out.println("Usuario guardado exitosamente con email: " + saved.getEmail()))
                .flatMap(saved -> {
                    URI location = URI.create("/api/v1/usuarios/" + saved.getEmail());
                    return ServerResponse.created(location)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(CreateUserResponse.from(saved));
                })
                // --- Mapeo de errores de negocio (409) ---
                .onErrorResume(EmailDuplicadoException.class, ex ->
                        ServerResponse.status(HttpStatus.CONFLICT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ErrorResponse.of("EMAIL_DUPLICADO", ex.getMessage()))
                )
                // --- Fallback genérico (500) ---
                .onErrorResume(ex -> {
                    System.out.println("Error al guardar usuario: " + ex.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ErrorResponse.of("ERROR_INTERNO",
                                    "Ocurrió un error inesperado. Contacte con el administrador."));
                });
    }
}
