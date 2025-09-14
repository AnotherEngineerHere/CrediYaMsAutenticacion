package co.com.crediya.autenticacion.usecase.usuario;

import co.com.crediya.autenticacion.model.excepciones.DocumentoDuplicadoException;
import co.com.crediya.autenticacion.model.excepciones.EmailDuplicadoException;
import co.com.crediya.autenticacion.model.excepciones.EmailInvalidoException;
import co.com.crediya.autenticacion.model.usuario.Usuario;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioUseCase usuarioUseCase;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .nombre("Juan")
                .apellido("Perez")
                .email("juan@example.com")
                .contrasena("password123")
                .documento_identidad("123456789")
                .telefono("3001234567")
                .rolId(1L)
                .salario_base(1000000L)
                .build();
    }

    @Test
    @DisplayName("Debe guardar usuario exitosamente cuando no hay duplicados")
    void shouldSaveUserSuccessfully() {
        // Given
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Mono.just(false));
        when(usuarioRepository.existsByDocumentoIdentidad(anyString())).thenReturn(Mono.just(false));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(Mono.just(usuario));

        // When & Then
        StepVerifier.create(usuarioUseCase.save(usuario))
                .expectNext(usuario)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe fallar si email es nulo")
    void shouldFailWhenEmailIsNull() {
        Usuario usuarioConEmailNull = usuario.toBuilder().email(null).build();

        StepVerifier.create(usuarioUseCase.save(usuarioConEmailNull))
                .expectError(EmailInvalidoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si email es vacío")
    void shouldFailWhenEmailIsEmpty() {
        Usuario usuarioConEmailVacio = usuario.toBuilder().email("   ").build();

        StepVerifier.create(usuarioUseCase.save(usuarioConEmailVacio))
                .expectError(EmailInvalidoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si email tiene formato inválido")
    void shouldFailWhenEmailFormatIsInvalid() {
        Usuario usuarioConEmailInvalido = usuario.toBuilder().email("invalid-email").build();

        StepVerifier.create(usuarioUseCase.save(usuarioConEmailInvalido))
                .expectError(EmailInvalidoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si email ya existe")
    void shouldFailWhenEmailAlreadyExists() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(usuarioUseCase.save(usuario))
                .expectError(EmailDuplicadoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si documento ya existe")
    void shouldFailWhenDocumentoAlreadyExists() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Mono.just(false));
        when(usuarioRepository.existsByDocumentoIdentidad(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(usuarioUseCase.save(usuario))
                .expectError(DocumentoDuplicadoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe retornar true si email existe")
    void shouldReturnTrueWhenEmailExists() {
        when(usuarioRepository.findByEmail("existing@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(usuarioUseCase.findByEmail("existing@example.com"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar false si email no existe")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        when(usuarioRepository.findByEmail("nonexisting@example.com")).thenReturn(Mono.just(false));

        StepVerifier.create(usuarioUseCase.findByEmail("nonexisting@example.com"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar false si email es nulo")
    void shouldReturnFalseWhenEmailIsNull() {
        StepVerifier.create(usuarioUseCase.findByEmail(null))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar false si email es vacío")
    void shouldReturnFalseWhenEmailIsEmpty() {
        StepVerifier.create(usuarioUseCase.findByEmail("   "))
                .expectNext(false)
                .verifyComplete();
    }
}