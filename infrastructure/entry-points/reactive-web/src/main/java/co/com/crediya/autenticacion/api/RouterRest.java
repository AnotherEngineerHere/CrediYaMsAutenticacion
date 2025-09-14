package co.com.crediya.autenticacion.api;

import co.com.crediya.autenticacion.api.dto.CreateUserDTO;
import co.com.crediya.autenticacion.api.dto.CreateUserResponse;
import co.com.crediya.autenticacion.api.dto.LoginDTO;
import co.com.crediya.autenticacion.api.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    beanClass = Handler.class,
                    beanMethod = "saveUser",            // ← coincide con tu Handler
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Crear usuario",
                            description = "Crea un nuevo usuario",
                            tags = {"Usuarios"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = CreateUserDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Usuario creado",
                                            content = @Content(schema = @Schema(implementation = CreateUserResponse.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                                    @ApiResponse(responseCode = "409", description = "Usuario ya existe")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios/login",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    beanClass = AuthHandler.class,
                    beanMethod = "login",
                    operation = @Operation(
                            operationId = "loginUser",
                            summary = "Iniciar sesión",
                            description = "Autentica un usuario y devuelve tokens de acceso",
                            tags = {"Autenticación"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = LoginDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Login exitoso",
                                            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
                                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                    @ApiResponse(responseCode = "403", description = "Rol no permitido")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler, AuthHandler authHandler) {
        return route(POST("/api/v1/usuarios"), handler::saveUser)
                .andRoute(POST("/api/v1/usuarios/login"), authHandler::login);
    }
}
