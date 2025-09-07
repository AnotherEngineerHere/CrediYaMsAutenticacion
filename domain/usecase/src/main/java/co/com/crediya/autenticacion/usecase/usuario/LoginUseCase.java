package co.com.crediya.autenticacion.usecase.usuario;

import co.com.crediya.autenticacion.model.excepciones.*;
import co.com.crediya.autenticacion.model.rol.gateways.RolRepository;
import co.com.crediya.autenticacion.model.usuario.gateways.PasswordService;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.autenticacion.usecase.excepciones.CredencialesInvalidasException;
import co.com.crediya.autenticacion.usecase.excepciones.RolNoPermitidoException;
import co.com.crediya.autenticacion.usecase.excepciones.RoleNotFoundException;
import co.com.crediya.autenticacion.usecase.excepciones.UsuarioNoEncontradoException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;


@RequiredArgsConstructor
public class LoginUseCase {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordService passwordService;

    private static final Pattern EMAIL_RX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Mono<LoginResult> execute(String correo, String contrasena) {
        String email = correo == null ? null : correo.trim();

        if (email == null || email.isEmpty())
            return Mono.error(new EmailVacioException("El email no puede ser nulo o vacío"));
        if (!EMAIL_RX.matcher(email).matches())
            return Mono.error(new EmailInvalidoException("El email no tiene un formato válido"));
        if (contrasena == null || contrasena.isBlank())
            return Mono.error(new ContrasenaVaciaException("La contraseña es obligatoria"));

        return usuarioRepository.getByEmail(email) // o getByEmailFull, según tu gateway
                .switchIfEmpty(Mono.error(new UsuarioNoEncontradoException("No existe un usuario con el email proporcionado")))
                .flatMap(u -> passwordService.matches(contrasena, u.getContrasena())
                        .flatMap(ok -> ok
                                ? rolRepository.findById(u.getRolId())
                                .switchIfEmpty(Mono.error(new RoleNotFoundException("El rol asignado al usuario no existe")))
                                .flatMap(rol -> accesosPorRol(rol.getNombre())
                                        .map(acc -> new LoginResult(u.getEmail(), u.getRolId(), rol.getNombre(), acc)))
                                : Mono.error(new CredencialesInvalidasException("Correo o contraseña incorrectos"))
                        )
                );
    }

    public Mono<List<String>> accesosPorRol(String rolNombre) {
        if (rolNombre == null || rolNombre.isBlank())
            return Mono.error(new RoleNotEmptyException("El nombre del rol es obligatorio"));
        String rn = rolNombre.trim().toUpperCase();

        List<String> admin = List.of(
                "GET /api/v1/usuarios/**",
                "POST /api/v1/usuarios",
                "PUT /api/v1/usuarios/**",
                "GET /api/v1/roles/**"
        );
        List<String> asesor = List.of(
                "GET /api/v1/usuarios/**"
        );
        List<String> solicitante = List.of();

        return switch (rn) {
            case "ADMINISTRATOR", "ADMIN", "ADMINISTRADOR" -> Mono.just(admin);
            case "ASESOR", "ADVISOR" -> Mono.just(asesor);
            case "SOLICITANTE", "APPLICANT" -> Mono.just(solicitante);
            default -> Mono.error(new RolNoPermitidoException("No hay catálogo de accesos para el rol: " + rolNombre));
        };
    }

    public record LoginResult(String email, Long rolId, String rol, List<String> accesos) {}
}
