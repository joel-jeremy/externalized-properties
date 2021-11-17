package io.github.jeyjeyemem.externalizedproperties.core.exceptions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Exception is thrown when a property cannot be resolved.
 */
public class UnresolvedPropertyException extends ExternalizedPropertiesException {
    /**
     * The unresolved property names.
     */
    private final Set<String> unresolvedPropertyNames;

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyName The unresolved property name.
     * @param message The exception message.
     */
    public UnresolvedPropertyException(
            String unresolvedPropertyName, 
            String message
    ) {
        this(Collections.singleton(unresolvedPropertyName), message);
    }

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyName The unresolved property name.
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public UnresolvedPropertyException(
            String unresolvedPropertyName, 
            String message, 
            Throwable cause
    ) {
        this(Collections.singleton(unresolvedPropertyName), message, cause);
    }

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyNames The unresolved property names.
     * @param message The exception message.
     */
    public UnresolvedPropertyException(
            Collection<String> unresolvedPropertyNames, 
            String message
    ) {
        super(message);
        this.unresolvedPropertyNames = new HashSet<>(unresolvedPropertyNames);
    }

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyNames The unresolved property names.
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public UnresolvedPropertyException(
            Collection<String> unresolvedPropertyNames, 
            String message, 
            Throwable cause
    ) {
        super(message, cause);
        this.unresolvedPropertyNames = new HashSet<>(unresolvedPropertyNames);
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