package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.DefaultConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.CachingExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalVariableExpander;
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
import java.util.HashMap;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The builder for {@link ExternalizedProperties}.
 */
public class ExternalizedPropertiesBuilder {
    private List<ExternalizedPropertyResolver> resolvers = new ArrayList<>();
    private List<ConversionHandler<?>> conversionHandlers = 
        new ArrayList<>();

    // Caching invocation handler settings.
    private Duration invocationResultCacheItemLifetime;

    // Eager loading settings.
    private Duration eagerLoadedPropertyCacheItemLifetime;

    // Caching settings.
    private Duration cacheItemLifetime;

    private ExternalizedPropertiesBuilder(){}

    /**
     * Enable default configurations. This will enable default {@link ExternalizedPropertyResolver}s 
     * and {@link ConversionHandler}s via the {@link #withDefaultResolvers()} and 
     * {@link #withDefaultConversionHandlers()} methods and enable caching via 
     * {@link #withCaching(Duration)} and {@link #withInvocationCaching(Duration)} methods.
     * 
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withDefaults() {
        return withDefaultResolvers()
            .withDefaultConversionHandlers()
            .withCaching(Duration.ofMinutes(15))
            .withInvocationCaching(Duration.ofMinutes(15));
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
     * Enable caching of proxy invocation results.
     * 
     * @param cacheItemLifetime The duration of cache items in the cache 
     * before being expired.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withCaching(Duration cacheItemLifetime) {
        this.cacheItemLifetime = requireNonNull(
            cacheItemLifetime, 
            "cacheItemLifetime"
        );
        return this;
    }

    /**
     * Enable caching of proxy invocation results.
     * 
     * @param cacheItemLifetime The duration of cache items in the cache 
     * before being expired.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withInvocationCaching(Duration cacheItemLifetime) {
        this.invocationResultCacheItemLifetime = requireNonNull(
            cacheItemLifetime, 
            "cacheItemLifetime"
        );
        return this;
    }

    /**
     * Eagerly resolve property values of proxy interface methods marked with 
     * {@link ExternalizedProperty} annotation.
     * 
     * @param cacheItemLifetime The duration the eager loaded properties will 
     * stay in the cache before being expired.
     * @return This builder.
     */
    public ExternalizedPropertiesBuilder withEagerLoading(Duration cacheItemLifetime) {
        this.eagerLoadedPropertyCacheItemLifetime = requireNonNull(
            cacheItemLifetime, 
            "cacheItemLifetime"
        );
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
        ExternalizedPropertyResolver resolver = configureExternalizedPropertyResolver();

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

        if (cacheItemLifetime != null) {
            return new CachingExternalizedProperties(
                externalizedProperties,
                cacheItemLifetime
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

        if (invocationResultCacheItemLifetime != null) {
            // Decorate with CachingInvocationHandler.
            factory = factory.compose((before, externalizedProperties, proxyInterface) -> 
                new CachingInvocationHandler(
                    before.createInvocationHandler(externalizedProperties, proxyInterface),
                    new HashMap<>(),
                    invocationResultCacheItemLifetime
                ));
        }
            
        if (eagerLoadedPropertyCacheItemLifetime != null) {
            // Decorate with EagerLoadingInvocationHandler.
            factory = factory.compose((before, externalizedProperties, proxyInterface) -> 
                new EagerLoadingInvocationHandler(
                    before.createInvocationHandler(externalizedProperties, proxyInterface),
                    externalizedProperties,
                    proxyInterface,
                    new HashMap<>(),
                    eagerLoadedPropertyCacheItemLifetime
                ));
        }

        return factory;
    }

    private ExternalizedPropertyResolver configureExternalizedPropertyResolver() {
        if (resolvers.isEmpty()) {
            throw new IllegalStateException(
                "At least one externalized property resolver is required."
            );
        }

        return CompositePropertyResolver.flatten(resolvers);
    }
}