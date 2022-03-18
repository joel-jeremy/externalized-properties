package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyCollection;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * A {@link Resolver} implementation which resolves requested properties 
 * from a given properties map.
 */
public class MapResolver implements Resolver {
    private static final Function<String, String> NULL_UNRESOLVED_PROPERTY_HANDLER = 
        propertyName -> null;

    private final Map<String, String> propertySource;
    private final Function<String, String> unresolvedPropertyHandler;

    /**
     * Constructor.
     * 
     * @param propertySource The source map where requested properties will be derived from.
     * 
     * @apiNote The property source map must be a mutable map as this resolver will attempt 
     * to add any unresolved properties to the map using the configured unresolved property handler.
     */
    public MapResolver(Map<String, String> propertySource) {
        this(
            propertySource, 
            NULL_UNRESOLVED_PROPERTY_HANDLER
        );
    }

    /**
     * Constructor.
     * 
     * @param propertySource The source map where requested properties will be derived from.
     * @param unresolvedPropertyHandler Any properties not found in the source properties will tried 
     * to be resolved via this handler. This should accept a property name and return the property value 
     * for the given property name. {@code null} return values are allowed but will be discarded when 
     * building the {@link ResolverResult}.
     * 
     * @apiNote The property source map must be a mutable map as this resolver will attempt 
     * to add any unresolved properties to the map using the configured unresolved property handler.
     */
    public MapResolver(
            Map<String, String> propertySource,
            Function<String, String> unresolvedPropertyHandler
    ) {
        this.propertySource = requireNonNull(propertySource, "propertySource");
        this.unresolvedPropertyHandler = requireNonNull(
            unresolvedPropertyHandler,
            "unresolvedPropertyHandler"
        );
    }
    
    /**
     * Resolve property from a given properties map.
     * 
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    @Override
    public Optional<String> resolve(String propertyName) {
        requireNonNullOrEmptyString(propertyName, "propertyName");

        return Optional.ofNullable(getPropertyOrNull(propertyName));
    }

    /**
     * Resolve properties from a given properties map.
     * 
     * @return The {@link ResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ResolverResult resolve(Collection<String> propertyNames) {
        requireNonNullOrEmptyCollection(propertyNames, "propertyNames");

        ResolverResult.Builder resultBuilder = ResolverResult.builder(propertyNames);

        for (String propertyName : propertyNames) {
            throwIfNullOrEmptyValue(propertyName);
            String resolvedPropertyValue = getPropertyOrNull(propertyName);
            if (resolvedPropertyValue != null) {
                resultBuilder.add(propertyName, resolvedPropertyValue);
            }
        }

        return resultBuilder.build();
    }

    private String getPropertyOrNull(String propertyName) {
        String propertyValue = propertySource.get(propertyName);
        if (propertyValue != null) {
            return propertyValue;
        }

        // Try to resolve from unresolved handler and cache result.
        propertyValue = unresolvedPropertyHandler.apply(propertyName);
        if (propertyValue != null) {
            propertySource.putIfAbsent(propertyName, propertyValue);
            return propertyValue;
        }

        // Unable to resolve property.
        return null;
    }

    private void throwIfNullOrEmptyValue(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Property name entries must not be null or empty.");
        }
    }
}
