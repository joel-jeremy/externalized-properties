package io.github.joeljeremy7.externalizedproperties.core.internal.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.CompositeResolver;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * The root {@link Resolver}. All requests to resolve properties are routed through this resolver
 * and delegated to the registered {@link Resolver}s. 
 * This holds all registered {@link Resolver}s, {@link Processor}s, and {@link VariableExpander} 
 * and takes care of expanding variables in property names, resolving properties from the 
 * registered resolvers, and processing resolved properties via the registered processors.
 */
public class RootResolver implements Resolver {
    private final Resolver resolver;
    private final RootProcessor rootProcessor;
    private final VariableExpander variableExpander;

    /**
     * Constructor.
     * 
     * @apiNote If ordering of resolvers is important, callers of this constructor must use a 
     * {@link Collection} implementation that supports ordering such as {@link List}.
     * 
     * @param resolvers The collection of {@link Resolver}s to resolve properties from.
     * @param rootProcessor The root processor.
     * @param variableExpander The variable expander.
     */
    public RootResolver(
            Collection<Resolver> resolvers,
            RootProcessor rootProcessor,
            VariableExpander variableExpander
    ) {
        requireNonNull(resolvers, "resolvers");
        requireNonNull(rootProcessor, "rootProcessor");
        requireNonNull(variableExpander, "variableExpander");
        
        this.resolver = CompositeResolver.flatten(resolvers);
        this.rootProcessor = rootProcessor;
        this.variableExpander = variableExpander;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolve(ProxyMethod proxyMethod, String propertyName) {
        requireNonNull(proxyMethod, "proxyMethod");
        requireNonNullOrEmptyString(propertyName, "propertyName");

        String expanded = variableExpander.expandVariables(proxyMethod, propertyName);
        return resolver.resolve(proxyMethod, expanded)
            .map(resolved -> rootProcessor.process(proxyMethod, resolved));
    }
}
