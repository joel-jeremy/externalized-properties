package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * A {@link Resolver} implementation which resolves requested properties 
 * from resolvers that are loaded from {@link ServiceLoader}.
 */
public class ServiceLoaderResolver extends CompositeResolver {
    /**
     * Constuctor. This will load {@link Resolver} implementations via
     * {@link ServiceLoader#load(Class)}.
     */
    public ServiceLoaderResolver() {
        this(ServiceLoader.load(Resolver.class));
    }

    /**
     * Constructor.
     * 
     * @param resolvers The service loader for {@link Resolver} implementations.
     */
    public ServiceLoaderResolver(ServiceLoader<Resolver> resolvers) {
        super(toList(requireNonNull(resolvers, "resolvers")));
    }
    
    private static List<Resolver> toList(ServiceLoader<Resolver> resolvers) {
        List<Resolver> loadedResolvers = new ArrayList<>();
        for (Resolver resolver : resolvers) {
            loadedResolvers.add(resolver);
        }
        return loadedResolvers;
    }
}
