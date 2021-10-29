package io.github.jeyjeyemem.externalizedproperties.core.exceptions;

/**
 * Resolve property conversion related exception. 
 */
public class ResolvedPropertyConversionException extends ExternalizedPropertiesException {
    /**
     * Constructor.
     * 
     * @param message The exception message.
     */
    public ResolvedPropertyConversionException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public ResolvedPropertyConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
