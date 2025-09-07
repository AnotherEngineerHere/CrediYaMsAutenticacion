package co.com.crediya.autenticacion.model.excepciones;

public class ContrasenaVaciaException extends RuntimeException {
    public ContrasenaVaciaException(String message) {
        super(message);
    }
}
