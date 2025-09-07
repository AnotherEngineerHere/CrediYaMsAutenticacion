package co.com.crediya.autenticacion.api.security;

import co.com.crediya.autenticacion.model.usuario.gateways.PasswordService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Adaptador que implementa el puerto usando BCrypt de Spring Security.
 * Corre en boundedElastic porque es CPU-bound.
 */
@Component
public class BCryptPasswordService implements PasswordService {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordService(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public Mono<Boolean> matches(String raw, String encoded) {
        return Mono.fromCallable(() -> encoder.matches(raw, encoded))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> encode(String raw) {
        return Mono.fromCallable(() -> encoder.encode(raw))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
