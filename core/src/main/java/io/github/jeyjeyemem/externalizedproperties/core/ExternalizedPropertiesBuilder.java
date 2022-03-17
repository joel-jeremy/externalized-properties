package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.ExpiringCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.WeakConcurrentHashMapCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.CachingInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.EagerLoadingInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.ExternalizedPropertyInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.InvocationHandlerFactory;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CompositeResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.EnvironmentVariableResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.variableexpansion.BasicVariableExpander;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The builder for {@link ExternalizedProperties}.
 */
public class ExternalizedPropertiesBuilder {
    private List<Resolver> resolvers = new ArrayList<>();
    private List<Converter<?>> converters = new ArrayList<>();
    private List<Processor> processingHandlers = new ArrayList<>();
    private Function<Resolver, VariableExpander> variableExpanderFactory =
        BasicVariableExpander::new;
    
    // Caching settings.
    private Duration cacheDuration = getDefaultCacheDuration();
    private boolean withResolverCaching = false;
    private boolean withProxyEagerLoading = false;
    private boolean withProxyInvocationCaching = false;

    // Default settings.
    private boolean withDefaultResolvers = false;
    private boolean withDefaultConversionHandlers = false;

    private ExternalizedPropertiesBuilder(){}

    /**
     * Enable default configurations. This will enable default {@link Resolver}s 
     * and {@link Converter}s via the {@link #withDefaultResolvers()} and 
     * {@link #withDefaultConverters()} methods and enable caching via 
     * {@link #withResolverCaching()} and {@link #withProxyInvocationCaching()} methods.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withDefaults() {
        return withDefaultResolvers()
            .withDefaultConverters()
            .withResolverCaching()
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
     * Adds the {@link DefaultConverter} to the registered 
     * {@link Converter}s.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withDefaultConverters() {
        this.withDefaultConversionHandlers = true;
        return this;
    }

    /**
     * Enable caching of properties resolved via {@link Resolver}s.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withResolverCaching() {
        this.withResolverCaching = true;
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
     * The array of {@link Converter}s to convert properties
     * to various types.
     * 
     * @param converters The converters.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder converters(
            Converter<?>... converters
    ) {
        requireNonNull(converters, "converters");
        return converters(Arrays.asList(converters));
    }

    /**
     * The collection of {@link Converter}s to convert properties
     * to various types.
     * 
     * @param converters The converters.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder converters(
            Collection<Converter<?>> converters
    ) {
        requireNonNull(converters, "converters");
        this.converters.addAll(converters);
        return this;
    }

    /**
     * The array of {@link Processor}s to register.
     * 
     * @param processors The processors to register.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder processors(
            Processor... processors
    ) {
        requireNonNull(processors, "processors");
        return processors(Arrays.asList(processors));
    }

    /**
     * The collection of {@link Processor}s to register.
     * 
     * @param processors The processors to register.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder processors(
            Collection<Processor> processors
    ) {
        requireNonNull(processors, "processors");
        this.processingHandlers.addAll(processors);
        return this;
    }

    /**
     * The factory for creating the {@link VariableExpander}.
     * 
     * @param variableExpanderFactory The factory for creating the 
     * {@link VariableExpander}.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder variableExpander(
            Function<Resolver, VariableExpander> variableExpanderFactory
    ) {
        requireNonNull(variableExpanderFactory, "variableExpanderFactory");
        this.variableExpanderFactory = variableExpanderFactory;
        return this;
    }

    /**
     * Build the {@link InternalExternalizedProperties} instance.
     * 
     * @return The built {@link InternalExternalizedProperties} instance.
     */
    public ExternalizedProperties build() {
        Resolver resolver = buildResolver();
        Converter<?> converter = buildConverter();
        Processor processor = buildProcessor();
        VariableExpander variableExpander = buildVariableExpander(resolver);
        InvocationHandlerFactory invocationHandlerFactory = buildInvocationHandlerFactory();

        ExternalizedProperties externalizedProperties = new InternalExternalizedProperties(
            resolver, 
            processor,
            converter,
            variableExpander,
            invocationHandlerFactory
        );

        // if (withCaching) {
        //     return new CachingExternalizedProperties(
        //         externalizedProperties,
        //         new ExpiringCacheStrategy<>(
        //             new ConcurrentHashMapCacheStrategy<>(),
        //             cacheDuration
        //         ),
        //         new ExpiringCacheStrategy<>(
        //             new ConcurrentHashMapCacheStrategy<>(),
        //             cacheDuration
        //         )
        //     );
        // }

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
                EagerLoadingInvocationHandler.eagerLoad(
                    baseFactory.createInvocationHandler(externalizedProperties, proxyInterface),
                    new ExpiringCacheStrategy<>(
                        new WeakConcurrentHashMapCacheStrategy<>(),
                        cacheDuration
                    ),
                    externalizedProperties.resolver(),
                    externalizedProperties.converter(),
                    externalizedProperties.processor(),
                    proxyInterface
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

        Resolver resolver = CompositeResolver.flatten(resolvers);
        if (withResolverCaching) {
            resolver = new CachingResolver(
                resolver, 
                new ExpiringCacheStrategy<>(
                    new WeakConcurrentHashMapCacheStrategy<>(),
                    cacheDuration
                )
            );
        }
        return resolver;
    }

    private Converter<?> buildConverter() {
        // Add default conversion handlers last.
        // Custom conversion handlers always take precedence.
        if (withDefaultConversionHandlers) {
            converters(new DefaultConverter());
        }
        
        return new RootConverter(converters);
    }

    private Processor buildProcessor() {
        return new RootProcessor(processingHandlers);
    }

    private VariableExpander buildVariableExpander(
            Resolver resolver
    ) {
        return variableExpanderFactory.apply(resolver);
    }

    private Duration getDefaultCacheDuration() {
        return Duration.ofMinutes(Integer.parseInt(
            System.getProperty("externalizedproperties.defaultCacheDuration", "30")
        ));
    }
}