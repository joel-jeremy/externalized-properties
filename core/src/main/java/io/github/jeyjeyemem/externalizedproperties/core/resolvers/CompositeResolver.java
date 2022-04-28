package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyCollection;

/**
 * A {@link Resolver} decorator which resolves requested properties 
 * from a collection of {@link Resolver}s.
 */
public class CompositeResolver implements Resolver, Iterable<Resolver> {

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

    /**
     * The {@link ResolverProvider} for {@link CompositeResolver}.
     * 
     * @param resolverProviders The {@link ResolverProvider}s.
     * @return The {@link ResolverProvider} for {@link CompositeResolver}.
     */
    public static ResolverProvider<CompositeResolver> provider(
            ResolverProvider<?>... resolverProviders
    ) {
        requireNonNull(resolverProviders, "resolverProviders");
        return provider(Arrays.asList(resolverProviders));
    }

    /**
     * The {@link ResolverProvider} for {@link CompositeResolver}.
     * 
     * @param resolverProviders The {@link ResolverProvider}s.
     * @return The {@link ResolverProvider} for {@link CompositeResolver}.
     */
    public static ResolverProvider<CompositeResolver> provider(
            Collection<ResolverProvider<?>> resolverProviders
    ) {
        requireNonNull(resolverProviders, "resolverProviders");
        return externalizedProperties -> new CompositeResolver(
            resolverProviders.stream()
                .map(rp -> rp.get(externalizedProperties))
                .collect(Collectors.toList())
        );
    }

    /**
     * The {@link ResolverProvider} which creates a {@link Resolver} via 
     * {@link CompositeResolver#flatten(Collection)}.
     * 
     * @implNote This may not necessarily return a {@link CompositeResolver} instance. 
     * If the flattening operation resulted in a single resolver remaining, 
     * that resolver instance will be returned. Otherwise, a {@link CompositeResolver} 
     * instance will be returned which is composed of all the remaining resolvers.
     * 
     * @param resolverProviders The {@link ResolverProvider}s.
     * @return The {@link ResolverProvider} for {@link CompositeResolver}.
     */
    public static ResolverProvider<Resolver> flattenedProvider(
            ResolverProvider<?>... resolverProviders
    ) {
        requireNonNull(resolverProviders, "resolverProviders");
        return flattenedProvider(Arrays.asList(resolverProviders));
    }

    /**
     * The {@link ResolverProvider} which creates a {@link Resolver} via 
     * {@link CompositeResolver#flatten(Collection)}.
     * 
     * @implNote This may not necessarily return a {@link CompositeResolver} instance. 
     * If the flattening operation resulted in a single resolver remaining, 
     * that resolver instance will be returned. Otherwise, a {@link CompositeResolver} 
     * instance will be returned which is composed of all the remaining resolvers.
     * 
     * @param resolverProviders The {@link ResolverProvider}s.
     * @return The {@link ResolverProvider} for {@link CompositeResolver}.
     */
    public static ResolverProvider<Resolver> flattenedProvider(
            Collection<ResolverProvider<?>> resolverProviders
    ) {
        requireNonNull(resolverProviders, "resolverProviders");
        return externalizedProperties -> CompositeResolver.flatten(
            resolverProviders.stream()
                .map(rp -> rp.get(externalizedProperties))
                .collect(Collectors.toList())
        );
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
