package co.com.crediya.autenticacion.usecase.usuario;

import co.com.crediya.autenticacion.model.excepciones.DocumentoDuplicadoException;
import co.com.crediya.autenticacion.model.excepciones.EmailDuplicadoException;
import co.com.crediya.autenticacion.model.excepciones.EmailInvalidoException;
import co.com.crediya.autenticacion.model.usuario.Usuario;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para operaciones relacionadas con la entidad {@link Usuario}.
 * <p>
 * Esta clase contiene la lógica de negocio encargada de validar y orquestar las
 * operaciones antes de interactuar con el repositorio subyacente.
 * </p>
 */
@RequiredArgsConstructor
public class UsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    /**
     * Guarda un nuevo usuario en el sistema validando previamente:
     * <ul>
     *     <li>Que el correo electrónico no sea nulo, vacío ni inválido.</li>
     *     <li>Que el correo electrónico no exista ya en el sistema.</li>
     *     <li>Que el documento de identidad no exista ya en el sistema.</li>
     * </ul>
     *
     * @param usuario objeto de tipo {@link Usuario} que contiene los datos a persistir.
     * @return un {@link Mono} que emite el {@link Usuario} guardado, o un error en caso de que:
     * <ul>
     *     <li>El correo electrónico sea nulo, vacío o tenga un formato inválido → {@link EmailInvalidoException}</li>
     *     <li>El correo electrónico ya exista → {@link EmailDuplicadoException}</li>
     *     <li>El documento de identidad ya exista → {@link DocumentoDuplicadoException}</li>
     * </ul>
     */
    public Mono<Usuario> save(Usuario usuario) {
        System.out.println("Iniciando guardado de usuario con email: " + usuario.getEmail()
                + " y documento: " + usuario.getDocumento_identidad());

        // Validación de email nulo o vacío
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return Mono.error(new EmailInvalidoException("El email no puede ser nulo o vacío"));
        }

        // Validación de formato básico de email
        if (!usuario.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return Mono.error(new EmailInvalidoException("El email no tiene un formato válido"));
        }

        // Verificación de duplicados
        return usuarioRepository.findByEmail(usuario.getEmail())
                .flatMap(existeEmail -> {
                    if (Boolean.TRUE.equals(existeEmail)) {
                        System.out.println("Error: el email ya existe en el sistema -> " + usuario.getEmail());
                        return Mono.error(new EmailDuplicadoException("El email ya se encuentra registrado"));
                    }

                    // Validar documento de identidad
                    return usuarioRepository.existsByDocumentoIdentidad(usuario.getDocumento_identidad())
                            .flatMap(existeDoc -> {
                                if (Boolean.TRUE.equals(existeDoc)) {
                                    System.out.println("Error: el documento ya existe en el sistema -> "
                                            + usuario.getDocumento_identidad());
                                    return Mono.error(new DocumentoDuplicadoException(
                                            "El documento de identidad ya se encuentra registrado"));
                                }

                                // Persistir usuario si pasa todas las validaciones
                                return usuarioRepository.save(usuario)
                                        .doOnSuccess(u -> System.out.println("Usuario guardado exitosamente: "
                                                + u.getEmail() + " - Doc: " + u.getDocumento_identidad()))
                                        .doOnError(e -> System.out.println("Error al guardar usuario con email: "
                                                + usuario.getEmail() + " -> " + e.getMessage()));
                            });
                });
    }

    /**
     * Verifica si existe un usuario registrado con el correo electrónico dado.
     *
     * @param email correo electrónico a verificar.
     * @return un {@link Mono} que emite:
     * <ul>
     *     <li>{@code true} si el correo existe</li>
     *     <li>{@code false} si el correo no existe o si es nulo/vacío</li>
     * </ul>
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
