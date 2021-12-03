package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.StringUtilities;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * A regex/pattern-based {@link VariableExpander} implementation.
 * This resolves the variables from the externalized property resolver.
 * 
 * @implNote By default, this will match the basic pattern: ${variable}
 */
public class PatternVariableExpander implements VariableExpander {
    private final ExternalizedPropertyResolver externalizedPropertyResolver;

    /**
     * Construct a string variable expander which looks up variable values
     * from the externalized property resolver.
     * 
     * @param externalizedPropertyResolver The externalized property resolver to lookup variable values from.
     */
    public PatternVariableExpander(ExternalizedPropertyResolver externalizedPropertyResolver) {
        this.externalizedPropertyResolver = requireNonNull(
            externalizedPropertyResolver, 
            "externalizedPropertyResolver"
        );
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(String value) {
        requireNonNull(value, "value");
        
        if (value.isEmpty()) {
            return value;
        }

        try {
            return StringUtilities.replaceVariables(value, this::resolvePropertyValue);
        } catch (Exception ex) {
            throw new VariableExpansionException(
                "Exception occurred while trying to expand value: " + value,
                ex
            );
        }
    }

    private String resolvePropertyValue(String propertyName) {
        return externalizedPropertyResolver.resolve(propertyName)
            .orElseThrow(() -> new VariableExpansionException(
                "Failed to expand \"" + propertyName + "\" variable. " +
                "Variable value cannot be resolved from any of the externalized property resolvers."
            ));
    }
    
}
