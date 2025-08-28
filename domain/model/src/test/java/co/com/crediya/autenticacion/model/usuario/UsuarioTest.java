package co.com.crediya.autenticacion.model.usuario;

import co.com.crediya.autenticacion.model.excepciones.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

	@Test
	@DisplayName("Debe crear usuario válido (happy path)")
	void shouldCreateValidUser() {
		Usuario usuario = Usuario.builder()
				.nombre(" Juan ")
				.apellido(" Perez ")
				.email("USER@MAIL.COM")
				.contrasena("password123")
				.documento_identidad("123")
				.telefono("3001234567")
				.rolId(1L)
				.salario_base(1_000_000)
				.build();

		assertEquals("juan@mail.com".split("@")[1], usuario.getEmail().split("@")[1]);
		assertEquals("juan@mail.com".split("@")[0].toLowerCase(), usuario.getEmail().split("@")[0]);
		assertEquals(1_000_000, usuario.getSalario_base());
	}

	@Test
	@DisplayName("Debe fallar si nombre es nulo o vacío")
	void shouldFailWhenNombreEmpty() {
		assertThrows(NombreVacioException.class, () -> Usuario.builder()
				.nombre(null)
				.apellido("Perez")
				.email("a@b.com")
				.rolId(1L)
				.salario_base(0)
				.build());

		assertThrows(NombreVacioException.class, () -> Usuario.builder()
				.nombre(" ")
				.apellido("Perez")
				.email("a@b.com")
				.rolId(1L)
				.salario_base(0)
				.build());
	}

	@Test
	@DisplayName("Debe fallar si apellido es nulo o vacío")
	void shouldFailWhenApellidoEmpty() {
		assertThrows(ApellidoVacioException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido(null)
				.email("a@b.com")
				.rolId(1L)
				.salario_base(0)
				.build());

		assertThrows(ApellidoVacioException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido(" ")
				.email("a@b.com")
				.rolId(1L)
				.salario_base(0)
				.build());
	}

	@Test
	@DisplayName("Debe validar formato de email y no vacío")
	void shouldValidateEmail() {
		assertThrows(EmailVacioException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email(" ")
				.rolId(1L)
				.salario_base(0)
				.build());

		assertThrows(EmailInvalidoException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("no-email")
				.rolId(1L)
				.salario_base(0)
				.build());
	}

	@Test
	@DisplayName("Debe validar longitud del teléfono cuando es provisto")
	void shouldValidateTelefonoLength() {
		assertThrows(PhoneNotvalidException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("a@b.com")
				.telefono("123456789") // 9
				.rolId(1L)
				.salario_base(0)
				.build());

		assertThrows(PhoneNotvalidException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("a@b.com")
				.telefono("123456789012345678901") // 21
				.rolId(1L)
				.salario_base(0)
				.build());
	}

	@Test
	@DisplayName("Debe fallar si rolId es nulo")
	void shouldRequireRol() {
		assertThrows(RoleNotEmptyException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("a@b.com")
				.rolId(null)
				.salario_base(0)
				.build());
	}

	@Test
	@DisplayName("Debe validar rango de salario base [0, 15'000.000]")
	void shouldValidateSalarioBaseRange() {
		assertThrows(SalarioBaseException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("a@b.com")
				.rolId(1L)
				.salario_base(-1)
				.build());

		assertThrows(SalarioBaseException.class, () -> Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("a@b.com")
				.rolId(1L)
				.salario_base(15_000_001)
				.build());

		Usuario ok = Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("a@b.com")
				.rolId(1L)
				.salario_base(15_000_000)
				.build();
		assertEquals(15_000_000, ok.getSalario_base());
	}
}
