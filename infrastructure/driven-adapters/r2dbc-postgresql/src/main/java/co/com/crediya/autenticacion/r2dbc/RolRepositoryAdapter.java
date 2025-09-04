package co.com.crediya.autenticacion.r2dbc;

import co.com.crediya.autenticacion.model.rol.Rol;
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
    public Mono<Rol> findByName(String name) {
        return repository.findByNombre(name) // <- devuelve RolEntity
                .map(RolRepositoryAdapter::toDomain)
                .doOnSuccess(r -> log.info("Rol encontrado por nombre: {}", name))
                .doOnError(e -> log.error("Error buscando rol {}: {}", name, e.getMessage()));
    }

    @Override
    public Mono<Rol> findById(Long id) {
        return repository.findById(id)
                .map(RolRepositoryAdapter::toDomain)
                .doOnSuccess(r -> log.info("Rol encontrado por id: {}", id))
                .doOnError(e -> log.error("Error buscando rol id {}: {}", id, e.getMessage()));
    }

    // -------------------- Mapper manual Entidad <-> Dominio --------------------
    private static Rol toDomain(RolEntity e) {
        return Rol.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .build();
    }

    private static RolEntity toEntity(Rol r) {
        return RolEntity.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    }
}
