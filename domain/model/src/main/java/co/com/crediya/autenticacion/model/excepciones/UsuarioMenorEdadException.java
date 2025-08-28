package co.com.crediya.autenticacion.model.excepciones;

public class UsuarioMenorEdadException extends RuntimeException {
    public UsuarioMenorEdadException(String message) {
        super(message);
    }
}
