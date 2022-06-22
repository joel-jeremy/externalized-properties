package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.CachingExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.internal.InvocationCacheKey;
import io.github.joeljeremy7.externalizedproperties.core.internal.SystemExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.internal.caching.ExpiringCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.caching.WeakConcurrentHashMapCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.ProfileLookup;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.CachingInvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.EagerLoadingInvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.ExternalizedPropertiesInvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.proxy.InvocationHandlerFactory;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.VariableExpandingResolver;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.NoOpVariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNoNullElements;
import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The core API for Externalized Properties. This provides methods to initialize proxies that
 * will redirect method invocations to Externalized Properties' handlers. The annotation 
 * provided by Externalized Properties will define how certain method invocations will be handled.
 * 
 * @see ExternalizedProperty
 * @see ResolverFacade
 * @see ConverterFacade
 * @see VariableExpanderFacade
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
    static class Builder implements BuilderConfiguration {
        private final List<ProfilesBuilder> profilesBuilders = new ArrayList<>();
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
        private boolean enableResolvedPropertyExpansion = false;

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
        public Builder enableVariableExpansionInProperties() {
            this.enableResolvedPropertyExpansion = true;
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
         * Profile-specific configurations that gets applied depending on the active profile. 
         * If no target profiles are specified, the configuration will be treated as a wildcard 
         * configuration such that it will be applied regardless of what the active profile is.
         * 
         * @param targetProfiles The profiles in which the configurations should be applied to.
         * If no target profiles are specified, the configuration will be treated as a wildcard
         * configuration such that it will be applied regardless of what the active profile is.
         * @return The profile builder for the target profiles.
         */
        public ProfilesBuilder onProfiles(String... targetProfiles) {
            requireNoNullElements(targetProfiles, "targetProfiles");
            return new ProfilesBuilder(this, targetProfiles);
        }

        /**
         * Build the {@link ExternalizedProperties} instance.
         * 
         * @return The built {@link ExternalizedProperties} instance.
         */
        public ExternalizedProperties build() {
            applyActiveProfileConfigurations();

            // At this point, profile configurations will have been applied. Let's build!
            ExternalizedProperties externalizedProperties = new SystemExternalizedProperties(
                buildRootResolver(resolvers, processors), 
                buildRootConverter(converters),
                variableExpander,
                buildInvocationHandlerFactory()
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

        /**
         * Resolve the active profile using non-profile-specific resolvers
         * and apply the applicable profile configurations based on that.
         */
        private void applyActiveProfileConfigurations() {
            resolveActiveProfile().ifPresent(activeProfile ->
                profilesBuilders.forEach(pc -> pc.runConfiguratorsFor(activeProfile))
            );
        }

        /**
         * Resolve the active Externalized Properties profile using the non-profile-specific 
         * resolvers.
         * 
         * @return The active Externalized Properties profile. Otherwise, an empty 
         * {@link Optional}.
         */
        private Optional<String> resolveActiveProfile() {
            // At this point, profile-specific configurations are not yet applied.
            // We are only determining the active profile at this point to know
            // which configurations should be applied.

            // Add a temporary default resolver to resolve active profile from.
            List<Resolver> profileResolvers = 
                Stream.concat(resolvers.stream(), Stream.of(new DefaultResolver()))
                    // Ignore exceptions since we are only using these to resolve 
                    // the active profile.
                    .map(ExceptionIgnoringResolver::new)
                    .collect(Collectors.toList());
            
            // We don't need fancy processors/converters/variable expanders and invocation 
            // handler factory here as we only need to use this to resolve the active profile 
            // which is a String.
            ExternalizedProperties resolverOnlyExternalizedProperties = 
                new SystemExternalizedProperties(
                    buildRootResolver(
                        profileResolvers, 
                        Collections.emptyList()
                    ), 
                    buildRootConverter(Collections.emptyList()),
                    NoOpVariableExpander.INSTANCE,
                    new ExternalizedPropertiesInvocationHandlerFactory()
                );

            ProfileLookup profileLookup = 
                resolverOnlyExternalizedProperties.initialize(ProfileLookup.class);
            
            // Treat blank profile as no profile.
            return profileLookup.activeProfile().filter(p -> !p.trim().isEmpty());
        }

        private InvocationHandlerFactory buildInvocationHandlerFactory() {
            // Default invocation handler factory.
            InvocationHandlerFactory base = 
                new ExternalizedPropertiesInvocationHandlerFactory();

            InvocationHandlerFactory factory = base;

            // Shared cache strategy.
            CacheStrategy<InvocationCacheKey, Object> propertiesByMethodCache =
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

        private Resolver buildRootResolver(
                List<Resolver> resolvers,
                List<Processor> processors
        ) {
            // Add default resolvers last.
            // Custom resolvers always take precedence.
            if (enableDefaultResolvers) {
                resolvers(new DefaultResolver());
            }

            Resolver rootResolver = new RootResolver(
                Ordinals.sortResolvers(resolvers), 
                buildRootProcessor(processors)
            );

            if (enableResolvedPropertyExpansion) {
                rootResolver = new VariableExpandingResolver(rootResolver);
            }
            
            return rootResolver;
        }

        private RootConverter buildRootConverter(List<Converter<?>> converters) {
            // Add default converters last.
            // Custom converters always take precedence.
            if (enableDefaultConverters) {
                converters(new DefaultConverter());
            }
            
            return new RootConverter(Ordinals.sortConverters(converters));
        }

        private RootProcessor buildRootProcessor(List<Processor> processors) {
            return new RootProcessor(processors);
        }

        private Builder registerProfilesBuilder(ProfilesBuilder profilesBuilder) {
            profilesBuilders.add(profilesBuilder);
            return this;
        }

        private static Duration getDefaultCacheDuration() {
            return Duration.ofMinutes(Integer.parseInt(
                System.getProperty(
                    ExternalizedProperties.class.getName() + ".default-cache-duration", 
                    "30"
                )
            ));
        }

        /**
         * Only used when resolving the active Externalized Properties profile.
         * We will ignore any exceptions from resolvers and move to the next one until 
         * we reach the first available resolver which has the active profile property.
         */
        private static class ExceptionIgnoringResolver implements Resolver {
            private static final Logger LOGGER = 
                Logger.getLogger(ExceptionIgnoringResolver.class.getName());
            
            private final Resolver decorated;
        
            private ExceptionIgnoringResolver(Resolver decorated) {
                this.decorated = requireNonNull(decorated, "decorated");
            }
        
            @Override
            public Optional<String> resolve(InvocationContext context, String propertyName) {
                try {
                    return decorated.resolve(context, propertyName);
                } catch (Throwable ex) {
                    // Ignore exception, but leave a log so user is made aware.
                    LOGGER.log(
                        Level.WARNING, 
                        "Exception occurred while resolving " + propertyName + ". Ignoring...", 
                        ex
                    );
                    return Optional.empty();
                }
            }
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
        BuilderConfiguration defaults();

        /**
         * Enable the default resolvers.
         * 
         * @return This builder.
         */
        BuilderConfiguration enableDefaultResolvers();

        /**
         * Enable the default converters.
         * 
         * @return This builder.
         */
        BuilderConfiguration enableDefaultConverters();

        /**
         * Enable caching of initialized instances (per proxy interface).
         * 
         * @return This builder.
         */
        BuilderConfiguration enableInitializeCaching();

        /**
         * Enable caching of proxy invocation results.
         * 
         * @return This builder.
         */
        BuilderConfiguration enableInvocationCaching();

        /**
         * Eagerly resolve property values for candidate proxy methods.
         * 
         * @return This builder.
         */
        BuilderConfiguration enableEagerLoading();

        /**
         * Expand variables in resolved properties.
         * 
         * @return This builder.
         */
        BuilderConfiguration enableVariableExpansionInProperties();

        /**
         * Sets the global cache duration.
         * 
         * @param cacheDuration The duration of caches before being reloaded.
         * @return This builder.
         */
        BuilderConfiguration cacheDuration(Duration cacheDuration);

        /**
         * Register {@link Resolver}s on which {@link ExternalizedProperties} will resolve 
         * properties from.
         * 
         * @apiNote If ordering is necessary, resolvers can be assigned an ordinal
         * via the {@link Ordinals#ordinalResolver(int, Resolver)} decorator method.
         * The lower the ordinal, the earlier the resolver will be placed in the resolver sequence.
         * 
         * @param resolvers The {@link Resolver}s to resolve properties from.
         * @return This builder.
         */
        BuilderConfiguration resolvers(Resolver... resolvers);

        /**
         * Register {@link Converter}s to be used by {@link ExternalizedProperties} for conversions.
         * 
         * @apiNote If ordering is necessary, converters can be assigned an ordinal
         * via the {@link Ordinals#ordinalConverter(int, Converter)} decorator method.
         * The lower the ordinal, the earlier the converter will be placed in the converter sequence.
         * 
         * @param converters The {@link Converter}s to register.
         * @return This builder.
         */
        BuilderConfiguration converters(Converter<?>... converters);

        /**
         * Register {@link Processor}s to be used by {@link ExternalizedProperties} for 
         * post-processing.
         * 
         * @param processors The {@link Processor}s to register.
         * @return This builder.
         */
        BuilderConfiguration processors(Processor... processors);

        /**
         * Register the {@link VariableExpander} to be used by {@link ExternalizedProperties} for
         * variable expansions.
         * 
         * @param variableExpander The {@link VariableExpander} to register.
         * @return This builder.
         */
        BuilderConfiguration variableExpander(VariableExpander variableExpander);
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

    /**
     * Build configurations that are specific to the target profiles.
     */
    public static class ProfilesBuilder {
        private final Builder builder;
        private final String[] targetProfiles;
        private final List<ProfileConfigurator> profileConfigurators = 
            new ArrayList<>();

        /**
         * Private constructor.
         * 
         * @param builder The {@link ExternalizedProperties} builder.
         * @param targetProfiles The profiles in which this configuration should be applied to.
         * If no target profiles are specified, the configuration will be treated as a wildcard
         * configuration such that it will be applied regardless of what the active profile is.
         */
        private ProfilesBuilder(Builder builder, String... targetProfiles) {
            // Self-register to builder.
            this.builder = builder.registerProfilesBuilder(this);   
            this.targetProfiles = targetProfiles;
        }

        /**
         * Apply the configurator if the set Externalized Properties profile is active.
         * 
         * @param profileConfigurators The profile configurators to apply if the target profile 
         * is active.
         * @return The builder.
         */
        public Builder apply(ProfileConfigurator... profileConfigurators) {
            requireNoNullElements(profileConfigurators, "profileConfigurators");
            if (profileConfigurators.length == 0) {
                throw new IllegalArgumentException(
                    "Please provide atleast 1 profile configurator."
                );
            }
            Collections.addAll(this.profileConfigurators, profileConfigurators);
            return builder;
        }

        private void runConfiguratorsFor(String activeProfile) {
            // Wildcard profile. Apply regardless of the active profile.
            if (targetProfiles.length == 0) {
                profileConfigurators.forEach(c -> c.configure(activeProfile, builder));
            } 
            else {
                for (String profile : targetProfiles) {
                    if (Objects.equals(profile, activeProfile)) {
                        profileConfigurators.forEach(pc -> pc.configure(activeProfile, builder));
                    }
                }
            }
        }
    }
}
