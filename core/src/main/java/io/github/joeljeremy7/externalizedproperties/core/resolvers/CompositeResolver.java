package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * A {@link Resolver} decorator which resolves requested properties 
 * from a collection of {@link Resolver}s.
 */
public class CompositeResolver implements Resolver, Iterable<Resolver> {
    /**
     * An empty {@link CompositeResolver} that do not resolve any properties.
     */
    public static final CompositeResolver EMPTY = new CompositeResolver(Collections.emptyList());

    private final Collection<Resolver> resolvers;

    /**
     * Constructor.
     * 
     * @apiNote If ordering of resolvers is important, callers of this constructor must use a 
     * {@link Collection} implementation that supports ordering such as {@link ArrayList}.
     * 
     * @param resolvers The collection of {@link Resolver}s to resolve properties from.
     */
    protected CompositeResolver(Collection<Resolver> resolvers) {
        this.resolvers = requireNonNull(resolvers, "resolvers");
    }

    /**
     * Factory method to create a {@link CompositeResolver} instance with the provided 
     * resolvers.
     * 
     * @param resolvers The resolvers.
     * @return The {@link CompositeResolver} instance which composes the provided resolvers. 
     */
    public static CompositeResolver from(Resolver... resolvers) {
        requireNonNull(resolvers, "resolvers");
        if (resolvers.length == 0) {
            return EMPTY;
        }
        return from(Arrays.asList(resolvers));
    }

    /**
     * Factory method to create a {@link CompositeResolver} instance with the provided 
     * resolvers.
     * 
     * @apiNote If ordering of resolvers is important, callers of this method must use a 
     * {@link Collection} implementation that supports ordering such as {@link ArrayList}.
     * 
     * @param resolvers The resolvers.
     * @return The {@link CompositeResolver} instance which composes the provided resolvers. 
     */
    public static CompositeResolver from(Collection<Resolver> resolvers) {
        requireNonNull(resolvers, "resolvers");
        if (resolvers.isEmpty()) {
            return EMPTY;
        }
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
     * @return The flattened {@link Resolver} instance. Otherwise, an empty 
     * {@link CompositeResolver} if the source resolver array is empty.
     */
    public static Resolver flatten(Resolver... resolvers) {
        requireNonNull(resolvers, "resolvers");
        if (resolvers.length == 0) {
            return EMPTY;
        }
        return flatten(Arrays.asList(resolvers));
    }

    /**
     * Factory method to create a {@link Resolver} instance with the provided resolvers. 
     * This will do some flattening to discard nested instances of {@link CompositeResolver}.
     * 
     * @apiNote If ordering of resolvers is important, callers of this method must use a 
     * {@link Collection} implementation that supports ordering such as {@link ArrayList}.
     * 
     * @implNote This may not necessarily return a {@link CompositeResolver} instance. 
     * If the flattening operation resulted in a single resolver remaining, 
     * that resolver instance will be returned. Otherwise, a {@link CompositeResolver} 
     * instance will be returned which is composed of all the flattened resolvers.
     * 
     * @param resolvers The resolvers.
     * @return The flattened {@link Resolver} instance. Otherwise, an empty 
     * {@link CompositeResolver} if the source resolver collection is empty.
     */
    public static Resolver flatten(Collection<Resolver> resolvers) {
        requireNonNull(resolvers, "resolvers");

        if (resolvers.isEmpty()) {
            return EMPTY;
        }
       
        List<Resolver> flattened = flattenResolvers(resolvers);
        if (flattened.size() == 1) {
            return flattened.get(0);
        }

        return from(flattened);
    }

    /**
     * Resolve property from a collection of {@link Resolver}s.
     * 
     * @param proxyMethod The proxy method.
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    @Override
    public Optional<String> resolve(ProxyMethod proxyMethod, String propertyName) {
        for (Resolver resolver : resolvers) {
            Optional<String> resolved = resolver.resolve(proxyMethod, propertyName);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
        return Optional.empty();
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
