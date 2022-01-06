package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.DefaultConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.CachingExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.ConcurrentHashMapCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.ExpiringCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.WeakConcurrentHashMapCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.processing.ProcessorRegistry;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.CachingInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.EagerLoadingInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.ExternalizedPropertyInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.InvocationHandlerFactory;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CompositeResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.EnvironmentVariableResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The builder for {@link ExternalizedProperties}.
 */
public class ExternalizedPropertiesBuilder {
    private List<Resolver> resolvers = new ArrayList<>();
    private List<ConversionHandler<?>> conversionHandlers = new ArrayList<>();
    private List<Processor> processors = new ArrayList<>();
    
    // Caching settings.
    private Duration cacheDuration = getDefaultCacheDuration();
    private boolean withCaching = false;
    private boolean withProxyEagerLoading = false;
    private boolean withProxyInvocationCaching = false;

    // Default settings.
    private boolean withDefaultResolvers = false;
    private boolean withDefaultConversionHandlers = false;

    private ExternalizedPropertiesBuilder(){}

    /**
     * Enable default configurations. This will enable default {@link Resolver}s 
     * and {@link ConversionHandler}s via the {@link #withDefaultResolvers()} and 
     * {@link #withDefaultConversionHandlers()} methods and enable caching via 
     * {@link #withCaching()} and {@link #withProxyInvocationCaching()} methods.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withDefaults() {
        return withDefaultResolvers()
            .withDefaultConversionHandlers()
            .withCaching()
            .withProxyInvocationCaching();
    }

    /**
     * Adds the {@link SystemPropertyResolver} and {@link EnvironmentVariableResolver} 
     * to the registered {@link Resolver}s.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withDefaultResolvers() {
        this.withDefaultResolvers = true;
        return this;
    }

    /**
     * Adds the {@link DefaultConversionHandler} to the registered 
     * {@link ConversionHandler}s.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withDefaultConversionHandlers() {
        this.withDefaultConversionHandlers = true;
        return this;
    }

    /**
     * Enable caching of resolved properties and variable expansion results.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withCaching() {
        this.withCaching = true;
        return this;
    }

    /**
     * Enable caching of proxy invocation results.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withProxyInvocationCaching() {
        this.withProxyInvocationCaching = true;
        return this;
    }

    /**
     * Eagerly resolve property values of proxy interface methods marked with 
     * {@link ExternalizedProperty} annotation.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withProxyEagerLoading() {
        this.withProxyEagerLoading = true;
        return this;
    }

    /**
     * Sets the global cache duration.
     * 
     * @param cacheDuration The duration of caches before being reloaded.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withCacheDuration(Duration cacheDuration) {
        this.cacheDuration = requireNonNull(cacheDuration, "cacheDuration");
        return this;
    }

    /**
     * The array of {@link Resolver}s to resolve properties from.
     * 
     * @param resolvers The resolvers.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder resolvers(Resolver... resolvers) {
        requireNonNull(resolvers, "resolvers");

        return resolvers(Arrays.asList(resolvers));
    }

    /**
     * The collection of {@link Resolver}s to resolve properties from.
     * 
     * @param resolvers The resolvers.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder resolvers(Collection<Resolver> resolvers) {
        requireNonNull(resolvers, "resolvers");

        this.resolvers.addAll(resolvers);
        return this;
    }

    /**
     * The array of {@link ConversionHandler}s to convert properties
     * to various types.
     * 
     * @param conversionHandlers The conversion handlers.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder conversionHandlers(
            ConversionHandler<?>... conversionHandlers
    ) {
        requireNonNull(conversionHandlers, "conversionHandlers");

        return conversionHandlers(Arrays.asList(conversionHandlers));
    }

    /**
     * The collection of {@link ConversionHandler}s to convert properties
     * to various types.
     * 
     * @param conversionHandlers The conversion handlers.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder conversionHandlers(
            Collection<ConversionHandler<?>> conversionHandlers
    ) {
        requireNonNull(conversionHandlers, "conversionHandlers");
        
        this.conversionHandlers.addAll(conversionHandlers);
        return this;
    }

    /**
     * The array of {@link Processor}s to register.
     * 
     * @param processors The processors to register.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder processors(Processor... processors) {
        requireNonNull(processors, "processors");

        return processors(Arrays.asList(processors));
    }

    /**
     * The collection of {@link Processor}s to register.
     * 
     * @param processors The processors to register.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder processors(Collection<Processor> processors) {
        requireNonNull(processors, "processors");

        this.processors.addAll(processors);
        return this;
    }

    /**
     * Build the {@link InternalExternalizedProperties} instance.
     * 
     * @return The built {@link InternalExternalizedProperties} instance.
     */
    public ExternalizedProperties build() {
        Resolver resolver = buildResolver();
        Converter converter = buildConverter();
        ProcessorRegistry processorRegistry = buildProcessorRegistry();
        VariableExpander variableExpander = buildVariableExpander(resolver);
        InvocationHandlerFactory invocationHandlerFactory = buildInvocationHandlerFactory();

        ExternalizedProperties externalizedProperties = new InternalExternalizedProperties(
            resolver, 
            processorRegistry,
            converter,
            variableExpander,
            invocationHandlerFactory
        );

        if (withCaching) {
            return new CachingExternalizedProperties(
                externalizedProperties,
                new ExpiringCacheStrategy<>(
                    new ConcurrentHashMapCacheStrategy<>(),
                    cacheDuration
                ),
                new ExpiringCacheStrategy<>(
                    new ConcurrentHashMapCacheStrategy<>(),
                    cacheDuration
                )
            );
        }

        return externalizedProperties;
    }

