package io.github.jeyjeyemem.externalizedproperties.core.exceptions;

/**
 * Variable expansion related exception. 
 */
public class VariableExpansionException extends ExternalizedPropertiesException {

    /**
     * Constructor.
     * 
     * @param message The exception message.
     */
    public VariableExpansionException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public VariableExpansionException(String message, Throwable cause) {
        super(message, cause);
    }
}
