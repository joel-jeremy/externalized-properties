package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.DefaultConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.CachingExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.ConcurrentMapCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.ExpiringCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.WeakConcurrentMapCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers.CachingInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers.EagerLoadingInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers.ExternalizedPropertyInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CompositePropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.EnvironmentPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The builder for {@link ExternalizedProperties}.
 */
public class ExternalizedPropertiesBuilder {
    private List<ExternalizedPropertyResolver> resolvers = new ArrayList<>();
    private List<ConversionHandler<?>> conversionHandlers = 
        new ArrayList<>();
    
    // Caching settings.
    private Duration cacheDuration = getDefaultCacheDuration();
    private boolean isCachingEnabled = false;
    private boolean isEagerLoadingEnabled = false;
    private boolean isInvocationCachingEnabled = false;

    private ExternalizedPropertiesBuilder(){}

    /**
     * Enable default configurations. This will enable default {@link ExternalizedPropertyResolver}s 
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
     * Adds the {@link SystemPropertyResolver} and {@link EnvironmentPropertyResolver} 
     * to the registered {@link ExternalizedPropertyResolver}s.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withDefaultResolvers() {
        return resolvers(
            new SystemPropertyResolver(),
            new EnvironmentPropertyResolver()
        );
    }

    /**
     * Adds the {@link DefaultConversionHandler} to the registered 
     * {@link ConversionHandler}s.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withDefaultConversionHandlers() {
        return conversionHandlers(new DefaultConversionHandler());
    }

    /**
     * Enable caching of resolved properties and variable expansion results.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withCaching() {
        this.isCachingEnabled = true;
        return this;
    }

    /**
     * Enable caching of proxy invocation results.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withProxyInvocationCaching() {
        this.isInvocationCachingEnabled = true;
        return this;
    }

    /**
     * Eagerly resolve property values of proxy interface methods marked with 
     * {@link ExternalizedProperty} annotation.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withProxyEagerLoading() {
        this.isEagerLoadingEnabled = true;
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
     * The array of {@link ExternalizedPropertyResolver}s to resolve properties from.
     * 
     * @param externalizedPropertyResolvers The externalized property resolver.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder resolvers(
            ExternalizedPropertyResolver... externalizedPropertyResolvers
    ) {
        requireNonNull(externalizedPropertyResolvers, "externalizedPropertyResolvers");

        return resolvers(Arrays.asList(externalizedPropertyResolvers));
    }

    /**
     * The collection of {@link ExternalizedPropertyResolver}s to resolve properties from.
     * 
     * @param externalizedPropertyResolvers The externalized property resolver.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder resolvers(
            Collection<ExternalizedPropertyResolver> externalizedPropertyResolvers
    ) {
        requireNonNull(externalizedPropertyResolvers, "externalizedPropertyResolvers");

        this.resolvers.addAll(externalizedPropertyResolvers);
        return this;
    }

    /**
     * The array of {@link ConversionHandler}s to convert resolved properties
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
     * The collection of {@link ConversionHandler}s to convert resolved properties
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
     * Build the {@link InternalExternalizedProperties} instance.
     * 
     * @return The built {@link InternalExternalizedProperties} instance.
     */
    public ExternalizedProperties build() {
        ExternalizedPropertyResolver resolver = buildExternalizedPropertyResolver();

        Converter converter = new InternalConverter(
            conversionHandlers
        );

        VariableExpander variableExpander = new InternalVariableExpander(
            resolver
        );

        InvocationHandlerFactory invocationHandlerFactory = 
            buildInvocationHandlerFactory(
                resolver,
                converter,
                variableExpander
            );

        ExternalizedProperties externalizedProperties = new InternalExternalizedProperties(
            resolver, 
            converter,
            variableExpander,
            invocationHandlerFactory
        );

        if (isCachingEnabled) {
            return new CachingExternalizedProperties(
                externalizedProperties,
                new ExpiringCacheStrategy<>(
                    new ConcurrentMapCacheStrategy<>(),
                    cacheDuration
                ),
                new ExpiringCacheStrategy<>(
                    new ConcurrentMapCacheStrategy<>(),
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

    private InvocationHandlerFactory buildInvocationHandlerFactory(
            ExternalizedPropertyResolver resolver,
            Converter converter,
            VariableExpander variableExpander
    ) {
        InvocationHandlerFactory factory = (externalizedProperties, proxyInterface) -> 
            new ExternalizedPropertyInvocationHandler(
                externalizedProperties
            );

        if (isInvocationCachingEnabled) {
            // Decorate with CachingInvocationHandler.
            factory = factory.compose((baseHandler, externalizedProperties, proxyInterface) -> 
                new CachingInvocationHandler(
                    baseHandler,
                    new ExpiringCacheStrategy<>(
                        new WeakConcurrentMapCacheStrategy<>(),
                        cacheDuration
                    )
                ));
        }
            
        if (isEagerLoadingEnabled) {
            // Decorate with EagerLoadingInvocationHandler.
            factory = factory.compose((baseHandler, externalizedProperties, proxyInterface) -> 
                new EagerLoadingInvocationHandler(
                    baseHandler,
                    externalizedProperties,
                    proxyInterface,
                    new ExpiringCacheStrategy<>(
                        new WeakConcurrentMapCacheStrategy<>(),
                        cacheDuration
                    )
                ));
        }

        return factory;
    }

    private ExternalizedPropertyResolver buildExternalizedPropertyResolver() {
        if (resolvers.isEmpty()) {
            throw new IllegalStateException(
                "At least one externalized property resolver is required."
            );
        }

        return CompositePropertyResolver.flatten(resolvers);
    }

    private Duration getDefaultCacheDuration() {
        return Duration.ofMinutes(Integer.parseInt(
            System.getProperty("externalizedproperties.defaultCacheDuration", "30")
        ));
    }
}