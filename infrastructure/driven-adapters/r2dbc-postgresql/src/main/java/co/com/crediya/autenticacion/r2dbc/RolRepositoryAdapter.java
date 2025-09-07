package co.com.crediya.autenticacion.r2dbc;

import co.com.crediya.autenticacion.model.rol.Rol;
import co.com.crediya.autenticacion.model.rol.gateways.RolRepository; // 👈 usa el puerto del dominio
import co.com.crediya.autenticacion.r2dbc.entity.RolEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class RolRepositoryAdapter implements RolRepository {

    private static final Logger log = LoggerFactory.getLogger(RolRepositoryAdapter.class);
    private final RolReactiveRepository repository;

    public RolRepositoryAdapter(RolReactiveRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Rol> findByNombre(String nombre) {
        return repository.findByNombre(nombre)              // <- devuelve RolEntity
                .map(RolRepositoryAdapter::toDomain)    // <- mapeo a dominio
                .doOnSuccess(r -> log.info("Rol encontrado por nombre: {}", nombre))
                .doOnError(e -> log.error("Error buscando rol {}: {}", nombre, e.getMessage()));
    }

    @Override
    public Mono<Rol> findById(Long id) {
        return repository.findById(id)
                .map(RolRepositoryAdapter::toDomain)
                .doOnSuccess(r -> log.info("Rol encontrado por id: {}", id))
                .doOnError(e -> log.error("Error buscando rol id {}: {}", id, e.getMessage()));
    }

    private static Rol toDomain(RolEntity e) {
        if (e == null) return null;
        return Rol.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .build();
    }
}
