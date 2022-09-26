package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A {@link Resolver} implementation which resolves requested properties from a given properties
 * instance.
 */
public class PropertiesResolver extends MapResolver {
  /**
   * Constructor which builds from a {@link Properties} instance.
   *
   * @implNote Only properties with keys or values that are of type {@link String} are supported.
   *     Properties that do not meet this criteria will be ignored.
   * @implNote The {@link Properties} keys and values will be copied over to an internal {@link
   *     ConcurrentHashMap} for thread safety and to avoid the performance penalty of {@link
   *     Properties}/{@link Hashtable} synchronization.
   * @param properties The properties instance to build from.
   */
  public PropertiesResolver(Properties properties) {
    super(ignoreNonStringProperties(requireNonNull(properties, "properties")));
  }

  /**
   * Constructor which builds from a {@link Properties} instance.
   *
   * @implNote Only properties with keys or values that are of type {@link String} are supported.
   *     Properties that do not meet this criteria will be ignored.
   * @implNote The {@link Properties} keys and values will be copied over to an internal {@link
   *     ConcurrentHashMap} for thread safety and to avoid the performance penalty of {@link
   *     Properties}/{@link Hashtable} synchronization.
   * @param properties The source properties instance to build from.
   * @param unresolvedPropertyHandler Any properties not found in the source properties will tried
   *     to be resolved via this handler. This should accept a property name and return the property
   *     value for the given property name. {@code null} return values are allowed but will be
   *     discarded.
   */
  public PropertiesResolver(
      Properties properties, UnresolvedPropertyHandler unresolvedPropertyHandler) {
    super(
        ignoreNonStringProperties(requireNonNull(properties, "properties")),
        requireNonNull(unresolvedPropertyHandler, "unresolvedPropertyHandler"));
  }

  private static Map<String, String> ignoreNonStringProperties(Properties properties) {
    return properties.entrySet().stream()
        .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
        .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
  }
}
