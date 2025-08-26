package co.com.crediya.autenticacion.r2dbc;

import co.com.crediya.autenticacion.model.usuario.Usuario;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.autenticacion.r2dbc.entity.UsuarioEntity;
import co.com.crediya.autenticacion.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Repository
public class UsuarioRepositoryAdapter extends ReactiveAdapterOperations<
        Usuario,
        UsuarioEntity,
        Long,
        UserRepository
> implements UsuarioRepository {
    public UsuarioRepositoryAdapter(UserRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, UsuarioEntity.class/* change for domain model */));


    }

    private static final Pattern EMAIL_RX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public Mono<Boolean> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Mono.error(new IllegalArgumentException("El email no puede ser nulo o vacío"));
        }
        if (!EMAIL_RX.matcher(email).matches()) {
            return Mono.error(new IllegalArgumentException("Formato de email inválido"));
        }
        return super.repository.existsByEmail(email);
    }
}
