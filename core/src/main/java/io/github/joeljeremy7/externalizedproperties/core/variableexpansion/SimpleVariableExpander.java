package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;
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

    private final ResolverProxy resolverProxy;
    private final String variablePrefix;
    private final String variableSuffix;

    /**
     * Construct a string variable expander which looks up variable values
     * from the resolver.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     */
    public SimpleVariableExpander(ExternalizedProperties externalizedProperties) {
        this(
            externalizedProperties,
            DEFAULT_VARIABLE_PREFIX, 
            DEFAULT_VARIABLE_END_SUFFIX
        );
    }

    /**
     * Construct a string variable expander which uses a custom variable prefix and suffix 
     * and looks up variable values from the resolver.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @param variablePrefix The variable prefix to look for when expanding variables.
     * @param variableSuffix The variable suffix to look for when expanding variables.
     */
    public SimpleVariableExpander(
            ExternalizedProperties externalizedProperties,
            String variablePrefix,
            String variableSuffix
    ) {
        requireNonNull(externalizedProperties, "externalizedProperties");
        this.resolverProxy = 
            externalizedProperties.proxy(ResolverProxy.class);
        this.variablePrefix = requireNonNullOrEmptyString(variablePrefix, "variablePrefix");
        this.variableSuffix = requireNonNullOrEmptyString(variableSuffix, "variableSuffix");
    }

    /**
     * The {@link VariableExpanderProvider} for {@link SimpleVariableExpander}.
     * 
     * @return The {@link VariableExpanderProvider} for {@link SimpleVariableExpander}.
     */
    public static VariableExpanderProvider<SimpleVariableExpander> provider() {
        return externalizedProperties -> new SimpleVariableExpander(externalizedProperties);
    }

    /**
     * The {@link VariableExpanderProvider} for {@link SimpleVariableExpander}.
     * 
     * @param variablePrefix The variable prefix to look for when expanding variables.
     * @param variableSuffix The variable suffix to look for when expanding variables.
     * @return The {@link VariableExpanderProvider} for {@link SimpleVariableExpander}.
     */
    public static VariableExpanderProvider<SimpleVariableExpander> provider(
            String variablePrefix,
            String variableSuffix
    ) {
        requireNonNullOrEmptyString(variablePrefix, "variablePrefix");
        requireNonNullOrEmptyString(variableSuffix, "variableSuffix");
        return externalizedProperties -> new SimpleVariableExpander(
            externalizedProperties,
            variablePrefix,
            variableSuffix
        );
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(ProxyMethod proxyMethod, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            return expandVariables(new StringBuilder(value)).toString();
        } catch (RuntimeException ex) {
            throw new VariableExpansionException(
                "Exception occurred while trying to expand value: " + value,
                ex
            );
        }
    }

    private StringBuilder expandVariables(StringBuilder builder) {
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

        String variableValue = resolvePropertyValueOrThrow(variableName);

        builder.replace(startIndex, variableNameEndIndex + 1, variableValue);

        return expandVariables(builder);
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
        public String resolve(String propertyName);
    }
}
