package co.com.crediya.autenticacion.model.excepciones;

public class EmailVacioException extends RuntimeException {

    public EmailVacioException(String mensaje) {
        super(mensaje);
    }
}