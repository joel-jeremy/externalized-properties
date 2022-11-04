package io.github.joeljeremy.externalizedproperties.core.internal.proxy;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.ConverterFacade;
import io.github.joeljeremy.externalizedproperties.core.InvocationArguments;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.TypeReference;
import io.github.joeljeremy.externalizedproperties.core.UnresolvedPropertyException;
import io.github.joeljeremy.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy.externalizedproperties.core.VariableExpanderFacade;
import io.github.joeljeremy.externalizedproperties.core.internal.DefaultInterfaceMethodHandlerFactory;
import io.github.joeljeremy.externalizedproperties.core.internal.DefaultInterfaceMethodHandlerFactory.DefaultInterfaceMethodHandler;
import io.github.joeljeremy.externalizedproperties.core.internal.ExternalizedPropertyName;
import io.github.joeljeremy.externalizedproperties.core.internal.Internal;
import io.github.joeljeremy.externalizedproperties.core.internal.InvocationContextFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

/** The core invocation handler for Externalized Properties. */
@Internal
public class ExternalizedPropertiesInvocationHandler implements InvocationHandler {
  private static final Object[] EMPTY_ARGS = new Object[0];

  private final Resolver rootResolver;
  private final Converter<?> rootConverter;
  private final VariableExpander variableExpander;
  private final InvocationContextFactory invocationContextFactory;
  private final DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory;

  /**
   * Constructor.
   *
   * @param rootResolver The root resolver.
   * @param rootConverter The root converter.
   * @param variableExpander The variable expander.
   * @param invocationContextFactory The proxy method factory.
   */
  public ExternalizedPropertiesInvocationHandler(
      Resolver rootResolver,
      Converter<?> rootConverter,
      VariableExpander variableExpander,
      InvocationContextFactory invocationContextFactory) {
    this.rootResolver = requireNonNull(rootResolver, "rootResolver");
    this.rootConverter = requireNonNull(rootConverter, "rootConverter");
    this.variableExpander = requireNonNull(variableExpander, "variableExpander");
    this.invocationContextFactory =
        requireNonNull(invocationContextFactory, "invocationContextFactory");
    this.defaultInterfaceMethodHandlerFactory = new DefaultInterfaceMethodHandlerFactory();
  }

  /** {@inheritDoc} */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // args is null instead of an empty array if there are no method parameters...
    // Let's change that to an empty array.
    args = args == null ? EMPTY_ARGS : args;

    // Handle invocations to native Object methods:
    // toString, equals, hashCode
    Object objectMethodResult = handleIfObjectMethod(proxy, method, args);
    if (objectMethodResult != null) {
      return objectMethodResult;
    }

    InvocationContext context = invocationContextFactory.create(proxy, method, args);

    // @ConverterFacade handling
    if (context.method().hasAnnotation(ConverterFacade.class)) {
      return handleConverterFacade(context);
    }
    // @VariableExpanderFacade handling.
    else if (context.method().hasAnnotation(VariableExpanderFacade.class)) {
      return handleVariableExpanderFacade(context);
    }

