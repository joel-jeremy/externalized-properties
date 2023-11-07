package io.github.joeljeremy.externalizedproperties.core.internal;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

/** The {@link DefaultInterfaceMethodHandler} factory. */
@Internal
public class DefaultInterfaceMethodHandlerFactory {
  private static final String DEFAULT_INTERFACE_METHOD_EXCEPTION_MESSAGE_FORMAT =
      "Error occurred while invoking default interface method. Proxy method: %s.";

  private static final Map<Method, DefaultInterfaceMethodHandler> weakHandlerCache =
      new WeakHashMap<>();

  private DefaultInterfaceMethodHandlerFactory() {}

  /**
   * Create a {@link DefaultInterfaceMethodHandler} for the specified default interface method.
   *
   * @param defaultInterfaceMethod The default interface method.
   * @return The built {@link DefaultInterfaceMethodHandler}.
   * @throws Throwable if an exception occurred while creating the default interface method handler.
   */
  public static DefaultInterfaceMethodHandler create(Method defaultInterfaceMethod)
      throws Throwable {
    DefaultInterfaceMethodHandler cachedHandler = weakHandlerCache.get(defaultInterfaceMethod);
    if (cachedHandler != null) {
      return cachedHandler;
    }

    if (!defaultInterfaceMethod.isDefault()) {
      throw new IllegalArgumentException(
          defaultInterfaceMethod.toGenericString() + " is not a default interface method.");
    }

    // Note: We optimize for methods with up to 2 arguments. We create lambda functions for better
    // performance. This number may change in the future.

    // Optimization for default interface methods that have no args.
    if (defaultInterfaceMethod.getParameterCount() == 0) {
      return cache(defaultInterfaceMethod, createNoArgLambda(defaultInterfaceMethod));
    }
    // Optimization for default interface methods that have one arg.
    else if (defaultInterfaceMethod.getParameterCount() == 1) {
      return cache(defaultInterfaceMethod, createOneArgLambda(defaultInterfaceMethod));
    }
    // Optimization for default interface methods that have two args.
    else if (defaultInterfaceMethod.getParameterCount() == 2) {
      return cache(defaultInterfaceMethod, createTwoArgsLambda(defaultInterfaceMethod));
    } else {
      // Fallback to using method handles.
      MethodHandle methodHandle =
          MethodHandleFactory.methodHandleFor(
                  defaultInterfaceMethod, defaultInterfaceMethod.getDeclaringClass())
              .asSpreader(Object[].class, defaultInterfaceMethod.getParameterCount());

      return cache(
          defaultInterfaceMethod,
          (instance, args) -> {
            try {
              return methodHandle.invoke(instance, args);
            } catch (Throwable e) {
              throw new ExternalizedPropertiesException(
                  String.format(
                      "Error occurred while invoking default interface method. "
                          + "Proxy method: %s.",
                      defaultInterfaceMethod.toGenericString()),
                  e);
            }
          });
    }
  }

  private static DefaultInterfaceMethodHandler cache(
      Method defaultInterfaceMethod, DefaultInterfaceMethodHandler handler) {

    weakHandlerCache.put(defaultInterfaceMethod, handler);
    return handler;
  }

  private static DefaultInterfaceMethodHandler createNoArgLambda(Method defaultInterfaceMethod)
      throws Throwable {
    NoArgLambdaFunction lambda =
        LambdaFactory.createLambdaFunction(
            defaultInterfaceMethod,
            NoArgLambdaFunction.class,
            defaultInterfaceMethod.getDeclaringClass());

    return (instance, args) -> {
      try {
        return lambda.invoke(instance);
      } catch (Throwable e) {
        throw new ExternalizedPropertiesException(
            String.format(
                DEFAULT_INTERFACE_METHOD_EXCEPTION_MESSAGE_FORMAT,
                defaultInterfaceMethod.toGenericString()),
            e);
      }
    };
  }

  private static DefaultInterfaceMethodHandler createOneArgLambda(Method defaultInterfaceMethod)
      throws Throwable {

    OneArgLambdaFunction lambda =
        LambdaFactory.createLambdaFunction(
            defaultInterfaceMethod,
            OneArgLambdaFunction.class,
            defaultInterfaceMethod.getDeclaringClass());

    return (instance, args) -> {
      try {
        return lambda.invoke(instance, args[0]);
      } catch (Throwable e) {
        throw new ExternalizedPropertiesException(
            String.format(
                DEFAULT_INTERFACE_METHOD_EXCEPTION_MESSAGE_FORMAT,
                defaultInterfaceMethod.toGenericString()),
            e);
      }
    };
  }

  private static DefaultInterfaceMethodHandler createTwoArgsLambda(Method defaultInterfaceMethod)
      throws Throwable {

    TwoArgsLambdaFunction lambda =
        LambdaFactory.createLambdaFunction(
            defaultInterfaceMethod,
            TwoArgsLambdaFunction.class,
            defaultInterfaceMethod.getDeclaringClass());

    return (instance, args) -> {
      try {
        return lambda.invoke(instance, args[0], args[1]);
      } catch (Throwable e) {
        throw new ExternalizedPropertiesException(
            String.format(
                DEFAULT_INTERFACE_METHOD_EXCEPTION_MESSAGE_FORMAT,
                defaultInterfaceMethod.toGenericString()),
            e);
      }
    };
  }

  /** Handler for invoking default interface methods. */
  @FunctionalInterface
  @Internal
  public static interface DefaultInterfaceMethodHandler {
    /**
     * Invoke the target default interface method.
     *
     * @param instance The object whose class declares the default interface method.
     * @param args The method arguments.
     * @return The method result.
     */
    Object invoke(Object instance, Object... args);
  }

  /** Interface used internally by the factory to generate a lambda function. */
  @FunctionalInterface
  @Internal
  public static interface NoArgLambdaFunction {
    /**
     * Invoke the target default interface method.
     *
     * @param instance The object whose class declares the default interface method.
     * @return The method result.
     */
    Object invoke(Object instance);
  }

  /** Interface used internally by the factory to generate a lambda function. */
  @FunctionalInterface
  @Internal
  public static interface OneArgLambdaFunction {
    /**
     * Invoke the target default interface method.
     *
     * @param instance The object whose class declares the default interface method.
     * @param arg The method argument.
     * @return The method result.
     */
    Object invoke(Object instance, Object arg);
  }

  /** Interface used internally by the factory to generate a lambda function. */
  @FunctionalInterface
  @Internal
  public static interface TwoArgsLambdaFunction {
    /**
     * Invoke the target default interface method.
     *
     * @param instance The object whose class declares the default interface method.
     * @param arg1 The first method argument.
     * @param arg2 The second method argument.
     * @return The method result.
     */
    Object invoke(Object instance, Object arg1, Object arg2);
  }
}
