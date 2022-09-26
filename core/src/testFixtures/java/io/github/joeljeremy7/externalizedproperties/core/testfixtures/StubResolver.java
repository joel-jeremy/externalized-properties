package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A stub {@link Resolver} implemenation that resolves properties based on a provided value resolver
 * function.
 *
 * <p>By default, it will resolve all properties by returning the property name suffixed with
 * "-value".
 */
public class StubResolver implements Resolver {

  private static final String DEFAULT_PROPERTY_NAME_SUFFIX = "-value";

  /** A delegate which returns the property name suffixed by "-value". */
  public static final Function<String, String> DEFAULT_DELEGATE =
      propertyName -> propertyName + DEFAULT_PROPERTY_NAME_SUFFIX;

  /** A delegate which always returns null. */
  public static final Function<String, String> NULL_DELEGATE = propertyName -> null;

  private final Map<String, String> trackedResolvedProperties = new HashMap<>();
  private final Function<String, String> delegate;

  /** Constructor. */
  public StubResolver() {
    this(DEFAULT_DELEGATE);
  }

  /**
   * Constructor.
   *
   * @param delegate The delegate to use in resolving properties.
   */
  public StubResolver(Function<String, String> delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> resolve(InvocationContext context, String propertyName) {
    String value = delegate.apply(propertyName);
    if (value != null) {
      // Add for tracking.
      trackedResolvedProperties.put(propertyName, value);

      return Optional.of(value);
    }
    return Optional.empty();
  }

  /**
   * The delegate used in resolving properties.
   *
   * @return The delegate used in resolving properties.
   */
  public Function<String, String> delegate() {
    return delegate;
  }

  /**
   * The properties resolved by this resolver.
   *
   * @return The properties resolved by this resolver.
   */
  public Map<String, String> resolvedProperties() {
    return Collections.unmodifiableMap(trackedResolvedProperties);
  }

  /**
   * The property names resolved by this resolver.
   *
   * @return The property names resolved by this resolver.
   */
  public Set<String> resolvedPropertyNames() {
    return Collections.unmodifiableSet(trackedResolvedProperties.keySet());
  }
}
