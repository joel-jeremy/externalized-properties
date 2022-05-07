package io.github.joeljeremy7.externalizedproperties.core.internal.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.ProcessorProvider;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderProvider;
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
    private final ExternalizedProperties externalizedProperties;
    private final ResolverProvider<?> resolverProvider;
    private final ProcessorProvider<?> rootProcessorProvider;
    private final VariableExpanderProvider<?> variableExpanderProvider;

    /**
     * Constructor.
     * 
     * @apiNote If ordering of resolvers is important, callers of this constructor must use a 
     * {@link Collection} implementation that supports ordering such as {@link List}.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @param resolverProviders The collection of {@link ResolverProvider}s to provide resolver
     * instances to resolve properties from.
     * @param rootProcessorProvider The root processor provider.
     * @param variableExpanderProvider The variable expander provider.
     */
    public RootResolver(
            ExternalizedProperties externalizedProperties,
            Collection<ResolverProvider<?>> resolverProviders,
            ProcessorProvider<RootProcessor> rootProcessorProvider,
            VariableExpanderProvider<?> variableExpanderProvider
    ) {
        requireNonNull(externalizedProperties, "externalizedProperties");
        requireNonNull(resolverProviders, "resolverProviders");
        requireNonNull(rootProcessorProvider, "rootProcessorProvider");
        requireNonNull(variableExpanderProvider, "variableExpanderProvider");
        
        this.externalizedProperties = externalizedProperties;
        this.resolverProvider = ResolverProvider.memoize(
            initializeResolverProvider(resolverProviders)
        );
        this.rootProcessorProvider = ProcessorProvider.memoize(rootProcessorProvider);
        this.variableExpanderProvider = VariableExpanderProvider.memoize(
            variableExpanderProvider
        );
    }

    /**
     * The {@link ResolverProvider} for {@link RootResolver}.
     * 
     * @param resolverProviders The registered {@link ResolverProvider}s which provide 
     * {@link Resolver} instances.
     * @param rootProcessorProvider The registerd {@link RootProcessor} provider.
     * @param variableExpanderProvider The registered {@link VariableExpander} provider.
     * @return The {@link ResolverProvider} for {@link RootResolver}.
     */
    public static ResolverProvider<RootResolver> provider(
            Collection<ResolverProvider<?>> resolverProviders,
            ProcessorProvider<RootProcessor> rootProcessorProvider,
            VariableExpanderProvider<?> variableExpanderProvider
    ) {
        requireNonNull(resolverProviders, "resolverProviders");
        requireNonNull(rootProcessorProvider, "rootProcessorProvider");
        requireNonNull(variableExpanderProvider, "variableExpanderProvider");
        return externalizedProperties -> new RootResolver(
            externalizedProperties, 
            resolverProviders, 
            rootProcessorProvider, 
            variableExpanderProvider
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolve(ProxyMethod proxyMethod, String propertyName) {
        requireNonNull(proxyMethod, "proxyMethod");
        requireNonNullOrEmptyString(propertyName, "propertyName");

        String expanded = variableExpander().expandVariables(proxyMethod, propertyName);
        return resolver().resolve(proxyMethod, expanded)
            .map(resolved -> processor().process(proxyMethod, resolved));
    }

    private Resolver resolver() {
        return resolverProvider.get(externalizedProperties);
    }

    private Processor processor() {
        return rootProcessorProvider.get(externalizedProperties);
    }

    private VariableExpander variableExpander() {
        return variableExpanderProvider.get(externalizedProperties);
    }
    
    private static ResolverProvider<?> initializeResolverProvider(
            Collection<ResolverProvider<?>> resolverProviders
    ) {
        return CompositeResolver.flattenedProvider(resolverProviders);
    }
}
