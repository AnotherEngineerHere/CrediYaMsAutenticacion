package co.com.crediya.autenticacion.model.rol.gateways;

import co.com.crediya.autenticacion.model.rol.Rol;
import reactor.core.publisher.Mono;

public interface RolRepository {

    Mono<Rol> findByName(String name);
}
