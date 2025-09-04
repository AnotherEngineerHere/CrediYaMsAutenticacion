package co.com.crediya.autenticacion.r2dbc;

import co.com.crediya.autenticacion.r2dbc.entity.RolEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RolReactiveRepository
        extends ReactiveCrudRepository<RolEntity, Long>,
        ReactiveQueryByExampleExecutor<RolEntity> {

    // Usa el nombre de columna/campo real. Si en la entidad es "nombre":
    Mono<RolEntity> findByNombre(String nombre);

    // findById ya existe en ReactiveCrudRepository, pero puedes redeclararlo si quieres
    Mono<RolEntity> findById(Long id);
}