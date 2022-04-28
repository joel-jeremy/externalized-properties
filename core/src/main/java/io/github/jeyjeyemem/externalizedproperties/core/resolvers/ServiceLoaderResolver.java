package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

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
        super(toList(ServiceLoader.load(Resolver.class)));
    }

    /**
     * The {@link ResolverProvider} for {@link ServiceLoaderResolver}.
     * 
     * @return The {@link ResolverProvider} for {@link ServiceLoaderResolver}.
     */
    public static ResolverProvider<ServiceLoaderResolver> provider() {
        return externalizedProperties -> new ServiceLoaderResolver();
    }
    
    private static List<Resolver> toList(ServiceLoader<Resolver> resolvers) {
        List<Resolver> loadedResolvers = new ArrayList<>();
        for (Resolver resolver : resolvers) {
            loadedResolvers.add(resolver);
        }
        return loadedResolvers;
    }
}
