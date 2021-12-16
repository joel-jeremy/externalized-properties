package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;

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
 * An {@link Resolver} decorator which resolves requested properties 
 * from a collection of {@link Resolver}s.
 */
public class CompositeResolver implements Resolver, Iterable<Resolver> {
    
    private static final Logger LOGGER = Logger.getLogger(CompositeResolver.class.getName());

    private final Collection<Resolver> resolvers;

    /**
     * Constructor.
     * 
     * @apiNote If ordering of resolvers is important, callers of this constructor must use a 
     * {@link Collection} implementation that supports ordering such as {@link List}.
     * 
     * @param resolvers The collection of {@link Resolver}s to resolve properties from.
     */
    protected CompositeResolver(Collection<Resolver> resolvers) {
        this.resolvers = requireNonNullOrEmptyCollection(resolvers, "resolvers");
    }

    /**
     * Resolve property from a collection of {@link Resolver}s.
     * 
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    @Override
    public Optional<String> resolve(String propertyName) {
        requireNonNullOrEmptyString(propertyName, "propertyName");

        for (Resolver resolver : resolvers) {
            Optional<String> resolved = resolver.resolve(propertyName);
            if (resolved.isPresent()) {
                return resolved;
            }
        }

        return Optional.empty();
    }

    /**
     * Resolve properties from a collection of {@link Resolver}s.
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

        for (Resolver resolver : resolvers) {
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
     * Factory method to create a {@link CompositeResolver} instance with the provided 
     * resolvers.
     * 
     * @param resolvers The resolvers.
     * @return The {@link CompositeResolver} instance which composes the provided resolvers. 
     */
    public static CompositeResolver from(Resolver... resolvers) {
        return from(
            resolvers == null ? Collections.emptyList() : Arrays.asList(resolvers)
        );
    }

    /**
     * Factory method to create a {@link CompositeResolver} instance with the provided 
     * resolvers.
     * 
     * @apiNote If ordering of resolvers is important, callers of this method must use a 
     * {@link Collection} implementation that supports ordering such as {@link List}.
     * 
     * @param resolvers The resolvers.
     * @return The {@link CompositeResolver} instance which composes the provided resolvers. 
     */
    public static CompositeResolver from(Collection<Resolver> resolvers) {
        return new CompositeResolver(resolvers);
    }

    /**
     * Factory method to create a {@link CompositeResolver} instance with the
     * provided resolvers. This will do some flattening to discard nested 
     * instances of {@link CompositeResolver}.
     * 
     * @implNote This may not necessarily return a {@link CompositeResolver} instance. 
     * If the flattening operation resulted in a single resolver remaining, 
     * that resolver instance will be returned. Otherwise, a {@link CompositeResolver} 
     * instance will be returned which is composed of all the remaining resolvers.
     * 
     * @param resolvers The resolvers.
     * @return The flattened {@link Resolver} instance. 
     */
    public static Resolver flatten(Resolver... resolvers) {
        return flatten(
            resolvers == null ? Collections.emptyList() : Arrays.asList(resolvers)
        );
    }

    /**
     * Factory method to create a {@link Resolver} instance with the provided resolvers. 
     * This will do some flattening to discard nested instances of {@link CompositeResolver}.
     * 
     * @apiNote If ordering of resolvers is important, callers of this method must use a 
     * {@link Collection} implementation that supports ordering such as {@link List}.
     * 
     * @implNote This may not necessarily return a {@link CompositeResolver} instance. 
     * If the flattening operation resulted in a single resolver remaining, 
     * that resolver instance will be returned. Otherwise, a {@link CompositeResolver} 
     * instance will be returned which is composed of all the flattened resolvers.
     * 
     * @param resolvers The resolvers.
     * @return The flattened {@link Resolver} instance.
     */
    public static Resolver flatten(Collection<Resolver> resolvers) {
        requireNonNullOrEmptyCollection(resolvers, "resolvers");
       
        List<Resolver> flattened = flattenResolvers(resolvers);
        if (flattened.size() == 1) {
            return flattened.get(0);
        }

        return from(flattened);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Resolver> iterator() {
        return resolvers.iterator();
    }

    private static List<Resolver> flattenResolvers(
            Collection<Resolver> registeredResolvers
    ) {
        // Resolver order is maintained.
        List<Resolver> flattened = new ArrayList<>();
        for (Resolver registered : registeredResolvers) {
            if (registered instanceof CompositeResolver) {
                flattened.addAll(
                    flattenResolvers(((CompositeResolver)registered).resolvers)
                );
            } else {
                flattened.add(registered);
            }
        }
        return flattened;
    }
}
