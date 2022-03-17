package io.github.jeyjeyemem.externalizedproperties.core.exceptions;

/**
 * Processing related exception.
 */
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
