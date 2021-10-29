package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The default {@link VariableExpander} implementation.
 * This resolves the variables from system properties / environment variables.
 */
public class InternalVariableExpander implements VariableExpander {

    private final StringSubstitutor variableExpander;

    /**
     * Construct a variable expander which looks up variables from the externalized property resolver.
     * 
     * @param externalizedPropertyResolver The externalized property resolver.
     */
    public InternalVariableExpander(
            ExternalizedPropertyResolver externalizedPropertyResolver
    ) {
        requireNonNull(externalizedPropertyResolver, "externalizedPropertyResolver");
        this.variableExpander = new StringSubstitutor(
            new ExternalizedPropertyResolverLookup(externalizedPropertyResolver)
        );
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(String value) {
        return variableExpander.replace(value);
    }

    private static class ExternalizedPropertyResolverLookup implements StringLookup {
        private final ExternalizedPropertyResolver externalizedPropertyResolver;

        public ExternalizedPropertyResolverLookup(
                ExternalizedPropertyResolver externalizedPropertyResolver
        ) {
            this.externalizedPropertyResolver = externalizedPropertyResolver;
        }

        @Override
        public String lookup(String key) {
            ExternalizedPropertyResolverResult result = externalizedPropertyResolver.resolve(key);
            return result.findResolvedProperty(key)
                .map(ResolvedProperty::value)
                .orElseThrow(() -> new VariableExpansionException(
                    "Failed to expand \"" + key + "\" variable in property name. " +
                    "Variable value cannot be resolved from any of the externalized property resolvers."
                ));
        }

    }
}
