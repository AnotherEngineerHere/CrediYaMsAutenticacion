package co.com.crediya.autenticacion.api.exception;

public class CredencialesInvalidadasException extends RuntimeException {
    public CredencialesInvalidadasException(String message) {
        super(message);
    }
}
