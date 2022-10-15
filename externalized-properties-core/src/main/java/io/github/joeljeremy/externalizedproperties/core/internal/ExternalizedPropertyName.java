package io.github.joeljeremy.externalizedproperties.core.internal;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationArguments;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.ProxyMethod;
import io.github.joeljeremy.externalizedproperties.core.ResolverFacade;
import java.util.Optional;

/** Utility class to determine the externalized property name from proxy methods. */
public class ExternalizedPropertyName {
  private ExternalizedPropertyName() {}

  /**
   * Determine the externalized property name from the proxy method invocation.
   *
   * <ol>
   *   <li>If method is annotated with {@link ExternalizedProperty}, the externalized property name
   *       will be derived from {@link ExternalizedProperty#value()}.
   *   <li>If method is annotated with {@link ResolverFacade}, the externalized property name will
   *       be derived from the proxy method's invocation arguments.
   *   <li>Otherwise, the proxy method name will be used as externalized property name.
   * </ol>
   *
   * @see ExternalizedProperty
   * @see ResolverFacade
   * @param context The invocation context.
   * @return The externalized property name derived from {@link ExternalizedProperty#value()}, or
   *     from proxy method arguments if method is annotated with {@link ResolverFacade}. Otherwise,
   *     the proxy method name.
   */
  public static String fromInvocationContext(InvocationContext context) {
    ProxyMethod proxyMethod = context.method();
    ExternalizedProperty externalizedProperty =
        proxyMethod.findAnnotation(ExternalizedProperty.class).orElse(null);
    if (externalizedProperty != null) {
      return externalizedProperty.value();
    }

    ResolverFacade resolverFacade = proxyMethod.findAnnotation(ResolverFacade.class).orElse(null);
    if (resolverFacade != null) {
      return determineNameFromInvocationArgs(context.arguments());
    }

    return proxyMethod.name();
  }

  private static String determineNameFromInvocationArgs(InvocationArguments invocationArgs) {
    // @ResolverFacade externalized property name detection.
    // Check method invocation arguments for the externalized property name.
    Optional<String> nameFromArguments =
        invocationArgs
            .get(0)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .filter(a -> !a.isEmpty());

    if (!nameFromArguments.isPresent()) {
      throw new IllegalArgumentException(
          "Please provide the externalized property name via method arguments. "
              + "Only String values are allowed. Null or empty values are not allowed.");
    }

    return nameFromArguments.get();
  }
}
