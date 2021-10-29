package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.Collections.requireNonNullOrEmptyCollection;

/**
 * An {@link ExternalizedPropertyResolver} decorator which resolves requested properties 
 * from a collection of {@link ExternalizedPropertyResolver}s.
 */
public class CompositePropertyResolver implements ExternalizedPropertyResolver {
    private static final Logger LOGGER = Logger.getLogger(CompositePropertyResolver.class.getName());

    private final Collection<ExternalizedPropertyResolver> resolvers;

    /**
     * Constructor.
     * @param resolvers The array of {@link ExternalizedPropertyResolver}s to resolve properties from.
     */
    public CompositePropertyResolver(ExternalizedPropertyResolver... resolvers) {
        this(resolvers == null ? Collections.emptyList() : Arrays.asList(resolvers));
    }

    /**
     * Constructor.
     * 
     * @param resolvers The collection of {@link ExternalizedPropertyResolver}s to resolve properties from.
     */
    public CompositePropertyResolver(Collection<ExternalizedPropertyResolver> resolvers) {
        this.resolvers = new ArrayList<>(
            requireNonNullOrEmptyCollection(resolvers, "resolvers")
        );
    }

    /**
     * Resolve properties from a collection of {@link ExternalizedPropertyResolver}s.
     * 
     * @return The {@link ExternalizedPropertyResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ExternalizedPropertyResolverResult resolve(Collection<String> propertyNames) {
        requireNonNullOrEmptyCollection(propertyNames, "propertyNames");

        List<ResolvedProperty> resolvedProperties = new ArrayList<>(propertyNames.size());
        List<String> unresolvedProperties = new ArrayList<>(propertyNames);

        for (ExternalizedPropertyResolver resolver : resolvers) {
            ExternalizedPropertyResolverResult result = resolver.resolve(unresolvedProperties);
            result.resolvedProperties().forEach(newResolvedProperty -> {
                LOGGER.log(
                    Level.INFO,
                    "Resolved {0} externalized property from {1}.",
                    new Object[] {
                        newResolvedProperty.name(),
                        resolver.getClass()
                    }
                );
                unresolvedProperties.remove(newResolvedProperty.name());
                resolvedProperties.add(newResolvedProperty);
            });

            // Stop when all properties are already resolved.
            if (unresolvedProperties.isEmpty()) {
                LOGGER.log(
                    Level.INFO,
                    "All externalized properties have been resolved: {0}",
                    propertyNames
                );
                break;
            }
        }

        return new ExternalizedPropertyResolverResult(
            propertyNames,
            resolvedProperties
        );
    }
}
