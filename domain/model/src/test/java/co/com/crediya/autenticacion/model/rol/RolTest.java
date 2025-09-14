package co.com.crediya.autenticacion.model.rol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolTest {

    @Test
    @DisplayName("Debe crear rol válido usando builder")
    void shouldCreateValidRol() {
        Rol rol = Rol.builder()
                .id(1L)
                .nombre("ADMINISTRADOR")
                .descripcion("Rol de administrador del sistema")
                .build();

        assertEquals(1L, rol.getId());
        assertEquals("ADMINISTRADOR", rol.getNombre());
        assertEquals("Rol de administrador del sistema", rol.getDescripcion());
    }

    @Test
    @DisplayName("Debe crear rol con valores nulos")
    void shouldCreateRolWithNullValues() {
        Rol rol = Rol.builder()
                .id(null)
                .nombre(null)
                .descripcion(null)
                .build();

        assertNull(rol.getId());
        assertNull(rol.getNombre());
        assertNull(rol.getDescripcion());
    }

    @Test
    @DisplayName("Debe crear rol usando constructor")
    void shouldCreateRolWithConstructor() {
        Rol rol = new Rol(2L, "ASESOR", "Rol de asesor");

        assertEquals(2L, rol.getId());
        assertEquals("ASESOR", rol.getNombre());
        assertEquals("Rol de asesor", rol.getDescripcion());
    }

    @Test
    @DisplayName("Debe modificar valores usando setters")
    void shouldModifyValuesWithSetters() {
        Rol rol = new Rol(null, null, null);

        rol.setId(3L);
        rol.setNombre("SOLICITANTE");
        rol.setDescripcion("Rol de solicitante");

        assertEquals(3L, rol.getId());
        assertEquals("SOLICITANTE", rol.getNombre());
        assertEquals("Rol de solicitante", rol.getDescripcion());
    }

    @Test
    @DisplayName("Debe crear rol usando toBuilder")
    void shouldCreateRolWithToBuilder() {
        Rol original = Rol.builder()
                .id(1L)
                .nombre("ADMIN")
                .descripcion("Admin role")
                .build();

        Rol modificado = original.toBuilder()
                .nombre("ADMINISTRADOR")
                .descripcion("Rol de administrador")
                .build();

        assertEquals(1L, modificado.getId());
        assertEquals("ADMINISTRADOR", modificado.getNombre());
        assertEquals("Rol de administrador", modificado.getDescripcion());
    }
}