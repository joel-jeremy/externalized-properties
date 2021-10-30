package io.github.jeyjeyemem.externalizedproperties.core.exceptions;

/**
 * String variable expansion related exception. 
 */
public class StringVariableExpansionException extends ExternalizedPropertiesException {

    /**
     * Constructor.
     * 
     * @param message The exception message.
     */
    public StringVariableExpansionException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public StringVariableExpansionException(String message, Throwable cause) {
        super(message, cause);
    }
}
