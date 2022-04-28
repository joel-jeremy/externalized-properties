package io.github.joeljeremy7.externalizedproperties.core.conversion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;

/**
 * Conversion related exception. 
 */
public class ConversionException extends ExternalizedPropertiesException {
    /**
     * Constructor.
     * 
     * @param message The exception message.
     */
    public ConversionException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