    /**
     * Create a new builder instance.
     * 
     * @return The builder instance.
     */
    public static ExternalizedPropertiesBuilder newBuilder() {
        return new ExternalizedPropertiesBuilder();
    }

    private InvocationHandlerFactory buildInvocationHandlerFactory() {
        // Default invocation handler factory.
        InvocationHandlerFactory factory = (externalizedProperties, proxyInterface) -> 
            new ExternalizedPropertyInvocationHandler(
                externalizedProperties
            );

        if (withProxyInvocationCaching) {
            // Decorate with CachingInvocationHandler.
            factory = factory.compose((baseFactory, externalizedProperties, proxyInterface) -> 
                new CachingInvocationHandler(
                    baseFactory.createInvocationHandler(externalizedProperties, proxyInterface),
                    new ExpiringCacheStrategy<>(
                        new WeakConcurrentHashMapCacheStrategy<>(),
                        cacheDuration
                    )
                ));
        }
            
        if (withProxyEagerLoading) {
            // Decorate with EagerLoadingInvocationHandler.
            factory = factory.compose((baseFactory, externalizedProperties, proxyInterface) -> 
                new EagerLoadingInvocationHandler(
                    baseFactory.createInvocationHandler(externalizedProperties, proxyInterface),
                    externalizedProperties,
                    proxyInterface,
                    new ExpiringCacheStrategy<>(
                        new WeakConcurrentHashMapCacheStrategy<>(),
                        cacheDuration
                    )
                ));
        }

        return factory;
    }

    private Resolver buildResolver() {
        // Add default resolvers last.
        // Custom resolvers always take precedence.
        if (withDefaultResolvers) {
            resolvers(new DefaultResolver());
        }

        if (resolvers.isEmpty()) {
            throw new IllegalStateException(
                "At least one resolver is required."
            );
        }

        return CompositeResolver.flatten(resolvers);
    }

    private InternalConverter buildConverter() {
        // Add default conversion handlers last.
        // Custom conversion handlers always take precedence.
        if (withDefaultConversionHandlers) {
            conversionHandlers(new DefaultConversionHandler());
        }
        
        return new InternalConverter(conversionHandlers);
    }

    private ProcessorRegistry buildProcessorRegistry() {
        return new ProcessorRegistry(processors);
    }

    private InternalVariableExpander buildVariableExpander(
            Resolver resolver
    ) {
        return new InternalVariableExpander(resolver);
    }

    private Duration getDefaultCacheDuration() {
        return Duration.ofMinutes(Integer.parseInt(
            System.getProperty("externalizedproperties.defaultCacheDuration", "30")
        ));
    }
}