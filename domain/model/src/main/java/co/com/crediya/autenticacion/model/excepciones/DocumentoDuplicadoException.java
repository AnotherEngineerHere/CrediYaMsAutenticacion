package co.com.crediya.autenticacion.model.excepciones;

public class DocumentoDuplicadoException extends RuntimeException {
    public DocumentoDuplicadoException(String message) {
        super(message);
    }
}
