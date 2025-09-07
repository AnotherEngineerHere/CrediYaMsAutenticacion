package co.com.crediya.autenticacion.usecase.excepciones;

public class RoleNotFoundException extends RuntimeException {
  public RoleNotFoundException(String message) {
    super(message);
  }
}
