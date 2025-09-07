package co.com.crediya.autenticacion.usecase.excepciones;

public class DataAccessException extends RuntimeException {
  public DataAccessException(String message) {
    super(message);
  }
}
