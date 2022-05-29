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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNoNullElements;
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
    public static class Builder implements BuilderConfiguration {
        private final List<Resolver> resolvers = new ArrayList<>();
        private final List<Processor> processors = new ArrayList<>();
        private final List<Converter<?>> converters = new ArrayList<>();
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
         * Private constructor.
         */
        private Builder() {}

        /** {@inheritDoc} */
        @Override
        public Builder defaults() {
            return enableDefaultResolvers()
                .enableDefaultConverters()
                .enableInitializeCaching()
                .enableInvocationCaching();
        }

        /** {@inheritDoc} */
        @Override
        public Builder enableDefaultResolvers() {
            this.enableDefaultResolvers = true;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder enableDefaultConverters() {
            this.enableDefaultConverters = true;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder enableInitializeCaching() {
            this.enableInitializeCaching = true;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder enableInvocationCaching() {
            this.enableInvocationCaching = true;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder enableEagerLoading() {
            this.enableEagerLoading = true;
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder cacheDuration(Duration cacheDuration) {
            this.cacheDuration = requireNonNull(cacheDuration, "cacheDuration");
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public ProfileConfiguration onProfiles(String... targetProfiles) {
            requireNonNull(targetProfiles, "targetProfiles");
            return new ProfileConfiguration(this, targetProfiles);
        }

        /** {@inheritDoc} */
        @Override
        public Builder resolvers(Resolver... resolvers) {
            requireNoNullElements(resolvers, "resolvers");
            Collections.addAll(this.resolvers, resolvers);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder converters(Converter<?>... converters) {
            requireNoNullElements(converters, "converters");
            Collections.addAll(this.converters, converters);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder processors(Processor... processors) {
            requireNoNullElements(processors, "processors");
            Collections.addAll(this.processors, processors);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Builder variableExpander(VariableExpander variableExpander) {
            this.variableExpander = requireNonNull(
                variableExpander, 
                "variableExpander"
            );
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
                externalizedProperties = new CachingExternalizedProperties(
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
                Ordinals.sortResolvers(resolvers), 
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
            
            return new RootConverter(
                Ordinals.sortConverters(converters)
            );
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

    /**
     * Builder configurations for {@link ExternalizedProperties}.
     */
    public static interface BuilderConfiguration {
        /**
         * Enable default configurations.
         * 
         * @return This builder.
         */
        Builder defaults();

        /**
         * Enable the default resolvers.
         * 
         * @return This builder.
         */
        Builder enableDefaultResolvers();

        /**
         * Enable the default converters.
         * 
         * @return This builder.
         */
        Builder enableDefaultConverters();

        /**
         * Enable caching of initialized instances (per proxy interface).
         * 
         * @return This builder.
         */
        Builder enableInitializeCaching();

        /**
         * Enable caching of proxy invocation results.
         * 
         * @return This builder.
         */
        Builder enableInvocationCaching();

        /**
         * Eagerly resolve property values for candidate proxy methods.
         * 
         * @return This builder.
         */
        Builder enableEagerLoading();

        /**
         * Sets the global cache duration.
         * 
         * @param cacheDuration The duration of caches before being reloaded.
         * @return This builder.
         */
        Builder cacheDuration(Duration cacheDuration);

        /**
         * Profile-specific configurations that gets applied depending on the active profile. 
         * If no target profiles are specified, the configuration will be treated as a wildcard 
         * configuration such that it will be applied regardless of what the active profile is.
         * 
         * @param targetProfiles The profiles in which the configurations should be applied to.
         * If no target profiles are specified, the configuration will be treated as a wildcard
         * configuration such that it will be applied regardless of what the active profile is.
         * @return The profile configuration.
         */
        ProfileConfiguration onProfiles(String... targetProfiles);

        /**
         * Register {@link Resolver}s on which {@link ExternalizedProperties} will resolve 
         * properties from.
         * 
         * @apiNote If ordering is desired, resolvers can be given an ordinal
         * by using the {@link Ordinals#ordinalResolver(int, Resolver)} decorator method.
         * The lower the ordinal, the earlier the resolver will be placed in the resolver sequence.
         * 
         * @param resolvers The {@link Resolver}s to resolve properties from.
         * @return This builder.
         */
        Builder resolvers(Resolver... resolvers);

        /**
         * Register {@link Converter}s to be used by {@link ExternalizedProperties} for conversions.
         * 
         * @apiNote If ordering is desired, converters can be given an ordinal
         * by using the {@link Ordinals#ordinalConverter(int, Converter)} decorator method.
         * The lower the ordinal, the earlier the converter will be placed in the converter sequence.
         * 
         * @param converters The {@link Converter}s to register.
         * @return This builder.
         */
        Builder converters(Converter<?>... converters);

        /**
         * Register {@link Processor}s to be used by {@link ExternalizedProperties} for 
         * post-processing.
         * 
         * @param processors The {@link Processor}s to register.
         * @return This builder.
         */
        Builder processors(Processor... processors);

        /**
         * Register the {@link VariableExpander} to be used by {@link ExternalizedProperties} for
         * variable expansions.
         * 
         * @param variableExpander The {@link VariableExpander} to register.
         * @return This builder.
         */
        Builder variableExpander(VariableExpander variableExpander);
    }

    /**
     * Profile-specific configurations.
     */
    public static class ProfileConfiguration {
        private static final String ACTIVE_PROFILE_SYSTEM_PROPERTY = 
            "externalizedproperties.profile";
        private static final String ACTIVE_PROFILE_ENV_VARIABLE = 
            "EXTERNALIZEDPROPERTIES_PROFILE";
        
        private final Builder builder;
        private final String[] targetProfiles;

        /**
         * Private constructor.
         * 
         * @param builder The {@link ExternalizedProperties} builder.
         * @param targetProfiles The profiles in which this configuration should be applied to.
         * If no target profiles are specified, the configuration will be treated as a wildcard
         * configuration such that it will be applied regardless of what the active profile is.
         */
        private ProfileConfiguration(Builder builder, String... targetProfiles) {
            this.builder = builder;   
            this.targetProfiles = targetProfiles;
        }

        /**
         * Register a configurator if the set Externalized Properties profile is active.
         * 
         * @param profileConfigurator The profile configurator to apply if the
         * set Externalized Properties profile is active.
         * @return The builder.
         */
        public Builder configure(ProfileConfigurator profileConfigurator) {
            requireNonNull(profileConfigurator, "profileConfigurator");
            // Wildcard profile. Apply regardless of the active profile.
            if (targetProfiles.length == 0) {
                profileConfigurator.configure(activeProfile(), builder);
            } 
            else {
                for (String profile : targetProfiles) {
                    if (Objects.equals(profile, activeProfile())) {
                        profileConfigurator.configure(activeProfile(), builder);
                    }
                }
            }
            return builder;
        }

        private static String activeProfile() {
            return System.getProperty(
                ACTIVE_PROFILE_SYSTEM_PROPERTY,
                System.getenv().getOrDefault(ACTIVE_PROFILE_ENV_VARIABLE, "")
            );
        }
    }

    /**
     * Profile configurator.
     */
    public static interface ProfileConfigurator {
        /**
         * Configure based on the active Externalized Properties profile.
         * 
         * @param activeProfile The active Externalized Properties profile.
         * @param builder The Externalized Properties builder configuration.
         */
        void configure(String activeProfile, BuilderConfiguration builder);
    }
}
