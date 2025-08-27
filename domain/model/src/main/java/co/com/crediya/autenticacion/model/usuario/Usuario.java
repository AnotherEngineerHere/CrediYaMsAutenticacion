package co.com.crediya.autenticacion.model.usuario;

import co.com.crediya.autenticacion.model.excepciones.*;
import co.com.crediya.autenticacion.model.rol.Rol;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
@Builder
public class Usuario {

    private final String nombre;
    private final String apellido;
    private final String email;
    private final String contrasena;
    private final String documento_identidad;
    private final String telefono;
    private final Long rolId;
    private final int salario_base;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Builder(toBuilder = true)
    private Usuario(
            String nombre,
            String apellido,
            String email,
            String contrasena,
            String documento_identidad,
            String telefono,
            Long rolId,
            int salario_base
    ) {
        this.nombre = trimOrNull(nombre);
        this.apellido = trimOrNull(apellido);
        this.email = lowerTrim(email);
        this.contrasena = trimOrNull(contrasena);
        this.documento_identidad = trimOrNull(documento_identidad);
        this.telefono = trimOrNull(telefono);
        this.rolId = rolId;
        this.salario_base = salario_base;

        validarInvariantes();
    }

    private void validarInvariantes() {
        validarNombre(this.nombre);
        validarApellido(this.apellido);

        if (email == null || email.isEmpty()) {
            throw new EmailVacioException("El email no puede estar vacío");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new EmailInvalidoException("El email no tiene un formato válido");
        }

        if (contrasena != null && contrasena.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        if (telefono != null) {
            int len = telefono.length();
            if (len < 10 || len > 20) {
                throw new PhoneNotvalidException("El teléfono debe tener entre 10 y 20 caracteres");
            }
        }

        if (Objects.isNull(rolId)) {
            throw new RoleNotEmptyException("El rol es obligatorio");
        }

        if (salario_base < 0 || salario_base > 15_000_000) {
            throw new SalarioBaseException("El salario base debe estar entre 0 y 15.000.000");
        }
    }

    // ---- Validaciones específicas ----
    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            throw new NombreVacioException("El nombre no puede estar vacío");
        }
        int len = nombre.length();
        if (len < 2 || len > 50) {
            throw new NombreLongitudInvalidaException("El nombre debe tener entre 2 y 50 caracteres");
        }
    }

    private static void validarApellido(String apellido) {
        if (apellido == null || apellido.isEmpty()) {
            throw new ApellidoVacioException("El apellido no puede estar vacío");
        }
        int len = apellido.length();
        if (len < 2 || len > 50) {
            throw new ApellidoLongitudInvalidaException("El apellido debe tener entre 2 y 50 caracteres");
        }
    }

    // ---- Helpers ----
    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String lowerTrim(String s) {
        String t = trimOrNull(s);
        return t == null ? null : t.toLowerCase();
    }
}
