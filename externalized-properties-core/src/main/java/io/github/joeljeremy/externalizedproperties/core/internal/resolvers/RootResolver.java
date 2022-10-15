package io.github.joeljeremy.externalizedproperties.core.internal.resolvers;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Processor;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy.externalizedproperties.core.resolvers.CompositeResolver;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The root {@link Resolver}. All requests to resolve properties are routed through this resolver
 * and delegated to the registered {@link Resolver}s. This holds all registered {@link Resolver}s
 * and {@link Processor}s and takes care of resolving properties from the registered resolvers, and
 * applying post-processing using the registered processors.
 */
public class RootResolver implements Resolver {
  private final Resolver resolver;
  private final RootProcessor rootProcessor;

  /**
   * Constructor.
   *
   * @apiNote If ordering of resolvers is important, callers of this constructor must use a {@link
   *     Collection} implementation that supports ordering such as {@link List}.
   * @param resolvers The collection of {@link Resolver}s to resolve properties from.
   * @param rootProcessor The root processor.
   */
  public RootResolver(Collection<Resolver> resolvers, RootProcessor rootProcessor) {
    requireNonNull(resolvers, "resolvers");
    requireNonNull(rootProcessor, "rootProcessor");

    this.resolver = CompositeResolver.flatten(resolvers);
    this.rootProcessor = rootProcessor;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> resolve(InvocationContext context, String propertyName) {
    return resolver
        .resolve(context, propertyName)
        .map(resolved -> rootProcessor.process(context, resolved));
  }
}
