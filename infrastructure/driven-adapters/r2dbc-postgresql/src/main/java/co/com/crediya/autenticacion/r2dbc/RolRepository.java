package co.com.crediya.autenticacion.r2dbc;

import co.com.crediya.autenticacion.model.rol.Rol;
import reactor.core.publisher.Mono;

public interface RolRepository {
    Mono<Rol> findByNombre(String name);
    Mono<Rol> findById(Long id);
}