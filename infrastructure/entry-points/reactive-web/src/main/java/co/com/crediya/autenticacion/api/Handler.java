package co.com.crediya.autenticacion.api;

import co.com.crediya.autenticacion.usecase.usuario.UsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
//Es mi controlador
public class Handler {

    private final UsuarioUseCase usuarioUseCase;

    public Mono<ServerResponse> getUsuarioByEmail(ServerRequest request) {
        String email = request.pathVariable("email");
        return usuarioUseCase.findByEmail(email)
                .flatMap(u -> ServerResponse.ok().bodyValue(u))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(IllegalArgumentException.class,
                        ex -> ServerResponse.badRequest().bodyValue(Map.of(
                                "status", 400, "error", ex.getMessage(), "path", request.path()
                        )));
    }


    public Mono<ServerResponse> getStatus(ServerRequest request) {
        return ServerResponse.ok().build();
    }

   
}
