package co.com.crediya.autenticacion.model.excepciones;

public class EmailInvalidoException extends RuntimeException {

    public EmailInvalidoException(String mensaje) {
        super(mensaje);
    }
}
