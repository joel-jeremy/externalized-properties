package io.github.joeljeremy.externalizedproperties.core.internal;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Factory for lambda functions created via {@code LambdaMetafactory}. */
@Internal
public class LambdaFactory {
  private static final LambdaFactory.FunctionalInterfaceMethodMap FUNCTIONAL_INTERFACE_METHOD_MAP =
      new FunctionalInterfaceMethodMap();

  private LambdaFactory() {}

  /**
   * Create a lambda function using {@code LambdaMetafactory}.
   *
   * @param <T> The functional interface.
   * @param targetMethod The default interface method which will be targeted by the lambda function.
   * @param functionalInterface The interface to serve as the functional interface. The functional
   *     interface must always accept an instance as first parameter (in addition to the expected
   *     method parameters) as the created lambda is not bound to any instance.
   * @return The instantiated lambda function which targets the specified target method.
   * @throws Throwable if an exception occurred while creating lambda function.
   */
  public static <T> T createLambdaFunction(Method targetMethod, Class<T> functionalInterface)
      throws Throwable {
    return createLambdaFunction(targetMethod, functionalInterface, null);
  }

  /**
   * Create a lambda function using {@code LambdaMetafactory}.
   *
   * @param <T> The functional interface.
   * @param targetMethod The default interface method which will be targeted by the lambda function.
   * @param functionalInterface The interface to serve as the functional interface. The functional
   *     interface must always accept an instance as first parameter (in addition to the expected
   *     method parameters) as the created lambda is not bound to any instance.
   * @param specialCaller The class nominally calling the method or {@code null} if there is no
   *     special caller.
   * @return The instantiated lambda function which targets the specified target method.
   * @throws Throwable if an exception occurred while creating lambda function.
   */
  public static <T> T createLambdaFunction(
      Method targetMethod, Class<T> functionalInterface, @Nullable Class<?> specialCaller)
      throws Throwable {
    MethodType invokedType = MethodType.methodType(functionalInterface);

    Method samMethod = FUNCTIONAL_INTERFACE_METHOD_MAP.get(functionalInterface);
    MethodType samMethodType =
        MethodType.methodType(samMethod.getReturnType(), samMethod.getParameterTypes());

    MethodHandle requestHandlerMethodHandle;
    MethodHandles.Lookup lookup;

    if (specialCaller != null) {
      requestHandlerMethodHandle = MethodHandleFactory.methodHandleFor(targetMethod, specialCaller);
      lookup = MethodHandleFactory.privateLookupIn(specialCaller);
    } else {
      requestHandlerMethodHandle = MethodHandleFactory.methodHandleFor(targetMethod);
      lookup = MethodHandleFactory.privateLookupIn(targetMethod.getDeclaringClass());
    }

    MethodType instantiatedMethodType =
        MethodType.methodType(
            targetMethod.getReturnType(), lookup.lookupClass(), targetMethod.getParameterTypes());

    CallSite callSite =
        LambdaMetafactory.metafactory(
            lookup,
            samMethod.getName(),
            invokedType,
            samMethodType,
            requestHandlerMethodHandle,
            instantiatedMethodType);

    return (T) callSite.getTarget().invoke();
  }

  private static class FunctionalInterfaceMethodMap extends ClassValue<Method> {
    /** Get the single abstract method (SAM) of the functional interface. */
    @Override
    protected Method computeValue(Class<?> functionalInterface) {
      Method[] methods =
          Stream.of(functionalInterface)
              .filter(Class::isInterface)
              .flatMap(m -> Stream.of(m.getMethods()))
              .filter(m -> Modifier.isAbstract(m.getModifiers()))
              .toArray(Method[]::new);

      if (methods.length != 1) {
        throw new IllegalArgumentException(
            "Class is not a functional interface: " + functionalInterface.getName());
      }

      return methods[0];
    }
  }
}
