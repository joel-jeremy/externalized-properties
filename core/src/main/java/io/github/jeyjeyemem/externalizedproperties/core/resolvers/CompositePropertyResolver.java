package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyCollection;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * An {@link ExternalizedPropertyResolver} decorator which resolves requested properties 
 * from a collection of {@link ExternalizedPropertyResolver}s.
 */
public class CompositePropertyResolver 
        implements ExternalizedPropertyResolver, Iterable<ExternalizedPropertyResolver> {
    
    private static final Logger LOGGER = Logger.getLogger(CompositePropertyResolver.class.getName());

    private final Collection<ExternalizedPropertyResolver> resolvers;

    /**
     * Constructor.
     * 
     * @param resolvers The collection of {@link ExternalizedPropertyResolver}s to resolve properties from.
     */
    protected CompositePropertyResolver(
            Collection<ExternalizedPropertyResolver> resolvers
    ) {
        this.resolvers = requireNonNullOrEmptyCollection(resolvers, "resolvers");
    }

    /**
     * Resolve property from a collection of {@link ExternalizedPropertyResolver}s.
     * 
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    @Override
    public Optional<String> resolve(String propertyName) {
        requireNonNullOrEmptyString(propertyName, "propertyName");

        for (ExternalizedPropertyResolver resolver : resolvers) {
            Optional<String> resolved = resolver.resolve(propertyName);
            if (resolved.isPresent()) {
                return resolved;
            }
        }

        return Optional.empty();
    }

    /**
     * Resolve properties from a collection of {@link ExternalizedPropertyResolver}s.
     * 
     * @param propertyNames The property names.
     * @return The {@link Result} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public Result resolve(Collection<String> propertyNames) {
        requireNonNullOrEmptyCollection(propertyNames, "propertyNames");

        Result.Builder resultBuilder = Result.builder(propertyNames);
        List<String> unresolvedPropertyNames = new ArrayList<>(propertyNames);

        for (ExternalizedPropertyResolver resolver : resolvers) {
            Result result = resolver.resolve(unresolvedPropertyNames);
            for (Map.Entry<String, String> newResolvedProperty : result.resolvedProperties().entrySet()) {
                LOGGER.log(
                    Level.FINE,
                    "Resolved {0} externalized property from {1}.",
                    new Object[] {
                        newResolvedProperty.getKey(),
                        resolver.getClass()
                    }
                );
                unresolvedPropertyNames.remove(newResolvedProperty.getKey());
                resultBuilder.add(
                    newResolvedProperty.getKey(), 
                    newResolvedProperty.getValue()
                );
            }

            // Stop when all properties are already resolved.
            if (unresolvedPropertyNames.isEmpty()) {
                LOGGER.log(
                    Level.FINE,
                    "All externalized properties have been resolved: {0}",
                    propertyNames
                );
                break;
            }
        }

        return resultBuilder.build();
    }

    /**
     * Returns the string containing the list of resolvers inside this composite resolver.
     * 
     * @return The string containing the list of resolvers inside this composite resolver.
     */
    @Override
    public String toString() {
        return resolvers.toString();
    }

    /**
     * Factory method to create a {@link CompositePropertyResolver} instance with the
     * provided externalized property resolvers.
     * 
     * @param resolvers The externalized property resolvers.
     * @return The {@link CompositePropertyResolver} instance which composes the provided resolvers. 
     */
    public static CompositePropertyResolver from(ExternalizedPropertyResolver... resolvers) {
        return from(
            resolvers == null ? Collections.emptyList() : Arrays.asList(resolvers)
        );
    }

    /**
     * Factory method to create a {@link CompositePropertyResolver} instance with the
     * provided externalized property resolvers.
     * 
     * @apiNote If ordering of resolvers is important, callers of this method must use a 
     * {@link Collection} implementation that supports ordering such as {@link List}.
     * 
     * @param resolvers The externalized property resolvers.
     * @return The {@link CompositePropertyResolver} instance which composes the provided resolvers. 
     */
    public static CompositePropertyResolver from(Collection<ExternalizedPropertyResolver> resolvers) {
        return new CompositePropertyResolver(resolvers);
    }

    /**
     * Factory method to create a {@link CompositePropertyResolver} instance with the
     * provided externalized property resolvers. This will do some flattening to discard nested 
     * instances of {@link CompositePropertyResolver}.
     * 
     * @implNote This may not necessarily return a {@link CompositePropertyResolver} instance. 
     * If the flattening operation resulted in a single resolver remaining, 
     * that resolver instance will be returned. Otherwise, a {@link CompositePropertyResolver} 
     * instance will be returned which is composed of all the remaining resolvers.
     * 
     * @param resolvers The externalized property resolvers.
     * @return The {@link ExternalizedPropertyResolver} instance. 
     */
    public static ExternalizedPropertyResolver flatten(ExternalizedPropertyResolver... resolvers) {
        return flatten(
            resolvers == null ? Collections.emptyList() : Arrays.asList(resolvers)
        );
    }

    /**
     * Factory method to create a {@link ExternalizedPropertyResolver} instance with the
     * provided externalized property resolvers. This will do some flattening to discard nested 
     * instances of {@link CompositePropertyResolver}.
     * 
     * @apiNote If ordering of resolvers is important, callers of this method must use a 
     * {@link Collection} implementation that supports ordering such as {@link List}.
     * 
     * @implNote This may not necessarily return a {@link CompositePropertyResolver} instance. 
     * If the flattening operation resulted in a single resolver remaining, 
     * that resolver instance will be returned. Otherwise, a {@link CompositePropertyResolver} 
     * instance will be returned which is composed of all the flattened resolvers.
     * 
     * @param resolvers The externalized property resolvers.
     * @return The {@link ExternalizedPropertyResolver} instance.
     */
    public static ExternalizedPropertyResolver flatten(
            Collection<ExternalizedPropertyResolver> resolvers
    ) {
        requireNonNullOrEmptyCollection(resolvers, "resolvers");
       
        List<ExternalizedPropertyResolver> flattened = flattenResolvers(resolvers);
        if (flattened.size() == 1) {
            return flattened.get(0);
        }

        return from(flattened);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ExternalizedPropertyResolver> iterator() {
        return resolvers.iterator();
    }

    private static List<ExternalizedPropertyResolver> flattenResolvers(
            Collection<ExternalizedPropertyResolver> registeredResolvers
    ) {
        // Resolver order is maintained.
        List<ExternalizedPropertyResolver> flattened = new ArrayList<>();
        for (ExternalizedPropertyResolver registered : registeredResolvers) {
            if (registered instanceof CompositePropertyResolver) {
                flattened.addAll(
                    flattenResolvers(((CompositePropertyResolver)registered).resolvers)
                );
            } else {
                flattened.add(registered);
            }
        }
        return flattened;
    }
}
