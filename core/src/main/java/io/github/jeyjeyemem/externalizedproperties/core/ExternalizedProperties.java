package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.DefaultPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalStringVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.internal.ExternalizedPropertyInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CompositePropertyResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The core API for Externalized Properties.
 */
public class ExternalizedProperties {

    private final ExternalizedPropertyResolver externalizedPropertyResolver;
    private final StringVariableExpander variableExpander;
    private final ResolvedPropertyConverter resolvedPropertyConverter;

    private ExternalizedProperties(
            ExternalizedPropertyResolver externalizedPropertyResolver,
            ResolvedPropertyConverter resolvedPropertyConverter,
            StringVariableExpander variableExpander
    ) {
        this.externalizedPropertyResolver = requireNonNull(
            externalizedPropertyResolver, 
            "externalizedPropertyResolver"
        );
        this.resolvedPropertyConverter = requireNonNull(
            resolvedPropertyConverter, 
            "resolvedPropertyConverter"
        );
        this.variableExpander = requireNonNull(variableExpander, "variableExpander");
    }

    /**
     * Create a proxy that proxies invocation of methods annotated with {@link ExternalizedProperty}.
     * 
     * @implNote The names specified in the {@link ExternalizedProperty} supports variables
     * e.g. "${MY_VARIABLE_NAME}-property". These variables will be expanded accordingly depending
     * on the provided {@link StringVariableExpander} implementation. By default,
     * the variable values will be resolved from system properties / environment variables if no
     * custom implementation is provided.
     * 
     * @param <T> The type of the proxy interface.
     * @param proxyInterface The interface whose methods are annotated with {@link ExternalizedProperty}. 
     * Only an interface will be accepted. If a non-interface is given, an exception will be thrown. 
     * @return The proxy instance implementing the specified interface.
     */
    public <T> T initialize(Class<T> proxyInterface) {
        return initialize(proxyInterface, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Create a proxy that proxies invocation of methods annotated with {@link ExternalizedProperty}.
     * 
     * @implNote The names specified in the {@link ExternalizedProperty} supports variables
     * e.g. "${MY_VARIABLE_NAME}-property". These variables will be expanded accordingly depending
     * on the provided {@link StringVariableExpander} implementation. By default,
     * the variable values will be resolved from system properties / environment variables if no
     * custom implementation is provided.
     * 
     * @param <T> The type of the proxy interface.
     * @param proxyInterface The interface whose methods are annotated with {@link ExternalizedProperty}. 
     * Only an interface will be accepted. If a non-interface is given, an exception will be thrown. 
     * @param classLoader The class loader to define the proxy class.
     * @return The proxy instance implementing the specified interface.
     */
    @SuppressWarnings("unchecked")
    public <T> T initialize(Class<T> proxyInterface, ClassLoader classLoader) {
        requireNonNull(proxyInterface, "proxyInterface");
        requireNonNull(classLoader, "classLoader");

        validate(proxyInterface);
        
        return (T) Proxy.newProxyInstance(
            classLoader, 
            new Class<?>[] { proxyInterface }, 
            new ExternalizedPropertyInvocationHandler(
                externalizedPropertyResolver,
                resolvedPropertyConverter,
                variableExpander
            )
        );
    }

    /**
     * Returns a {@link Builder} instance that can build an {@link ExternalizedProperties}.
     * 
     * @return A {@link Builder} instance that can build an {@link ExternalizedProperties}.
     */
    public static Builder builder() {
        return new Builder();
    }

    private <T> void validate(Class<T> proxyInterface) {
        requireNoVoidReturningMethods(proxyInterface);
    }

    private <T> void requireNoVoidReturningMethods(Class<T> proxyInterface) {
        List<String> voidReturningMethods = Arrays.stream(proxyInterface.getMethods())
            .filter(m -> m.getReturnType().equals(Void.TYPE))
            .map(Method::toGenericString)
            .collect(Collectors.toList());

        if (!voidReturningMethods.isEmpty()) {
            throw new IllegalArgumentException(
                "Proxy interface methods must not return void. " +
                "Invalid Methods: " + voidReturningMethods
            );
        }
    }

    /**
     * The builder for {@link ExternalizedProperties}.
     */
    public static class Builder {
        private List<ExternalizedPropertyResolver> externalizedPropertyResolvers = new ArrayList<>();
        private List<ResolvedPropertyConversionHandler<?>> resolvedPropertyConversionHandlers = 
            new ArrayList<>();

        // Caching resolver fields
        private Duration cacheItemLifetime;
        private ScheduledExecutorService expiryScheduler;
        private CacheStrategy cacheStrategy;

        private Builder(){}

        /**
         * The array of {@link ExternalizedPropertyResolver}s to resolve properties from.
         * 
         * @param externalizedPropertyResolvers The externalized property resolver.
         * @return This builder.
         */
        public Builder resolvers(
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
        public Builder resolvers(
                Collection<ExternalizedPropertyResolver> externalizedPropertyResolvers
        ) {
            requireNonNull(externalizedPropertyResolvers, "externalizedPropertyResolvers");

            this.externalizedPropertyResolvers.addAll(externalizedPropertyResolvers);
            return this;
        }

        /**
         * Adds the {@link DefaultPropertyConversionHandler} to the collection of 
         * {@link ExternalizedPropertyResolver}s.
         * 
         * @return This builder.
         */
        public Builder enableDefaultConversionHandlers() {
            return conversionHandlers(new DefaultPropertyConversionHandler());
        }

        /**
         * Adds the {@link CachingPropertyResolver} to the collection of 
         * {@link ExternalizedPropertyResolver}s.
         * 
         * @param cacheItemLifetime The duration of cache items in the cache before being expired.
         * @param expiryScheduler The cache item expiry scheduler.
         * @return This builder.
         */
        public Builder enableCachingResolver(
                Duration cacheItemLifetime,
                ScheduledExecutorService expiryScheduler
        ) {
            this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");
            this.expiryScheduler = requireNonNull(expiryScheduler, "expiryScheduler");
            return this;
        }

        /**
         * Adds the {@link CachingPropertyResolver} to the collection of 
         * {@link ExternalizedPropertyResolver}s.
         * 
         * @param cacheItemLifetime The duration of cache items in the cache before being expired.
         * @param expiryScheduler The cache item expiry scheduler.
         * @param cacheStrategy The cache strategy used by the caching resolver.
         * @return This builder.
         */
        public Builder enableCachingResolver(
                Duration cacheItemLifetime,
                ScheduledExecutorService expiryScheduler,
                CacheStrategy cacheStrategy
            ) {
            this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");
            this.expiryScheduler = requireNonNull(expiryScheduler, "expiryScheduler");
            this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
            return this;
        }

        /**
         * The array of {@link ResolvedPropertyConversionHandler}s to convert resolved properties
         * to various types.
         * 
         * @param resolvedPropertyConversionHandlers The resolved property conversion handlers.
         * @return This builder.
         */
        public Builder conversionHandlers(
                ResolvedPropertyConversionHandler<?>... resolvedPropertyConversionHandlers
        ) {
            requireNonNull(resolvedPropertyConversionHandlers, "resolvedPropertyConversionHandlers");

            return conversionHandlers(Arrays.asList(resolvedPropertyConversionHandlers));
        }

        /**
         * The collection of {@link ResolvedPropertyConversionHandler}s to convert resolved properties
         * to various types.
         * 
         * @param resolvedPropertyConversionHandlers The resolved property conversion handlers.
         * @return This builder.
         */
        public Builder conversionHandlers(
                Collection<ResolvedPropertyConversionHandler<?>> resolvedPropertyConversionHandlers
        ) {
            requireNonNull(resolvedPropertyConversionHandlers, "resolvedPropertyConversionHandlers");
            
            this.resolvedPropertyConversionHandlers.addAll(resolvedPropertyConversionHandlers);
            return this;
        }

        /**
         * Build the {@link ExternalizedProperties} instance.
         * 
         * @return The built {@link ExternalizedProperties} instance.
         */
        public ExternalizedProperties build() {
            ExternalizedPropertyResolver resolver = configureExternalizedPropertyResolver();

            return new ExternalizedProperties(
                resolver, 
                new InternalResolvedPropertyConverter(resolvedPropertyConversionHandlers),
                new InternalStringVariableExpander(resolver)
            );
        }

        private ExternalizedPropertyResolver configureExternalizedPropertyResolver() {
            if (externalizedPropertyResolvers.isEmpty()) {
                throw new IllegalStateException(
                    "At least one externalized property resolver is required."
                );
            }

            ExternalizedPropertyResolver resolver;
            
            if (externalizedPropertyResolvers.size() > 1) {
                resolver = new CompositePropertyResolver(
                    externalizedPropertyResolvers
                );
            } else {
                resolver = externalizedPropertyResolvers.get(0);
            }

            // Caching was enabled.
            if (cacheItemLifetime != null) {
                // Use custom cache strategy if present.
                resolver = cacheStrategy != null ?
                    new CachingPropertyResolver(resolver, cacheItemLifetime, expiryScheduler, cacheStrategy) :
                    new CachingPropertyResolver(resolver, cacheItemLifetime, expiryScheduler);
            }

            return resolver;
        }
    }
}