    // Resolve property (@ExternalizedProperty and @ResolverFacade handling)
    return resolveProperty(context, proxy, method, args);
  }

  private Object resolveProperty(
      InvocationContext context, Object proxy, Method method, Object[] args) {
    String externalizedPropertyName = ExternalizedPropertyName.fromInvocationContext(context);

    String expandedName = variableExpander.expandVariables(context, externalizedPropertyName);

    return rootResolver
        .resolve(context, expandedName)
        .map(resolved -> convert(context, resolved, determineTargetType(context)))
        .orElseGet(() -> determineDefaultValueOrThrow(context, proxy, method, args));
  }

  private String handleVariableExpanderFacade(InvocationContext context) {
    // No need to validate. Already validated when proxy was built.
    String valueToExpand = (String) context.arguments().getOrThrow(0);
    return variableExpander.expandVariables(context, valueToExpand);
  }

  private Object handleConverterFacade(InvocationContext context) {
    // No need to validate. Already validated when proxy was built.
    String valueToConvert = (String) context.arguments().getOrThrow(0);
    Type targetType = determineTargetType(context);
    return convert(context, valueToConvert, targetType);
  }

  private Object convert(InvocationContext context, String valueToConvert, Type targetType) {
    return rootConverter.convert(context, valueToConvert, targetType).value();
  }

  /**
   * Determine a default value for the externalized property method or throw. This will attempt to
   * do the following:
   *
   * <ol>
   *   <li>Invoke the method if it's a default interface method and return the value.
   *   <li>Return {@link Optional#empty()} if the method return type is an {@link Optional}.
   *   <li>Throw an {@link UnresolvedPropertyException}.
   * </ol>
   *
   * @param context The proxy method invocation context.
   * @param proxy The proxy.
   * @param method The proxy method.
   * @param args The arguments passed to the method.
   * @return The default value that shall be returned by the method.
   * @throws UnresolvedPropertyException if a default value cannot be determined.
   */
  private Object determineDefaultValueOrThrow(
      InvocationContext context, Object proxy, Method method, Object[] args) {
    if (method.isDefault()) {
      return invokeDefaultInterfaceMethod(proxy, method, args);
    }

    if (Optional.class.equals(method.getReturnType())) {
      return Optional.empty();
    }

    String externalizedPropertyName = ExternalizedPropertyName.fromInvocationContext(context);

    throw new UnresolvedPropertyException(
        externalizedPropertyName,
        String.format(
            "Failed to resolve property '%s' for proxy method (%s). "
                + "To prevent exceptions when a property cannot be resolved, "
                + "consider changing proxy interface method's return type to an Optional.",
            externalizedPropertyName, method.toGenericString()));
  }

  /**
   * Invoke the default interface method.
   *
   * @param proxy The proxy.
   * @param method The proxy method.
   * @param args The arguments to pass to the default interface method.
   * @return The result of the default interface method.
   */
  private Object invokeDefaultInterfaceMethod(Object proxy, Method method, Object[] args) {
    DefaultInterfaceMethodHandler handler = defaultInterfaceMethodHandlerFactory.create(method);
    return handler.invoke(proxy, args);
  }

  // Same handling for @ResolverFacade and @ConverterFacade.
  private static Type determineTargetType(InvocationContext context) {
    InvocationArguments invocationArgs = context.arguments();
    Type targetType = context.method().returnType();
    if (invocationArgs.count() < 2) {
      return targetType;
    }

    // Target type was provided as second parameter.
    Object arg = invocationArgs.getOrThrow(1);
    if (arg instanceof TypeReference<?>) {
      return ((TypeReference<?>) arg).type();
    }

    // Safe to cast as only allowed types are: TypeReference, Class, and Type.
    // Class is a subclass of Type. If new target types are supported, add it
    // here or else a ClassCastException will get thrown.
    return (Type) arg;
  }

  // Avoid invoking proxy object methods to avoid recursion.
  private static @Nullable Object handleIfObjectMethod(Object proxy, Method method, Object[] args) {
    if (method.getParameterCount() == 0) {
      if ("toString".equals(method.getName())) {
        return proxyToString(proxy);
      } else if ("hashCode".equals(method.getName())) {
        return proxyHashCode(proxy);
      }
    } else if (method.getParameterCount() == 1
        && "equals".equals(method.getName())
        && Object.class.equals(method.getParameterTypes()[0])) {
      return proxyEquals(proxy, args);
    }

    return null;
  }

  private static int proxyHashCode(Object proxy) {
    return System.identityHashCode(proxy);
  }

  // Only do reference equality.
  private static boolean proxyEquals(Object proxy, Object[] args) {
    return proxy == args[0];
  }

  private static String proxyToString(Object proxy) {
    return proxy.getClass().getName() + '@' + Integer.toHexString(proxyHashCode(proxy));
  }
}
