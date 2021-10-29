package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.DefaultPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.ExternalizedPropertyInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CompositePropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver.CacheStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The core API for Externalized Properties.
 */
public class ExternalizedProperties {

    private final ExternalizedPropertyResolver externalizedPropertyResolver;
    private final VariableExpander variableExpander;
    private final ResolvedPropertyConverter resolvedPropertyConverter;

    private ExternalizedProperties(
            ExternalizedPropertyResolver externalizedPropertyResolver,
            VariableExpander variableExpander,
            ResolvedPropertyConverter resolvedPropertyConverter
    ) {
        this.externalizedPropertyResolver = requireNonNull(
            externalizedPropertyResolver, 
            "externalizedPropertyResolver"
        );
        this.variableExpander = requireNonNull(variableExpander, "variableExpander");
        this.resolvedPropertyConverter = requireNonNull(
            resolvedPropertyConverter, 
            "resolvedPropertyConverter"
        );
    }

    /**
     * Create a proxy that proxies invocation of methods annotated with {@link ExternalizedProperty}.
     * 
     * @implNote The names specified in the {@link ExternalizedProperty} supports variables
     * e.g. "${MY_VARIABLE_NAME}-property". These variables will be expanded accordingly depending
     * on the provided {@link VariableExpander} implementation. By default,
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
     * on the provided {@link VariableExpander} implementation. By default,
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
                variableExpander,
                resolvedPropertyConverter
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
        private Duration cacheItemLifetime;
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
         * @return This builder.
         */
        public Builder enableCachingResolver(Duration cacheItemLifetime) {
            this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");
            return this;
        }

        /**
         * Adds the {@link CachingPropertyResolver} to the collection of 
         * {@link ExternalizedPropertyResolver}s.
         * 
         * @param cacheItemLifetime The duration of cache items in the cache before being expired.
         * @param cacheStrategy The cache strategy used by the caching resolver.
         * @return This builder.
         */
        public Builder enableCachingResolver(
                Duration cacheItemLifetime, 
                CacheStrategy cacheStrategy
            ) {
            this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");
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
                new InternalVariableExpander(resolver), 
                new InternalResolvedPropertyConverter(resolvedPropertyConversionHandlers)
            );
        }

        private ExternalizedPropertyResolver configureExternalizedPropertyResolver() {
            if (externalizedPropertyResolvers.isEmpty()) {
                throw new IllegalStateException(
                    "Atleast one externalized property resolver is required."
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
                    new CachingPropertyResolver(resolver, cacheItemLifetime, cacheStrategy) :
                    new CachingPropertyResolver(resolver, cacheItemLifetime);
            }

            return resolver;
        }
    }
}
