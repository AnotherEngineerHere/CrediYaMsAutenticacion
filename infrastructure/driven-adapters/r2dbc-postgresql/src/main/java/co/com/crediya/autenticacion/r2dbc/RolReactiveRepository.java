package co.com.crediya.autenticacion.r2dbc;

import co.com.crediya.autenticacion.r2dbc.entity.RolEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RolReactiveRepository
        extends ReactiveCrudRepository<RolEntity, Long>,
        ReactiveQueryByExampleExecutor<RolEntity> {

    Mono<RolEntity> findByName(String name);

    Mono<RolEntity> findById(Long id);
}
