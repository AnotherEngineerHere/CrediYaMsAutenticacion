package co.com.crediya.autenticacion.usecase.excepciones;

public class BadSqlGrammarException extends RuntimeException {
  public BadSqlGrammarException(String message) {
    super(message);
  }
}
