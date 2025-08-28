package co.com.crediya.autenticacion.model.usuario.gateways;

import co.com.crediya.autenticacion.model.usuario.Usuario;
import reactor.core.publisher.Mono;

public interface UsuarioRepository {

    Mono<Usuario> save(Usuario usuario);

    Mono<Boolean> findByEmail(String email);

    Mono<Boolean> existsByDocumentoIdentidad(String documentoIdentidad);

}
