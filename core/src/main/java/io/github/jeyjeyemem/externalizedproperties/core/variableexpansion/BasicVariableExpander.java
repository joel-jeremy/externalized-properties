package io.github.jeyjeyemem.externalizedproperties.core.variableexpansion;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * A basic {@link VariableExpander} implementation.
 * This resolves the variables from the resolver.
 * 
 * @implNote By default, this will match the basic pattern: ${variable}
 */
public class BasicVariableExpander implements VariableExpander {

    private static final String DEFAULT_VARIABLE_PREFIX = "${";
    private static final String DEFAULT_VARIABLE_END_SUFFIX = "}";

    private final Resolver resolver;
    private final String variablePrefix;
    private final String variableSuffix;

    /**
     * Construct a string variable expander which looks up variable values
     * from the resolver.
     * 
     * @param resolver The resolver to lookup variable values from.
     */
    public BasicVariableExpander(Resolver resolver) {
        this(
            resolver, 
            DEFAULT_VARIABLE_PREFIX, 
            DEFAULT_VARIABLE_END_SUFFIX
        );
    }

    /**
     * Construct a string variable expander which uses a custom variable prefix and suffix 
     * and looks up variable values from the resolver.
     * 
     * @param resolver The resolver to lookup variable values from.
     * @param variablePrefix The variable prefix to look for when expanding variables.
     * @param variableSuffix The variable suffix to look for when expanding variables.
     */
    public BasicVariableExpander(
            Resolver resolver,
            String variablePrefix,
            String variableSuffix
    ) {
        this.resolver = requireNonNull(resolver, "resolver");
        this.variablePrefix = requireNonNullOrEmptyString(variablePrefix, "variablePrefix");
        this.variableSuffix = requireNonNullOrEmptyString(variableSuffix, "variableSuffix");
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(String value) {
        requireNonNull(value, "value");
        try {
            return expandVariables(new StringBuilder(value)).toString();
        } catch (Exception ex) {
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

        int endIndex = builder.indexOf(variableSuffix, variableNameStartIndex);
        if (endIndex == -1 || variableNameStartIndex == endIndex) {
            // No end tag or no variable name in between start and end tags.
            // e.g. "${test" or "${}"
            return builder;
        }

        String variableName = builder.substring(variableNameStartIndex, endIndex);

        String variableValue = resolvePropertyValueOrThrow(variableName);

        builder.replace(startIndex, endIndex + 1, variableValue);

        return expandVariables(builder);
    }

    private String resolvePropertyValueOrThrow(String variableName) {
        return resolver.resolve(variableName)
            .orElseThrow(() -> new VariableExpansionException(
                "Failed to expand \"" + variableName + "\" variable. " +
                "Variable value cannot be resolved from the resolver."
            ));
    }
}
