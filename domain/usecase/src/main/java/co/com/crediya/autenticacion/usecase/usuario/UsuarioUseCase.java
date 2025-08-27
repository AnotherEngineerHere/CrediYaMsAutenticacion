package co.com.crediya.autenticacion.usecase.usuario;

import co.com.crediya.autenticacion.model.excepciones.EmailDuplicadoException;
import co.com.crediya.autenticacion.model.excepciones.EmailInvalidoException;
import co.com.crediya.autenticacion.model.usuario.Usuario;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para operaciones relacionadas con la entidad {@link Usuario}.
 * <p>
 * Esta clase orquesta la lógica de negocio antes de interactuar con el repositorio.
 * </p>
 */
@RequiredArgsConstructor
public class UsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    /**
     * Guarda un nuevo usuario en el sistema validando que el email no exista previamente.
     *
     * @param usuario objeto de tipo {@link Usuario} que contiene los datos a persistir.
     * @return un {@link Mono} que emite el {@link Usuario} guardado o error si el email ya existe.
     */
    public Mono<Usuario> save(Usuario usuario) {
        System.out.println("Iniciando guardado de usuario con email: " + usuario.getEmail());

        return usuarioRepository.findByEmail(usuario.getEmail())
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        System.out.println("Error: el email ya existe en el sistema -> " + usuario.getEmail());
                        return Mono.error(new EmailDuplicadoException("El email ya se encuentra registrado"));
                    }
                    return usuarioRepository.save(usuario)
                            .doOnSuccess(u -> System.out.println("Usuario guardado exitosamente: " + u.getEmail()))
                            .doOnError(e -> System.out.println("Error al guardar usuario con email: "
                                    + usuario.getEmail() + " -> " + e.getMessage()));
                });
    }

    /**
     * Verifica si existe un usuario por correo electrónico.
     *
     * @param email correo electrónico a verificar.
     * @return un {@link Mono} que emite {@code true} si el correo existe, {@code false} en caso contrario.
     */
    public Mono<Boolean> findByEmail(String email) {
        String safeEmail = email == null ? null : email.trim();
        if (safeEmail == null || safeEmail.isEmpty()) {
            System.out.println("findByEmail llamado con email nulo o vacío");
            return Mono.just(false);
        }

        System.out.println("Buscando existencia de email: " + safeEmail);
        return usuarioRepository.findByEmail(safeEmail)
                .doOnNext(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        System.out.println("El email " + safeEmail + " ya existe en el sistema");
                    } else {
                        System.out.println("El email " + safeEmail + " no existe en el sistema");
                    }
                })
                .doOnError(e -> System.out.println("Error al verificar email "
                        + safeEmail + " -> " + e.getMessage()));
    }
}
