package io.github.jeyjeyemem.externalizedproperties.core.exceptions;

import java.util.Collection;

/**
 * Exception is thrown when a property cannot be resolved.
 */
public class UnresolvedExternalizedPropertyException extends ExternalizedPropertiesException {
    /**
     * The unresolved property names.
     */
    private final Collection<String> unresolvedPropertyNames;

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyNames The unresolved property names.
     * @param message The exception message.
     */
    public UnresolvedExternalizedPropertyException(
            Collection<String> unresolvedPropertyNames, 
            String message
    ) {
        super(message);
        this.unresolvedPropertyNames = unresolvedPropertyNames;
    }

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyNames The unresolved property names.
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public UnresolvedExternalizedPropertyException(
            Collection<String> unresolvedPropertyNames, 
            String message, 
            Throwable cause
    ) {
        super(message, cause);
        this.unresolvedPropertyNames = unresolvedPropertyNames;
    }

    /**
     * The names of the properties which cannot be resolved.
     * 
     * @return The collection of unresolved property names.
     */
    public Collection<String> getUnresolvedPropertyNames() {
        return unresolvedPropertyNames;
    }
}