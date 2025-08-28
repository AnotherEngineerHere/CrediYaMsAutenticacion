package co.com.crediya.autenticacion.api.mapper;

import co.com.crediya.autenticacion.api.dto.CreateUserDTO;
import co.com.crediya.autenticacion.model.usuario.Usuario;

public final class UsuarioMapper {
    private UsuarioMapper() {}

    public static Usuario toDomain(CreateUserDTO dto) {
        String email = lower(trim(dto.getEmail()));
        String raw = trim(dto.getContrasena());
        return Usuario.builder()
                .nombre(trim(dto.getNombre()))
                .apellido(trim(dto.getApellido()))
                .email(email)
                .contrasena(raw)
                .documento_identidad(trim(dto.getDocumento_identidad()))
                .telefono(trim(dto.getTelefono()))
                .rolId(dto.getRolId())
                .salario_base(dto.getSalario_base())
                .fecha_nacimiento(dto.getFecha_nacimiento())
                .direccion(trim(dto.getDireccion()))
                .build();
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String lower(String s) {
        return s == null ? null : s.toLowerCase();
    }
}
