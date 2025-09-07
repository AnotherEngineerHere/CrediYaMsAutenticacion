package co.com.crediya.autenticacion.api.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String message) {
        super(message);
    }
}
