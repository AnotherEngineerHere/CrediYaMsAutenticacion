package co.com.crediya.autenticacion.r2dbc;

import co.com.crediya.autenticacion.model.usuario.Usuario;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.autenticacion.r2dbc.entity.UsuarioEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private static final Logger log = LoggerFactory.getLogger(UsuarioRepositoryAdapter.class);

    private final UsuarioReactiveRepository repository;

    public UsuarioRepositoryAdapter(UsuarioReactiveRepository repository) {
        this.repository = repository;
    }

    // -------------------- Reglas de email básicas --------------------
    private static final Pattern EMAIL_RX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public Mono<Usuario> save(Usuario usuario) {
        // Dominio -> Entidad -> DB -> Entidad -> Dominio
        UsuarioEntity entity = toEntity(usuario);
        return repository.save(entity)
                .map(UsuarioRepositoryAdapter::toDomain)
                .doOnSuccess(u -> log.info("Usuario guardado en DB: {}", u.getEmail()))
                .doOnError(e -> log.error("Error al guardar usuario {}: {}", usuario.getEmail(), e.getMessage()));
    }

    @Override
    public Mono<Boolean> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Mono.error(new IllegalArgumentException("El email no puede ser nulo o vacío"));
        }
        if (!EMAIL_RX.matcher(email).matches()) {
            return Mono.error(new IllegalArgumentException("Formato de email inválido"));
        }
        return repository.existsByEmail(email)
                .doOnNext(exists -> log.debug("existsByEmail({}) -> {}", email, exists));
    }

    // -------------------- Mapper manual Entidad <-> Dominio --------------------
    private static Usuario toDomain(UsuarioEntity e) {
        // Usa el builder del dominio (valida invariantes)
        return Usuario.builder()
                .nombre(e.getNombre())
                .apellido(e.getApellido())
                .email(e.getEmail())
                .contrasena(e.getContrasena())
                .documento_identidad(e.getDocumentoIdentidad())
                .telefono(e.getTelefono())
                .rolId(e.getRolId())
                .salario_base(e.getSalarioBase())
                .build();
    }

    private static UsuarioEntity toEntity(Usuario u) {
        return UsuarioEntity.builder()
                // id_usuario si es SERIAL/autogenerado, déjalo nulo
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .contrasena(u.getContrasena())
                .documentoIdentidad(u.getDocumento_identidad())
                .telefono(u.getTelefono())
                .rolId(u.getRolId())
                .salarioBase(u.getSalario_base())
                .build();
    }
}
