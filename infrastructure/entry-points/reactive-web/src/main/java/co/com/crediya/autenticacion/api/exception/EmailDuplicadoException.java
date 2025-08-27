package co.com.crediya.autenticacion.api.exception;

public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException(String message) {
        super(message);
    }
}
