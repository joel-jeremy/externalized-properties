package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.Resolver;

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
    
    private static List<Resolver> toList(ServiceLoader<Resolver> resolvers) {
        List<Resolver> loadedResolvers = new ArrayList<>();
        for (Resolver resolver : resolvers) {
            loadedResolvers.add(resolver);
        }
        return loadedResolvers;
    }
}
