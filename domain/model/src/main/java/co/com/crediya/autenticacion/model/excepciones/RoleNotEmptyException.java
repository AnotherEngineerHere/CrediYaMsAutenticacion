package co.com.crediya.autenticacion.model.excepciones;

public class RoleNotEmptyException extends RuntimeException {
    public RoleNotEmptyException(String message) {
        super(message);
    }
}
