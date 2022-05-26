package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.CachingExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.internal.InternalExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies.ExpiringCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies.WeakConcurrentHashMapCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.CachingInvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.EagerLoadingInvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.ExternalizedPropertiesInvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.proxy.InvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The core API for Externalized Properties. This provides methods to initialize proxies that
 * will redirect method invocations to Externalized Properties. The annotation provided by
 * Externalized Properties will define how certain method invocations will be handled.
 * 
 * @see ExternalizedProperty
 * @see Convert
 */
public interface ExternalizedProperties {
    /**
     * Initialize a proxy whose method invocations will be redirected to Externalized Properties.
     * 
     * @implNote Only interface types will be accepted. If a non-interface type is provided, 
     * an exception will be thrown. 
     * 
     * @implNote The names specified in the {@link ExternalizedProperty} supports variables
     * e.g. <code>"${my.variable}.property"</code>. These variables will be expanded accordingly.
     * 
     * @param <T> The type to create a proxy for.
     * @param proxyInterface The type to create a proxy for. The initialized proxy's method 
     * invocations will be redirected to Externalized Properties.
     * @return The initialized proxy whose method invocations will be redirected to 
     * Externalized Properties.
     */
    <T> T initialize(Class<T> proxyInterface);

    /**
     * Initialize a proxy whose method invocations will be redirected to Externalized Properties.
     * 
     * @implNote Only interface types will be accepted. If a non-interface type is provided, 
     * an exception will be thrown. 
     * 
     * @implNote The names specified in the {@link ExternalizedProperty} supports variables
     * e.g. <code>"${my.variable}.property"</code>. These variables will be expanded accordingly.
     * 
     * @param <T> The type to create a proxy for.
     * @param proxyInterface The type to create a proxy for. The initialized proxy's method 
     * invocations will be redirected to Externalized Properties.
     * @param classLoader The class loader to define the generated class in.
     * @return The initialized proxy whose method invocations will be redirected to 
     * Externalized Properties.
     */
    <T> T initialize(Class<T> proxyInterface, ClassLoader classLoader);

    /**
     * Create a new {@link Builder} to facilitate building of an
     * {@link ExternalizedProperties} instance.
     * 
     * @return The builder for {@link ExternalizedProperties}.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link ExternalizedProperties}.
     */
    public static class Builder {
        private final List<Converter<?>> converters = new ArrayList<>();
        private final List<Processor> processors = new ArrayList<>();
        private final List<Resolver> resolvers = new ArrayList<>();
        private VariableExpander variableExpander = new SimpleVariableExpander();
        
        // Caching settings.
        private Duration cacheDuration = getDefaultCacheDuration();
        private boolean enableEagerLoading = false;
        private boolean enableInitializeCaching = false;
        private boolean enableInvocationCaching = false;

        // Default settings.
        private boolean enableDefaultResolvers = false;
        private boolean enableDefaultConverters = false;

        /**
         * Enable default configurations.
         * 
         * @return This builder.
         */
        public Builder defaults() {
            return enableDefaultResolvers()
                .enableDefaultConverters()
                .enableInitializeCaching()
                .enableInvocationCaching();
        }

        /**
         * Enable the default resolvers.
         * 
         * @return This builder.
         */
        public Builder enableDefaultResolvers() {
            this.enableDefaultResolvers = true;
            return this;
        }

        /**
         * Enable the default converters.
         * 
         * @return This builder.
         */
        public Builder enableDefaultConverters() {
            this.enableDefaultConverters = true;
            return this;
        }

        /**
         * Enable caching of initialized instances (per proxy interface).
         * 
         * @return This builder.
         */
        public Builder enableInitializeCaching() {
            this.enableInitializeCaching = true;
            return this;
        }

        /**
         * Enable caching of proxy invocation results.
         * 
         * @return This builder.
         */
        public Builder enableInvocationCaching() {
            this.enableInvocationCaching = true;
            return this;
        }

        /**
         * Eagerly resolve property values for candidate proxy methods.
         * 
         * @return This builder.
         */
        public Builder enableEagerLoading() {
            this.enableEagerLoading = true;
            return this;
        }

        /**
         * Sets the global cache duration.
         * 
         * @param cacheDuration The duration of caches before being reloaded.
         * @return This builder.
         */
        public Builder cacheDuration(Duration cacheDuration) {
            this.cacheDuration = requireNonNull(cacheDuration, "cacheDuration");
            return this;
        }

        /**
         * The array of {@link Resolver}s to provide resolver instances
         * to resolve properties from.
         * 
         * @param resolvers The resolvers to resolve properties from.
         * @return This builder.
         */
        public Builder resolvers(Resolver... resolvers) {
            requireNonNull(resolvers, "resolvers");
            return resolvers(Arrays.asList(resolvers));
        }

