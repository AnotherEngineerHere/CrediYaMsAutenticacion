package co.com.crediya.autenticacion.usecase.usuario;

import co.com.crediya.autenticacion.model.excepciones.ContrasenaVaciaException;
import co.com.crediya.autenticacion.model.excepciones.EmailInvalidoException;
import co.com.crediya.autenticacion.model.excepciones.EmailVacioException;
import co.com.crediya.autenticacion.model.rol.Rol;
import co.com.crediya.autenticacion.model.rol.gateways.RolRepository;
import co.com.crediya.autenticacion.model.usuario.Usuario;
import co.com.crediya.autenticacion.model.usuario.gateways.PasswordService;
import co.com.crediya.autenticacion.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.autenticacion.usecase.excepciones.CredencialesInvalidasException;
import co.com.crediya.autenticacion.usecase.excepciones.RoleNotFoundException;
import co.com.crediya.autenticacion.usecase.excepciones.RolNoPermitidoException;
import co.com.crediya.autenticacion.usecase.excepciones.UsuarioNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .email("test@example.com")
                .contrasena("hashedPassword")
                .rolId(1L)
                .build();

        rol = Rol.builder()
                .id(1L)
                .nombre("ADMINISTRADOR")
                .descripcion("Rol administrador")
                .build();
    }

    @Test
    @DisplayName("Debe hacer login exitosamente con credenciales válidas")
    void shouldLoginSuccessfully() {
        // Given
        when(usuarioRepository.getByEmail("test@example.com")).thenReturn(Mono.just(usuario));
        when(passwordService.matches("password", "hashedPassword")).thenReturn(Mono.just(true));
        when(rolRepository.findById(1L)).thenReturn(Mono.just(rol));

        // When & Then
        StepVerifier.create(loginUseCase.execute("test@example.com", "password"))
                .expectNextMatches(result ->
                    result.email().equals("test@example.com") &&
                    result.rolId().equals(1L) &&
                    result.rol().equals("ADMINISTRADOR") &&
                    result.accesos().contains("GET /api/v1/usuarios/**")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe fallar si email es nulo")
    void shouldFailWhenEmailIsNull() {
        StepVerifier.create(loginUseCase.execute(null, "password"))
                .expectError(EmailVacioException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si email es vacío")
    void shouldFailWhenEmailIsEmpty() {
        StepVerifier.create(loginUseCase.execute("   ", "password"))
                .expectError(EmailVacioException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si email tiene formato inválido")
    void shouldFailWhenEmailFormatIsInvalid() {
        StepVerifier.create(loginUseCase.execute("invalid-email", "password"))
                .expectError(EmailInvalidoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si contraseña es nula")
    void shouldFailWhenPasswordIsNull() {
        StepVerifier.create(loginUseCase.execute("test@example.com", null))
                .expectError(ContrasenaVaciaException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si contraseña es vacía")
    void shouldFailWhenPasswordIsEmpty() {
        StepVerifier.create(loginUseCase.execute("test@example.com", "   "))
                .expectError(ContrasenaVaciaException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si usuario no existe")
    void shouldFailWhenUserNotFound() {
        when(usuarioRepository.getByEmail("nonexistent@example.com")).thenReturn(Mono.empty());

        StepVerifier.create(loginUseCase.execute("nonexistent@example.com", "password"))
                .expectError(UsuarioNoEncontradoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si contraseña es incorrecta")
    void shouldFailWhenPasswordIsIncorrect() {
        when(usuarioRepository.getByEmail("test@example.com")).thenReturn(Mono.just(usuario));
        when(passwordService.matches("wrongpassword", "hashedPassword")).thenReturn(Mono.just(false));

        StepVerifier.create(loginUseCase.execute("test@example.com", "wrongpassword"))
                .expectError(CredencialesInvalidasException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si rol no existe")
    void shouldFailWhenRoleNotFound() {
        when(usuarioRepository.getByEmail("test@example.com")).thenReturn(Mono.just(usuario));
        when(passwordService.matches("password", "hashedPassword")).thenReturn(Mono.just(true));
        when(rolRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(loginUseCase.execute("test@example.com", "password"))
                .expectError(RoleNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe retornar accesos para rol ADMINISTRADOR")
    void shouldReturnAdminAccess() {
        StepVerifier.create(loginUseCase.accesosPorRol("ADMINISTRADOR"))
                .expectNextMatches(accesos ->
                    accesos.contains("GET /api/v1/usuarios/**") &&
                    accesos.contains("POST /api/v1/usuarios") &&
                    accesos.size() == 4
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar accesos para rol ASESOR")
    void shouldReturnAsesorAccess() {
        StepVerifier.create(loginUseCase.accesosPorRol("ASESOR"))
                .expectNext(List.of("GET /api/v1/usuarios/**"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar lista vacía para rol SOLICITANTE")
    void shouldReturnEmptyAccessForSolicitante() {
        StepVerifier.create(loginUseCase.accesosPorRol("SOLICITANTE"))
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe fallar si nombre del rol es nulo")
    void shouldFailWhenRolNameIsNull() {
        StepVerifier.create(loginUseCase.accesosPorRol(null))
                .expectError(RolNoPermitidoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si nombre del rol es vacío")
    void shouldFailWhenRolNameIsEmpty() {
        StepVerifier.create(loginUseCase.accesosPorRol("   "))
                .expectError(RolNoPermitidoException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe fallar si rol no está en catálogo")
    void shouldFailWhenRolNotInCatalog() {
        StepVerifier.create(loginUseCase.accesosPorRol("UNKNOWN_ROLE"))
                .expectError(RolNoPermitidoException.class)
                .verify();
    }
}