package io.github.joeljeremy7.externalizedproperties.core;

import java.util.Collections;
import java.util.Set;

/**
 * Exception is thrown when properties cannot be resolved.
 */
public class UnresolvedPropertiesException extends ExternalizedPropertiesException {
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
    public UnresolvedPropertiesException(
            String unresolvedPropertyName, 
            String message
    ) {
        super(message);
        this.unresolvedPropertyNames = Collections.singleton(unresolvedPropertyName);
    }

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyName The unresolved property name.
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public UnresolvedPropertiesException(
            String unresolvedPropertyName, 
            String message, 
            Throwable cause
    ) {
        super(message, cause);
        this.unresolvedPropertyNames = Collections.singleton(unresolvedPropertyName);
    }

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyNames The unresolved property names.
     * @param message The exception message.
     */
    public UnresolvedPropertiesException(
            Set<String> unresolvedPropertyNames, 
            String message
    ) {
        super(message);
        this.unresolvedPropertyNames = Collections.unmodifiableSet(
            unresolvedPropertyNames
        );
    }

    /**
     * Constructor.
     * 
     * @param unresolvedPropertyNames The unresolved property names.
     * @param message The exception message.
     * @param cause The underlying cause.
     */
    public UnresolvedPropertiesException(
            Set<String> unresolvedPropertyNames, 
            String message, 
            Throwable cause
    ) {
        super(message, cause);
        this.unresolvedPropertyNames = Collections.unmodifiableSet(
            unresolvedPropertyNames
        );
    }

    /**
     * The names of the properties which cannot be resolved.
     * 
     * @return The collection of unresolved property names.
     */
    public Set<String> unresolvedPropertyNames() {
        return unresolvedPropertyNames;
    }
}