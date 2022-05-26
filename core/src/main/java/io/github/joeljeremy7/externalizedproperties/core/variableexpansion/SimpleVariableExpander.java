package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * A simple {@link VariableExpander} implementation.
 * This resolves the variables from the resolver.
 * 
 * @implNote By default, this will match the basic pattern: ${variable}
 */
public class SimpleVariableExpander implements VariableExpander {
    private static final String DEFAULT_VARIABLE_PREFIX = "${";
    private static final String DEFAULT_VARIABLE_END_SUFFIX = "}";

    private final String variablePrefix;
    private final String variableSuffix;

    /**
     * Construct a string variable expander which looks up variable values
     * from the resolver.
     */
    public SimpleVariableExpander() {
        this(DEFAULT_VARIABLE_PREFIX, DEFAULT_VARIABLE_END_SUFFIX);
    }

    /**
     * Construct a string variable expander which uses a custom variable prefix and suffix 
     * and looks up variable values from the resolver.
     * 
     * @param variablePrefix The variable prefix to look for when expanding variables.
     * @param variableSuffix The variable suffix to look for when expanding variables.
     */
    public SimpleVariableExpander(
            String variablePrefix,
            String variableSuffix
    ) {
        this.variablePrefix = requireNonNullOrEmptyString(
            variablePrefix, 
            "variablePrefix"
        );
        this.variableSuffix = requireNonNullOrEmptyString(
            variableSuffix, 
            "variableSuffix"
        );
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(ProxyMethod proxyMethod, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            return expandVariables(proxyMethod, new StringBuilder(value)).toString();
        } catch (RuntimeException ex) {
            throw new VariableExpansionException(
                "Exception occurred while trying to expand value: " + value,
                ex
            );
        }
    }

    private StringBuilder expandVariables(ProxyMethod proxyMethod, StringBuilder builder) {
        int startIndex = builder.indexOf(variablePrefix);
        if (startIndex == -1) {
            return builder;
        }

        int variableNameStartIndex = startIndex + variablePrefix.length();
        int variableNameEndIndex = builder.indexOf(variableSuffix, variableNameStartIndex);
        if (variableNameEndIndex == -1 || variableNameStartIndex == variableNameEndIndex) {
            // No end tag or no variable name in between start and end tags.
            // e.g. "${test" or "${}"
            return builder;
        }

        String variableName = builder.substring(variableNameStartIndex, variableNameEndIndex);

        String variableValue = resolvePropertyValueOrThrow(proxyMethod, variableName);

        builder.replace(startIndex, variableNameEndIndex + 1, variableValue);

        return expandVariables(proxyMethod, builder);
    }

    private String resolvePropertyValueOrThrow(ProxyMethod proxyMethod, String variableName) {
        ResolverProxy resolverProxy = proxyMethod.externalizedProperties()
            .initialize(ResolverProxy.class);
        
        try {
            // Should throw if cannot be resolved.
            return resolverProxy.resolve(variableName);
        } catch (RuntimeException e) {
            throw new VariableExpansionException(
                "Failed to expand \"" + variableName + "\" variable. " +
                "Variable value cannot be resolved.",
                e
            );
        }
    }

    private static interface ResolverProxy {
        @ExternalizedProperty
        public String resolve(String propertyName);
    }
}
