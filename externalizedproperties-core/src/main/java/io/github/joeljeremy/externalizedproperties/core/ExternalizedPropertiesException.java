package io.github.joeljeremy.externalizedproperties.core;

/** Generic Externalized Properties exception. */
public class ExternalizedPropertiesException extends RuntimeException {
  /**
   * Constructor.
   *
   * @param message The exception message.
   */
  public ExternalizedPropertiesException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message The exception message.
   * @param cause The underlying cause.
   */
  public ExternalizedPropertiesException(String message, Throwable cause) {
    super(message, cause);
  }
}
