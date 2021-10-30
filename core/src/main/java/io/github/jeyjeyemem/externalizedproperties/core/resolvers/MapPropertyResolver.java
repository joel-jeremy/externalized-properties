package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.Collections.requireNonNullOrEmptyCollection;

/**
 * {@link ExternalizedPropertyResolver} implementation which resolves requested properties 
 * from a given properties map.
 */
public class MapPropertyResolver implements ExternalizedPropertyResolver {

    private static final Function<String, String> NULL_UNRESOLVED_PROPERTY_HANDLER = 
        propertyName -> null;

    private final ConcurrentMap<String, String> propertySource;
    private final Function<String, String> unresolvedPropertyHandler;

    /**
     * Constructor which builds from a {@link Properties} instance.
     * 
     * @implNote Only properties with keys or values that are of type {@link String}
     * are supported. Properties that do not meet this criteria will be ignored.
     * 
     * @implNote The {@link Properties} keys and values will be copied over to an internal 
     * {@link ConcurrentHashMap} for thread safety and to avoid the performance penalty of 
     * {@link Properties}/{@link Hashtable} synchronization.
     * 
     * @param properties The properties instance to build from.
     */
    public MapPropertyResolver(Properties properties) {
        this(
            filterNonStringProperties(requireNonNull(properties, "properties")),
            NULL_UNRESOLVED_PROPERTY_HANDLER
        );
    }

    /**
     * Constructor which builds from a {@link Properties} instance.
     * 
     * @implNote Only properties with keys or values that are of type {@link String}
     * are supported. Properties that do not meet this criteria will be ignored.
     * 
     * @implNote The {@link Properties} keys and values will be copied over to an internal 
     * {@link ConcurrentHashMap} for thread safety and to avoid the performance penalty of 
     * {@link Properties}/{@link Hashtable} synchronization.
     * 
     * @param properties The source properties instance to build from.
     * @param unresolvedPropertyHandler Any properties not found in the source properties will tried 
     * to be resolved via this handler. This should accept a property name and return the property value 
     * for the given property name. {@code null} return values are allowed but will be discarded when 
     * building the {@link ExternalizedPropertyResolverResult}.
     */
    public MapPropertyResolver(
            Properties properties, 
            Function<String, String> unresolvedPropertyHandler
    ) {
        this(
            filterNonStringProperties(requireNonNull(properties, "properties")),
            requireNonNull(unresolvedPropertyHandler, "unresolvedPropertyHandler")
        );
    }

    /**
     * Constructor.
     * 
     * @implNote The {@link Map} keys and values will be copied over to an internal 
     * {@link ConcurrentHashMap} for thread safety.
     * 
     * @param propertySource The source map where requested properties will be derived from.
     */
    public MapPropertyResolver(Map<String, String> propertySource) {
        this(
            new ConcurrentHashMap<>(requireNonNull(propertySource, "propertySource")), 
            NULL_UNRESOLVED_PROPERTY_HANDLER
        );
    }

    /**
     * Constructor.
     * 
     * @implNote The {@link Map} keys and values will be copied over to an internal 
     * {@link ConcurrentHashMap} for thread safety.
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
        this(
            new ConcurrentHashMap<>(requireNonNull(propertySource, "propertySource")), 
            requireNonNull(unresolvedPropertyHandler, "unresolvedPropertyHandler")
        );
    }

    /**
     * Constructor.
     * 
     * @param propertySource The source concurrent map where requested properties will be derived from.
     * @param unresolvedPropertyHandler Any properties not found in the source properties will tried 
     * to be resolved via this handler. This should accept a property name and return the property value 
     * for the given property name. {@code null} return values are allowed but will be discarded when 
     * building the {@link ExternalizedPropertyResolverResult}.
     */
    private MapPropertyResolver(
            ConcurrentMap<String, String> propertySource,
            Function<String, String> unresolvedPropertyHandler
    ) {
        this.propertySource = propertySource;
        this.unresolvedPropertyHandler = unresolvedPropertyHandler;
    }
    
    /**
     * Resolve properties from a given properties map.
     * 
     * @return The {@link ExternalizedPropertyResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ExternalizedPropertyResolverResult resolve(Collection<String> propertyNames) {
        validate(propertyNames);

        List<ResolvedProperty> resolvedProperties = propertyNames.stream()
            .map(this::getPropertyOrNull)
            .filter(Objects::nonNull) // Discard null properties.
            .collect(Collectors.toList());

        return new ExternalizedPropertyResolverResult(
            propertyNames, 
            resolvedProperties
        );
    }

    private ResolvedProperty getPropertyOrNull(String propertyName) {
        String propValue = propertySource.computeIfAbsent(
            propertyName, 
            unresolvedPropertyHandler::apply
        );

        if (propValue == null) {
            return null;
        }
        
        return ResolvedProperty.with(propertyName, propValue);
    }

    private static ConcurrentMap<String, String> filterNonStringProperties(
            Properties properties
    ) {
        return properties.entrySet()
            .stream()
            .filter(e -> 
                e.getKey() instanceof String &&
                e.getValue() instanceof String
            )
            .collect(Collectors.toConcurrentMap(
                e -> (String)e.getKey(), 
                e -> (String)e.getValue()
            ));
    }

    private void validate(Collection<String> propertyNames) {
        requireNonNullOrEmptyCollection(propertyNames, "propertyNames");
        propertyNames.forEach(this::throwWhenNullOrEmptyValue);
    }

    private void throwWhenNullOrEmptyValue(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Property names must not be null or empty.");
        }
    }
}
