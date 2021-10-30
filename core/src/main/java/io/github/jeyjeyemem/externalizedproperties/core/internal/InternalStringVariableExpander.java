package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.StringVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.StringVariableExpansionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.StringUtilities;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The default {@link StringVariableExpander} implementation.
 * This resolves the variables from the externalized property resolver.
 * 
 * @implNote By default, this will match the basic pattern: ${variable}
 */
public class InternalStringVariableExpander implements StringVariableExpander {
    private final ExternalizedPropertyResolver externalizedPropertyResolver;

    /**
     * Construct a string variable expander which looks up variable values
     * from the externalized property resolver.
     * 
     * @param externalizedPropertyResolver The externalized property resolver to lookup variable values from.
     */
    public InternalStringVariableExpander(ExternalizedPropertyResolver externalizedPropertyResolver) {
        this.externalizedPropertyResolver = requireNonNull(
            externalizedPropertyResolver, 
            "externalizedPropertyResolver"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String expandVariables(String value) {
        try {
            return StringUtilities.replaceVariables(value, this::resolvePropertyValue);
        } catch (StringVariableExpansionException sve){
            // Just rethrow.
            throw sve;
        } catch (Exception ex) {
            throw new StringVariableExpansionException(
                "Exception occurred while trying to expand value: " + value,
                ex
            );
        }
    }

    private String resolvePropertyValue(String propertyName) {
        return externalizedPropertyResolver.resolve(propertyName)
            .findResolvedProperty(propertyName)
            .map(ResolvedProperty::value)
            .orElseThrow(() -> new StringVariableExpansionException(
                "Failed to expand \"" + propertyName + "\" variable in property name. " +
                "Variable value cannot be resolved from any of the externalized property resolvers."
            ));
    }
    
}
