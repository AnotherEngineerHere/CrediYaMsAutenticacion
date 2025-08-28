package co.com.crediya.autenticacion.api;

import co.com.crediya.autenticacion.api.dto.CreateUserDTO;
import co.com.crediya.autenticacion.api.dto.CreateUserResponse;
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
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/usuarios"), handler::saveUser);
    }
}
