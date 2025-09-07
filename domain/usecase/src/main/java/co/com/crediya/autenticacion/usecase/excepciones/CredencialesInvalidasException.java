package co.com.crediya.autenticacion.usecase.excepciones;

public class CredencialesInvalidasException extends RuntimeException {
  public CredencialesInvalidasException(String message) {
    super(message);
  }
}
