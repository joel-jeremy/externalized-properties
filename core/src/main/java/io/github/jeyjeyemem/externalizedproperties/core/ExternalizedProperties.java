package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.CachingExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.ExpiringCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.WeakConcurrentHashMapCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.CachingInvocationHandlerFactory;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.EagerLoadingInvocationHandlerFactory;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.ExternalizedPropertiesInvocationHandlerFactory;
import io.github.jeyjeyemem.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.InvocationHandlerFactory;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.jeyjeyemem.externalizedproperties.core.variableexpansion.SimpleVariableExpander;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The core API for Externalized Properties.
 */
public interface ExternalizedProperties {
    /**
     * Create a proxy that handles property resolution according to the invoked method.
     * 
     * @param <T> The type of the proxy interface.
     * @param proxyInterface The proxy interface whose methods are annotated with 
     * {@link ExternalizedProperty}. Only interfaces will be accepted. 
     * If a non-interface class is given, an exception will be thrown. 
     * @return The proxy instance implementing the specified interface.
     */
    <T> T proxy(Class<T> proxyInterface);

    /**
     * Create a proxy that handles property resolution according to the invoked method.
     * 
     * @implNote The names specified in the {@link ExternalizedProperty} supports variables
     * e.g. "${my.variable}.property". These variables will be expanded accordingly.
     * 
     * @param <T> The type of the proxy interface.
     * @param proxyInterface The interface whose methods are annotated with 
     * {@link ExternalizedProperty}. Only an interface will be accepted. 
     * If a non-interface is given, an exception will be thrown. 
     * 
     * @param classLoader The class loader to define the proxy class in.
     * @return The proxy instance implementing the specified interface.
     */
    <T> T proxy(Class<T> proxyInterface, ClassLoader classLoader);

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
        private final List<ResolverProvider<?>> resolverProviders = new ArrayList<>();
        private final List<ConverterProvider<?>> converterProviders = new ArrayList<>();
        private final List<ProcessorProvider<?>> processorProviders = new ArrayList<>();
        private VariableExpanderProvider<?> variableExpanderProvider = 
            SimpleVariableExpander.provider();
        
        // Caching settings.
        private Duration cacheDuration = getDefaultCacheDuration();
        private boolean withProxyEagerLoading = false;
        private boolean withProxyCaching = false;
        private boolean withProxyInvocationCaching = false;

        // Default settings.
        private boolean withDefaultResolvers = false;
        private boolean withDefaultConverters = false;

        /**
         * Enable default configurations.
         * 
         * @return This builder.
         */
        public Builder withDefaults() {
            return withDefaultResolvers()
                .withDefaultConverters()
                .withProxyCaching()
                .withProxyInvocationCaching();
        }

        /**
         * Adds the default resolvers to the registered {@link Resolver}s.
         * 
         * @return This builder.
         */
        public Builder withDefaultResolvers() {
            this.withDefaultResolvers = true;
            return this;
        }

        /**
         * Adds the default converters to the registered {@link Converter}s.
         * 
         * @return This builder.
         */
        public Builder withDefaultConverters() {
            this.withDefaultConverters = true;
            return this;
        }

        /**
         * Enable caching of proxy instances (per proxy interface).
         * 
         * @return This builder.
         */
        public Builder withProxyCaching() {
            this.withProxyCaching = true;
            return this;
        }

        /**
         * Enable caching of proxy invocation results.
         * 
         * @return This builder.
         */
        public Builder withProxyInvocationCaching() {
            this.withProxyInvocationCaching = true;
            return this;
        }

        /**
         * Eagerly resolve property values of proxy interface methods marked with 
         * {@link ExternalizedProperty} annotation.
         * 
         * @return This builder.
         */
        public Builder withProxyEagerLoading() {
            this.withProxyEagerLoading = true;
            return this;
        }

        /**
         * Sets the global cache duration.
         * 
         * @param cacheDuration The duration of caches before being reloaded.
         * @return This builder.
         */
        public Builder withCacheDuration(Duration cacheDuration) {
            this.cacheDuration = requireNonNull(cacheDuration, "cacheDuration");
            return this;
        }

        /**
         * The array of {@link ResolverProvider}s to provide resolver instances
         * to resolve properties from.
         * 
         * @param resolverProviders The resolver providers.
         * @return This builder.
         */
        public Builder resolvers(ResolverProvider<?>... resolverProviders) {
            requireNonNull(resolverProviders, "resolverProviders");
            return resolvers(Arrays.asList(resolverProviders));
        }

