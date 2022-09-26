package io.github.joeljeremy7.externalizedproperties.core;

/** Exception is thrown when a externalized property cannot be resolved. */
public class UnresolvedPropertyException extends ExternalizedPropertiesException {
  /** The name of the externalized property which cannot be resolved. */
  private final String externalizedPropertyName;

  /**
   * Constructor.
   *
   * @param externalizedPropertyName The name of the externalized property which cannot be resolved.
   * @param message The exception message.
   */
  public UnresolvedPropertyException(String externalizedPropertyName, String message) {
    super(message);
    this.externalizedPropertyName = externalizedPropertyName;
  }

  /**
   * Constructor.
   *
   * @param externalizedPropertyName The name of the externalized property which cannot be resolved.
   * @param message The exception message.
   * @param cause The underlying cause.
   */
  public UnresolvedPropertyException(
      String externalizedPropertyName, String message, Throwable cause) {
    super(message, cause);
    this.externalizedPropertyName = externalizedPropertyName;
  }

  /**
   * The name of the externalized property which cannot be resolved.
   *
   * @return The name of the externalized property which cannot be resolved.
   */
  public String externalizedPropertyName() {
    return externalizedPropertyName;
  }
}
