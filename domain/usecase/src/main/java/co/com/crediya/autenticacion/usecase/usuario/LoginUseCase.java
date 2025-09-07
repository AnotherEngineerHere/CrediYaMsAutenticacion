package co.com.crediya.autenticacion.usecase.usuario;

package co.com.crediya.autenticacion.usecase.usuario;

import co.com.crediya.autenticacion.model.excepciones.*;
import co.com.crediya.autenticacion.model.rol.gateways.RolRepository;
import co.com.crediya.autenticacion.model.security.gateways.PasswordService;
import co.com.crediya.autenticacion.model.usuario.Usuario;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class LoginUseCase {

    private final UsuarioRepository usuarioRepository;   // Debe exponer getByEmailFull(String) -> Mono<Usuario>
    private final RolRepository rolRepository;
    private final PasswordService passwordService;       // Puerto de dominio (implementación en infra)

    private static final Pattern EMAIL_RX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * Valida credenciales y retorna información mínima para emisión de token en la capa de entrada.
     * No usa BCrypt ni librerías de seguridad; delega la comparación al PasswordService (puerto).
     */
    public Mono<LoginResult> execute(String correo, String contrasena) {
        final String email = correo == null ? null : correo.trim();

        if (email == null || email.isEmpty()) {
            return Mono.error(new EmailVacioException("El email no puede ser nulo o vacío"));
        }
        if (!EMAIL_RX.matcher(email).matches()) {
            return Mono.error(new EmailInvalidoException("El email no tiene un formato válido"));
        }
        if (contrasena == null || contrasena.isBlank()) {
            return Mono.error(new ContrasenaVaciaException("La contraseña es obligatoria"));
        }

        return usuarioRepository.getByEmail(email)
                .switchIfEmpty(Mono.error(new UsuarioNoEncontradoException("No existe un usuario con el email proporcionado")))
                .flatMap(u -> passwordService.matches(contrasena, u.getContrasena())
                        .flatMap(ok -> ok
                                ? rolRepository.findById(u.getRolId())
                                .switchIfEmpty(Mono.error(new RoleNotFoundException("El rol asignado al usuario no existe")))
                                .flatMap(rol -> accesosPorRol(rol.getName())
                                        .map(acc -> new LoginResult(u.getEmail(), u.getRolId(), rol.getName(), acc)))
                                : Mono.error(new CredencialesInvalidasException("Correo o contraseña incorrectos"))
                        )
                );
    }

    /**
     * Catálogo simple de accesos por rol (puedes moverlo a otra capa si prefieres).
     */
    public Mono<List<String>> accesosPorRol(String rolNombre) {
        if (rolNombre == null || rolNombre.isBlank()) {
            return Mono.error(new RoleNotEmptyException("El nombre del rol es obligatorio"));
        }
        String rn = rolNombre.trim().toUpperCase();

        List<String> admin = List.of(
                "GET /api/v1/usuarios/**",
                "POST /api/v1/usuarios",
                "PUT /api/v1/usuarios/**",
                "DELETE /api/v1/usuarios/**",
                "GET /api/v1/roles/**"
        );
        List<String> asesor = List.of(
                "GET /api/v1/usuarios/**",
                "POST /api/v1/usuarios"
        );
        List<String> solicitante = List.of("GET /api/v1/usuarios/me");

        return switch (rn) {
            case "ADMINISTRATOR", "ADMIN", "ADMINISTRADOR" -> Mono.just(admin);
            case "ASESOR", "ADVISOR" -> Mono.just(asesor);
            case "SOLICITANTE", "APPLICANT" -> Mono.just(solicitante);
            default -> Mono.error(new RolNoPermitidoException("No hay catálogo de accesos para el rol: " + rolNombre));
        };
    }

    /** Payload mínimo para el handler (/login). */
    public record LoginResult(String email, Long rolId, String rol, List<String> accesos) {}
}
