package co.com.crediya.autenticacion.api.error;

import co.com.crediya.autenticacion.model.excepciones.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2) // Alta precedencia
@RequiredArgsConstructor
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = mapStatus(ex);

        String safeMessage = (status.is5xxServerError())
                ? "Contacte con el administrador"   // <- mensaje genérico para 5xx
                : ex.getMessage();                  // <- 4xx mantiene el detalle

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", safeMessage);
        body.put("path", exchange.getRequest().getPath().value());

        byte[] bytes;

        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            bytes = ("{\"status\":" + status.value() + ",\"message\":\"" + safeMessage.replace("\"","'") + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        var response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(status);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }


    private HttpStatus mapStatus(Throwable ex) {
        // 400 Bad Request – reglas de dominio
        if (ex instanceof EmailVacioException
                || ex instanceof EmailInvalidoException
                || ex instanceof PhoneNotvalidException
                || ex instanceof RoleNotEmptyException
                || ex instanceof SalarioBaseException
                || ex instanceof NombreVacioException
                || ex instanceof NombreLongitudInvalidaException
                || ex instanceof ApellidoVacioException
                || ex instanceof ApellidoLongitudInvalidaException
                || ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        // 409 Conflict – ejemplo: email duplicado (ajusta a tu excepción o DataIntegrity)
        // if (ex instanceof EmailDuplicadoException || ex instanceof DataIntegrityViolationException) return HttpStatus.CONFLICT;

        // 404, 422… agrega aquí si tienes más casos

        // 500 por defecto
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String safe(String s) { return s == null ? "" : s.replace("\"", "'"); }
}
