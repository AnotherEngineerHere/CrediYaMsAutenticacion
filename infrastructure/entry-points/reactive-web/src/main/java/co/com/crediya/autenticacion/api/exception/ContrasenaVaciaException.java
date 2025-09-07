package co.com.crediya.autenticacion.api.exception;

public class ContrasenaVaciaException extends RuntimeException {
    public ContrasenaVaciaException(String message) {
        super(message);
    }
}
