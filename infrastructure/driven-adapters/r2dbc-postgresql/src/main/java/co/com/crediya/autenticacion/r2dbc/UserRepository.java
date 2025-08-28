package co.com.crediya.autenticacion.r2dbc;

import co.com.crediya.autenticacion.r2dbc.entity.UsuarioEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

// TODO: This file is just an example, you should delete or modify it
public interface UserRepository extends ReactiveCrudRepository<UsuarioEntity, Long>, ReactiveQueryByExampleExecutor<UsuarioEntity> {

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByDocumentoIdentidad(String documentoIdentidad);
}
