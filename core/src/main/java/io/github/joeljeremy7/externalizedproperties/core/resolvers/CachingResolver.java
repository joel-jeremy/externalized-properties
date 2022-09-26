package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import java.util.Optional;

/** A {@link Resolver} decorator which caches resolved properties for a specified duration. */
public class CachingResolver implements Resolver {
  private final Resolver decorated;
  private final CacheStrategy<String, String> cacheStrategy;

  /**
   * Constructor.
   *
   * @param decorated The decorated {@link Resolver} where properties will actually be resolved
   *     from.
   * @param cacheStrategy The cache strategy.
   */
  public CachingResolver(Resolver decorated, CacheStrategy<String, String> cacheStrategy) {
    this.decorated = requireNonNull(decorated, "decorated");
    this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> resolve(InvocationContext context, String propertyName) {
    Optional<String> cached = cacheStrategy.get(propertyName);
    if (cached.isPresent()) {
      return cached;
    }

    Optional<String> resolved = decorated.resolve(context, propertyName);
    if (resolved.isPresent()) {
      // Cache.
      cacheStrategy.cache(propertyName, resolved.get());
      return resolved;
    }

    return Optional.empty();
  }
}
