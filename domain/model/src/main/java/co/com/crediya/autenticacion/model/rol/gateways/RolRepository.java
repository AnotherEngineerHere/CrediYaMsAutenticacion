package co.com.crediya.autenticacion.model.rol.gateways;

import co.com.crediya.autenticacion.model.rol.Rol;
import reactor.core.publisher.Mono;

public interface RolRepository {

    Mono<Rol> findById(Long id);

    Mono<Rol> findByNombre(String name);

}
