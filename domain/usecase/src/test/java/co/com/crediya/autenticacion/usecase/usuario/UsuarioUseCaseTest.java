package co.com.crediya.autenticacion.usecase.usuario;

import co.com.crediya.autenticacion.model.excepciones.EmailDuplicadoException;
import co.com.crediya.autenticacion.model.usuario.Usuario;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;

class UsuarioUseCaseTest {

	private UsuarioUseCase useCase;
	private InMemoryUsuarioRepository repository;

	@BeforeEach
	void setUp() {
		repository = new InMemoryUsuarioRepository();
		useCase = new UsuarioUseCase(repository);
	}

	@Test
	@DisplayName("Debe guardar usuario cuando email no existe")
	void shouldSaveWhenEmailNotExists() {
		Usuario usuario = Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("jp@mail.com")
				.rolId(1L)
				.salario_base(1_000_000)
				.build();

		StepVerifier.create(useCase.save(usuario))
				.expectNextMatches(saved -> saved.getEmail().equals("jp@mail.com"))
				.verifyComplete();

		// Verifica "persistencia" en el fake repo
		StepVerifier.create(repository.findByEmail("jp@mail.com"))
				.expectNext(true)
				.verifyComplete();
	}

	@Test
	@DisplayName("Debe fallar con EmailDuplicadoException si el email ya existe")
	void shouldFailWhenEmailAlreadyExists() {
		// pre-cargar correo
		repository.add("dup@mail.com");

		Usuario usuario = Usuario.builder()
				.nombre("Juan")
				.apellido("Perez")
				.email("dup@mail.com")
				.rolId(1L)
				.salario_base(1_000_000)
				.build();

		StepVerifier.create(useCase.save(usuario))
				.expectError(EmailDuplicadoException.class)
				.verify();
	}

	// --- Fake repository in-memory ---
	static class InMemoryUsuarioRepository implements UsuarioRepository {
		private final Set<String> emails = new HashSet<>();

		void add(String email) { emails.add(email); }

		@Override
		public Mono<Usuario> save(Usuario usuario) {
			emails.add(usuario.getEmail());
			return Mono.just(usuario);
		}

		@Override
		public Mono<Boolean> findByEmail(String email) {
			return Mono.just(emails.contains(email));
		}
	}
}