        /**
         * The collection of {@link ResolverProvider}s to provide resolver instances
         * to resolve properties from.
         * 
         * @param resolverProviders The resolver providers.
         * @return This builder.
         */
        public Builder resolvers(Collection<ResolverProvider<?>> resolverProviders) {
            requireNonNull(resolverProviders, "resolverProviders");
            this.resolverProviders.addAll(resolverProviders);
            return this;
        }

        /**
         * The array of {@link ConverterProvider}s to provide converters.
         * 
         * @param converterProviders The converter providers.
         * @return This builder.
         */
        public Builder converters(
                ConverterProvider<?>... converterProviders
        ) {
            requireNonNull(converterProviders, "converterProviders");
            return converters(Arrays.asList(converterProviders));
        }

        /**
         * The collection of {@link ConverterProvider}s to provide converters.
         * 
         * @param converterProviders The converter providers.
         * @return This builder.
         */
        public Builder converters(
                Collection<ConverterProvider<?>> converterProviders
        ) {
            requireNonNull(converterProviders, "converterProviders");
            this.converterProviders.addAll(converterProviders);
            return this;
        }

        /**
         * The array of {@link ProcessorProvider}s to register.
         * 
         * @param processorProviders The processor providers to register.
         * @return This builder.
         */
        public Builder processors(
                ProcessorProvider<?>... processorProviders
        ) {
            requireNonNull(processorProviders, "processorProviders");
            return processors(Arrays.asList(processorProviders));
        }

        /**
         * The collection of {@link ProcessorProvider}s to register.
         * 
         * @param processorProviders The processors to register.
         * @return This builder.
         */
        public Builder processors(
                Collection<ProcessorProvider<?>> processorProviders
        ) {
            requireNonNull(processorProviders, "processorProviders");
            this.processorProviders.addAll(processorProviders);
            return this;
        }

        /**
         * The {@link VariableExpanderProvider} to register.
         * 
         * @param variableExpanderProvider The variable expander provider to register.
         * @return This builder.
         */
        public Builder variableExpander(
                VariableExpanderProvider<?> variableExpanderProvider
        ) {
            requireNonNull(variableExpanderProvider, "variableExpanderProvider");
            this.variableExpanderProvider = variableExpanderProvider;
            return this;
        }

        /**
         * Build the {@link InternalExternalizedProperties} instance.
         * 
         * @return The built {@link InternalExternalizedProperties} instance.
         */
        public ExternalizedProperties build() {
            ResolverProvider<RootResolver> rootResolverProvider = buildRootResolverProvider();
            RootConverter.Provider rootConverterProvider = buildRootConverterProvider();
            InvocationHandlerFactory<?> invocationHandlerFactory = buildInvocationHandlerFactory();

            ExternalizedProperties externalizedProperties = new InternalExternalizedProperties(
                rootResolverProvider, 
                rootConverterProvider,
                invocationHandlerFactory
            );

            if (withProxyCaching) {
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

        private InvocationHandlerFactory<?> buildInvocationHandlerFactory() {
            // Default invocation handler factory.
            InvocationHandlerFactory<?> base = 
                new ExternalizedPropertiesInvocationHandlerFactory();

            InvocationHandlerFactory<?> factory = base;

            // Shared cache strategy.
            CacheStrategy<Method, Object> propertiesByMethodCache =
                new WeakConcurrentHashMapCacheStrategy<>();
            
            if (withProxyEagerLoading) {
                // Decorate with EagerLoadingInvocationHandler.
                factory = new EagerLoadingInvocationHandlerFactory(
                    factory, 
                    new ExpiringCacheStrategy<>(
                        propertiesByMethodCache,
                        cacheDuration
                    )
                );
            }

            if (withProxyInvocationCaching) {
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

        private ResolverProvider<RootResolver> buildRootResolverProvider() {
            // Add default resolvers last.
            // Custom resolvers always take precedence.
            if (withDefaultResolvers) {
                resolvers(DefaultResolver.provider());
            }

            if (resolverProviders.isEmpty()) {
                throw new IllegalStateException("At least one resolver is required.");
            }

            return RootResolver.provider(
                resolverProviders, 
                buildRootProcessorProvider(), 
                variableExpanderProvider
            );
        }

        private RootConverter.Provider buildRootConverterProvider() {
            // Add default converters last.
            // Custom converters always take precedence.
            if (withDefaultConverters) {
                converters(DefaultConverter.provider());
            }
            
            return RootConverter.provider(converterProviders);
        }

        private ProcessorProvider<RootProcessor> buildRootProcessorProvider() {
            return RootProcessor.provider(processorProviders);
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
