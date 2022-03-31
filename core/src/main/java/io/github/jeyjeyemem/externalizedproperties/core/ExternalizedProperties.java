package io.github.jeyjeyemem.externalizedproperties.core;

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
import io.github.jeyjeyemem.externalizedproperties.core.variableexpansion.SimpleVariableExpander;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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
     * The configured {@link Resolver} for this {@link ExternalizedProperties} instance.
     * 
     * @return The configured {@link Resolver} for this {@link ExternalizedProperties} 
     * instance.
     */
    Resolver resolver();

    /**
     * The configured {@link Converter} for this {@link ExternalizedProperties} instance.
     * 
     * @return The configured {@link Converter} for this {@link ExternalizedProperties} 
     * instance.
     */
    Converter<?> converter();

    /**
     * The configured {@link VariableExpander} for this {@link ExternalizedProperties} 
     * instance.
     * 
     * @return The configured {@link VariableExpander} for this 
     * {@link ExternalizedProperties} instance.
     */
    VariableExpander variableExpander();

    /**
     * The configured {@link Processor} for this {@link ExternalizedProperties} instance.
     * 
     * @return The configured {@link Processor} for this {@link ExternalizedProperties} 
     * instance.
     */
    Processor processor();

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
        private List<Resolver> resolvers = new ArrayList<>();
        private List<Converter<?>> converters = new ArrayList<>();
        private List<Processor> processingHandlers = new ArrayList<>();
        private Function<Resolver, VariableExpander> variableExpanderFactory =
            SimpleVariableExpander::new;
        
        // Caching settings.
        private Duration cacheDuration = getDefaultCacheDuration();
        private boolean withResolverCaching = false;
        private boolean withProxyEagerLoading = false;
        private boolean withProxyInvocationCaching = false;

        // Default settings.
        private boolean withDefaultResolvers = false;
        private boolean withDefaultConverters = false;

        /**
         * Enable default configurations. This will enable default {@link Resolver}s 
         * via the {@link #withDefaultResolvers()} and {@link Converter}s via the
         * {@link #withDefaultConverters()} methods. This will also enable caching via 
         * {@link #withResolverCaching()} and {@link #withProxyInvocationCaching()} methods.
         * 
         * @return This builder.
         */
        public Builder withDefaults() {
            return withDefaultResolvers()
                .withDefaultConverters()
                .withResolverCaching()
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
         * Enable caching of properties resolved via {@link Resolver}s.
         * 
         * @return This builder.
         */
        public Builder withResolverCaching() {
            this.withResolverCaching = true;
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
         * The array of {@link Resolver}s to resolve properties from.
         * 
         * @param resolvers The resolvers.
         * @return This builder.
         */
        public Builder resolvers(Resolver... resolvers) {
            requireNonNull(resolvers, "resolvers");
            return resolvers(Arrays.asList(resolvers));
        }

        /**
         * The collection of {@link Resolver}s to resolve properties from.
         * 
         * @param resolvers The resolvers.
         * @return This builder.
         */
        public Builder resolvers(Collection<Resolver> resolvers) {
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
        public Builder converters(
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
        public Builder converters(
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
        public Builder processors(
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
        public Builder processors(
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
        public Builder variableExpander(
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
            // Add default converters last.
            // Custom converters always take precedence.
            if (withDefaultConverters) {
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
}
