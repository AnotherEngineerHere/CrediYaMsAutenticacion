package co.com.crediya.autenticacion.usecase.excepciones;

public class RolNoPermitidoException extends RuntimeException {
  public RolNoPermitidoException(String message) {
    super(message);
  }
}
