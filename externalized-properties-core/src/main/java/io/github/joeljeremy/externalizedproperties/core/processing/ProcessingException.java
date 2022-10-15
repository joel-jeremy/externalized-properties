package io.github.joeljeremy.externalizedproperties.core.processing;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;

/** Processing related exception. */
public class ProcessingException extends ExternalizedPropertiesException {
  /**
   * Constructor.
   *
   * @param message The exception message.
   */
  public ProcessingException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message The exception message.
   * @param cause The underlying cause.
   */
  public ProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
