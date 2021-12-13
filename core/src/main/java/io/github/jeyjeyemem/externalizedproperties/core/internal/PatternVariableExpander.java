package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * A regex/pattern-based {@link VariableExpander} implementation.
 * This resolves the variables from the externalized property resolver.
 * 
 * @implNote By default, this will match the basic pattern: ${variable}
 */
public class PatternVariableExpander implements VariableExpander {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
    
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
            return replaceVariables(value);
        } catch (Exception ex) {
            throw new VariableExpansionException(
                "Exception occurred while trying to expand value: " + value,
                ex
            );
        }
    }

    private String replaceVariables(String value) {
        StringBuffer output = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(value);
        
        while (matcher.find()) {
            // Resolve property from variable.
            String propertyNameVariable = matcher.group(1);
            String propertyValue = resolvePropertyValueOrThrow(propertyNameVariable);
            matcher.appendReplacement(output, Matcher.quoteReplacement(propertyValue));
        }

        // Append any text after the variable if there are any.
        return matcher.appendTail(output).toString();
    }

    private String resolvePropertyValueOrThrow(String propertyName) {
        return externalizedPropertyResolver.resolve(propertyName)
            .orElseThrow(() -> new VariableExpansionException(
                "Failed to expand \"" + propertyName + "\" variable. " +
                "Variable value cannot be resolved from any of the externalized property resolvers."
            ));
    }
}
