package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * A {@link Resolver} implementation which resolves requested properties 
 * from a given properties map.
 */
public class MapResolver implements Resolver {
    private final Map<String, String> propertySource;
    private final UnresolvedPropertyHandler unresolvedPropertyHandler;

    /**
     * Constructor.
     * 
     * @param propertySource The source map where requested properties will be derived from.
     */
    public MapResolver(Map<String, String> propertySource) {
        this(
            propertySource, 
            propertyName -> null
        );
    }

    /**
     * Constructor.
     * 
     * @param propertySource The source map where properties will be resolved from.
     * @param unresolvedPropertyHandler Any properties not found in the source properties will tried 
     * to be resolved via this handler. This should accept a property name and return the property value 
     * for the given property name. {@code null} return values are allowed but will be discarded.
     */
    public MapResolver(
            Map<String, String> propertySource,
            UnresolvedPropertyHandler unresolvedPropertyHandler
    ) {
        // Copy.
        this.propertySource = new ConcurrentHashMap<>(
            requireNonNull(propertySource, "propertySource")
        );
        this.unresolvedPropertyHandler = requireNonNull(
            unresolvedPropertyHandler,
            "unresolvedPropertyHandler"
        );
    }

    /**
     * The {@link ResolverProvider} for {@link MapResolver}.
     * 
     * @param propertySource The source map where properties will be resolved from.
     * @return The {@link ResolverProvider} for {@link MapResolver}.
     */
    public static ResolverProvider<MapResolver> provider(
            Map<String, String> propertySource 
    ) {
        requireNonNull(propertySource, "propertySource");
        return externalizedProperties -> new MapResolver(propertySource);
    }

    /**
     * The {@link ResolverProvider} for {@link MapResolver}.
     * 
     * @param propertySource The source map where properties will be resolved from.
     * @param unresolvedPropertyHandler Any properties not found in the source properties will tried 
     * to be resolved via this handler. This should accept a property name and return the property value 
     * for the given property name. {@code null} return values are allowed but will be discarded.
     * @return The {@link ResolverProvider} for {@link MapResolver}.
     */
    public static ResolverProvider<MapResolver> provider(
            Map<String, String> propertySource,
            UnresolvedPropertyHandler unresolvedPropertyHandler
    ) {
        requireNonNull(propertySource, "propertySource");
        requireNonNull(unresolvedPropertyHandler, "unresolvedPropertyHandler");

        return externalizedProperties -> new MapResolver(
            propertySource,
            unresolvedPropertyHandler
        );
    }
    
    /**
     * Resolve property from a given properties map.
     * 
     * @param proxyMethod The proxy method.
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    @Override
    public Optional<String> resolve(ProxyMethod proxyMethod, String propertyName) {
        return Optional.ofNullable(getPropertyOrNull(propertyName));
    }

    private @Nullable String getPropertyOrNull(String propertyName) {
        String propertyValue = propertySource.get(propertyName);
        if (propertyValue != null) {
            return propertyValue;
        }

        // Try to resolve from unresolved handler and cache result.
        propertyValue = unresolvedPropertyHandler.handle(propertyName);
        if (propertyValue != null) {
            propertySource.putIfAbsent(propertyName, propertyValue);
            return propertyValue;
        }

        // Unable to resolve property.
        return null;
    }

    /**
     * Any properties not found in the source properties will tried to be resolved via this handler. 
     * This should accept a property name and return the property value for the given property name. 
     * {@code null} return values are allowed but will be discarded.
     */
    public static interface UnresolvedPropertyHandler {
        /**
         * Try to resolve the value for the property name which {@link MapResolver} failed to resolve.
         * 
         * @param unresolvedPropertyName The name of the property which cannot be resolved from the
         * {@link MapResolver}.
         * @return The resolved property value. {@code null} is allowed but will be discarded.
         */
        @Nullable String handle(String unresolvedPropertyName);
    }
}
