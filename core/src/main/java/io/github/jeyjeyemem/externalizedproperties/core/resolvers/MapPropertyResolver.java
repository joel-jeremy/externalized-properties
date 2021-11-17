package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNullOrEmptyCollection;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNullOrEmptyString;

/**
 * {@link ExternalizedPropertyResolver} implementation which resolves requested properties 
 * from a given properties map.
 */
public class MapPropertyResolver implements ExternalizedPropertyResolver {
    private static final Function<String, String> NULL_UNRESOLVED_PROPERTY_HANDLER = 
        propertyName -> null;

    private final Map<String, String> propertySource;
    private final Function<String, String> unresolvedPropertyHandler;

    /**
     * Constructor.
     * 
     * @param propertySource The source map where requested properties will be derived from.
     */
    public MapPropertyResolver(Map<String, String> propertySource) {
        this(
            requireNonNull(propertySource, "propertySource"), 
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
     * building the {@link ExternalizedPropertyResolverResult}.
     */
    public MapPropertyResolver(
            Map<String, String> propertySource,
            Function<String, String> unresolvedPropertyHandler
    ) {
        this.propertySource = new HashMap<>(
            requireNonNull(propertySource, "propertySource")
        );
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
    public Optional<ResolvedProperty> resolve(String propertyName) {
        requireNonNullOrEmptyString(propertyName, "propertyName");

        return Optional.ofNullable(getPropertyOrNull(propertyName));
    }

    /**
     * Resolve properties from a given properties map.
     * 
     * @return The {@link ExternalizedPropertyResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ExternalizedPropertyResolverResult resolve(Collection<String> propertyNames) {
        requireNonNullOrEmptyCollection(propertyNames, "propertyNames");

        List<ResolvedProperty> resolvedProperties = new ArrayList<>(propertyNames.size());

        for (String propertyName : propertyNames) {
            throwIfNullOrEmptyValue(propertyName);
            ResolvedProperty resolved = getPropertyOrNull(propertyName);
            if (resolved != null) {
                resolvedProperties.add(resolved);
            }
        }

        return new ExternalizedPropertyResolverResult(
            propertyNames, 
            resolvedProperties
        );
    }

    private ResolvedProperty getPropertyOrNull(String propertyName) {
        String propertyValue = propertySource.get(propertyName);
        if (propertyValue == null) {
            // Try to resolve from unresolved handler and cache result.
            propertyValue = unresolvedPropertyHandler.apply(propertyName);
            if (propertyValue == null) {
                // Unable to resolve property.
                return null;
            }

            propertySource.putIfAbsent(propertyName, propertyValue);
        }
        
        return ResolvedProperty.with(propertyName, propertyValue);
    }

    private void throwIfNullOrEmptyValue(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Property name entries must not be null or empty.");
        }
    }
}
