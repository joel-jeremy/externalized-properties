package io.github.joeljeremy7.externalizedproperties.core.internal.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.CompositeResolver;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNullOrEmpty;

/**
 * The root {@link Resolver}. All requests to resolve properties are routed through this resolver
 * and delegated to the registered {@link Resolver}s. 
 * This holds all registered {@link Resolver}s and {@link Processor}s and takes care of 
 * resolving properties from the registered resolvers, and applying post-processing using the 
 * registered processors.
 */
public class RootResolver implements Resolver {
    private final Resolver resolver;
    private final RootProcessor rootProcessor;

    /**
     * Constructor.
     * 
     * @apiNote If ordering of resolvers is important, callers of this constructor must use a 
     * {@link Collection} implementation that supports ordering such as {@link List}.
     * 
     * @param resolvers The collection of {@link Resolver}s to resolve properties from.
     * @param rootProcessor The root processor.
     */
    public RootResolver(
            Collection<Resolver> resolvers,
            RootProcessor rootProcessor
    ) {
        requireNonNull(resolvers, "resolvers");
        requireNonNull(rootProcessor, "rootProcessor");
        
        this.resolver = CompositeResolver.flatten(resolvers);
        this.rootProcessor = rootProcessor;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolve(ProxyMethod proxyMethod, String propertyName) {
        requireNonNull(proxyMethod, "proxyMethod");
        requireNonNullOrEmpty(propertyName, "propertyName");

        return resolver.resolve(proxyMethod, propertyName)
            .map(resolved -> rootProcessor.process(proxyMethod, resolved));
    }
}