        /**
         * The collection of {@link Resolver}s to resolve properties from.
         * 
         * @param resolvers The resolvers to resolve properties from.
         * @return This builder.
         */
        public Builder resolvers(Collection<Resolver> resolvers) {
            requireNonNull(resolvers, "resolvers");
            this.resolvers.addAll(resolvers);
            return this;
        }

        /**
         * The array of {@link Converter}s to register.
         * 
         * @param converters The converters to register.
         * @return This builder.
         */
        public Builder converters(Converter<?>... converters) {
            requireNonNull(converters, "converters");
            return converters(Arrays.asList(converters));
        }

        /**
         * The collection of {@link Converter}s to register.
         * 
         * @param converters The converter providers.
         * @return This builder.
         */
        public Builder converters(Collection<Converter<?>> converters) {
            requireNonNull(converters, "converters");
            this.converters.addAll(converters);
            return this;
        }

        /**
         * The array of {@link Processor}s to register.
         * 
         * @param processors The processor to register.
         * @return This builder.
         */
        public Builder processors(Processor... processors) {
            requireNonNull(processors, "processors");
            return processors(Arrays.asList(processors));
        }

        /**
         * The collection of {@link Processor}s to register.
         * 
         * @param processors The processors to register.
         * @return This builder.
         */
        public Builder processors(Collection<Processor> processors) {
            requireNonNull(processors, "processors");
            this.processors.addAll(processors);
            return this;
        }

        /**
         * The {@link VariableExpander} to register.
         * 
         * @param variableExpander The {@link VariableExpander} to register.
         * @return This builder.
         */
        public Builder variableExpander(VariableExpander variableExpander) {
            requireNonNull(variableExpander, "variableExpander");
            this.variableExpander = variableExpander;
            return this;
        }

        /**
         * Build the {@link InternalExternalizedProperties} instance.
         * 
         * @return The built {@link InternalExternalizedProperties} instance.
         */
        public ExternalizedProperties build() {
            RootResolver rootResolver = buildRootResolver();
            RootConverter rootConverter = buildRootConverter();
            InvocationHandlerFactory invocationHandlerFactory = buildInvocationHandlerFactory();

            ExternalizedProperties externalizedProperties = new InternalExternalizedProperties(
                rootResolver, 
                rootConverter,
                invocationHandlerFactory
            );

            if (enableInitializeCaching) {
                return new CachingExternalizedProperties(
                    externalizedProperties,
                    new ExpiringCacheStrategy<>(
                        new WeakConcurrentHashMapCacheStrategy<>(),
                        cacheDuration   
                    )
                );
            }

            return externalizedProperties;
        }

        private InvocationHandlerFactory buildInvocationHandlerFactory() {
            // Default invocation handler factory.
            InvocationHandlerFactory base = 
                new ExternalizedPropertiesInvocationHandlerFactory();

            InvocationHandlerFactory factory = base;

            // Shared cache strategy.
            CacheStrategy<Method, Object> propertiesByMethodCache =
                new WeakConcurrentHashMapCacheStrategy<>();
            
            if (enableEagerLoading) {
                // Decorate with EagerLoadingInvocationHandler.
                factory = new EagerLoadingInvocationHandlerFactory(
                    factory, 
                    new ExpiringCacheStrategy<>(
                        propertiesByMethodCache,
                        cacheDuration
                    )
                );
            }

            if (enableInvocationCaching) {
                // Decorate with CachingInvocationHandler.
                factory = new CachingInvocationHandlerFactory(
                    factory, 
                    new ExpiringCacheStrategy<>(
                        propertiesByMethodCache,
                        cacheDuration
                    )
                );
            }

            return factory;
        }

        private RootResolver buildRootResolver() {
            // Add default resolvers last.
            // Custom resolvers always take precedence.
            if (enableDefaultResolvers) {
                resolvers(new DefaultResolver());
            }

            if (resolvers.isEmpty()) {
                throw new IllegalStateException("At least one resolver is required.");
            }

            return new RootResolver(
                resolvers, 
                buildRootProcessor(), 
                variableExpander
            );
        }

        private RootConverter buildRootConverter() {
            // Add default converters last.
            // Custom converters always take precedence.
            if (enableDefaultConverters) {
                converters(new DefaultConverter());
            }
            
            return new RootConverter(converters);
        }

        private RootProcessor buildRootProcessor() {
            return new RootProcessor(processors);
        }

        private Duration getDefaultCacheDuration() {
            return Duration.ofMinutes(Integer.parseInt(
                System.getProperty(
                    ExternalizedProperties.class.getName() + ".default-cache-duration", 
                    "30"
                )
            ));
        }
    }
}
