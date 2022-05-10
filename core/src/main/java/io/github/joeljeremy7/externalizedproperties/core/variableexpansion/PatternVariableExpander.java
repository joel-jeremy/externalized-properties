package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * A regex/pattern-based {@link VariableExpander} implementation.
 * This resolves the variables from the resolver.
 * 
 * @implNote By default, this will match the basic pattern: ${variable}
 */
public class PatternVariableExpander implements VariableExpander {
    /** Pattern: ${variable} */
    private static final Pattern DEFAULT_VARIABLE_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
    
    private final ResolverProxy resolverProxy;
    private final Pattern variablePattern;

    /**
     * Constructor.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     */
    public PatternVariableExpander(ExternalizedProperties externalizedProperties) {
        this(externalizedProperties, DEFAULT_VARIABLE_PATTERN);
    }

    /**
     * Constructor.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @param variablePattern The pattern to look for when looking for variables to expand.
     */
    public PatternVariableExpander(
            ExternalizedProperties externalizedProperties,
            Pattern variablePattern
    ) {
        requireNonNull(externalizedProperties, "externalizedProperties");
        requireNonNull(variablePattern, "variablePattern");
        this.resolverProxy = externalizedProperties.proxy(ResolverProxy.class);
        this.variablePattern = variablePattern;
    }

    /**
     * The {@link VariableExpanderProvider} for {@link PatternVariableExpander}.
     * 
     * @return The {@link VariableExpanderProvider} for {@link PatternVariableExpander}.
     */
    public static VariableExpanderProvider<PatternVariableExpander> provider() {
        return PatternVariableExpander::new;
    }

    /**
     * The {@link VariableExpanderProvider} for {@link PatternVariableExpander}.
     * 
     * @param variablePattern The pattern to look for when looking for variables to expand.
     * @return The {@link VariableExpanderProvider} for {@link PatternVariableExpander}.
     */
    public static VariableExpanderProvider<PatternVariableExpander> provider(
            Pattern variablePattern
    ) {
        requireNonNull(variablePattern, "variablePattern");
        return externalizedProperties -> new PatternVariableExpander(
            externalizedProperties,
            variablePattern
        );
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(ProxyMethod proxyMethod, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            return replaceVariables(value);
        } catch (RuntimeException ex) {
            throw new VariableExpansionException(
                "Exception occurred while trying to expand value: " + value,
                ex
            );
        }
    }

    private String replaceVariables(String value) {
        StringBuffer output = new StringBuffer();
        Matcher matcher = variablePattern.matcher(value);
        
        while (matcher.find()) {
            // Resolve property from variable.
            String propertyNameVariable = matcher.group(1);
            String propertyValue = resolvePropertyValueOrThrow(
                propertyNameVariable
            );
            matcher.appendReplacement(output, propertyValue);
        }

        // Append any text after the variable if there are any.
        return matcher.appendTail(output).toString();
    }

    private String resolvePropertyValueOrThrow(String variableName) {
        try {
            // Should throw if cannot be resolved.
            return resolverProxy.resolve(variableName);
        } catch (RuntimeException e) {
            throw new VariableExpansionException(
                "Failed to expand \"" + variableName + "\" variable. " +
                "Variable value cannot be resolved from the resolver.",
                e
            );
        }
    }

    static interface ResolverProxy {
        @ExternalizedProperty
        String resolve(String propertyName);
    }
}
