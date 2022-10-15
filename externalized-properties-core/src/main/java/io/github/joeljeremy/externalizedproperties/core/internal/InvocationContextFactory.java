package io.github.joeljeremy.externalizedproperties.core.internal;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.InvocationArguments;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.ProxyMethod;
import io.github.joeljeremy.externalizedproperties.core.TypeUtilities;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

/** The factory for {@link InvocationContext}. */
public class InvocationContextFactory {

  private final ExternalizedProperties externalizedProperties;

  /**
   * Constructor.
   *
   * @param externalizedProperties The {@link ExternalizedProperties} instance.
   */
  public InvocationContextFactory(ExternalizedProperties externalizedProperties) {
    this.externalizedProperties = requireNonNull(externalizedProperties, "externalizedProperties");
  }

  /**
   * Create a context object for the proxy method invocation.
   *
   * @param proxy The proxy which declares the invoked proxy method.
   * @param method The invoked proxy method.
   * @param args The invocation arguments.
   * @return A context object for the proxy method invocation.
   */
  public InvocationContext create(Object proxy, Method method, Object[] args) {
    return new SystemInvocationContext(externalizedProperties, method, args);
  }

  /** Built-in invocation context. */
  private static class SystemInvocationContext implements InvocationContext {
    private final ExternalizedProperties externalizedProperties;
    private final ProxyMethod proxyMethod;
    private final InvocationArguments invocationArgs;

    /**
     * Constructor.
     *
     * @param externalizedProperties The {@link ExternalizedProperties} instance that initialized
     *     the proxy which declares the invoked method.
     * @param method The invoked proxy method.
     * @param args The proxy method invocation arguments.
     */
    private SystemInvocationContext(
        ExternalizedProperties externalizedProperties, Method method, Object[] args) {
      this.externalizedProperties = externalizedProperties;
      this.proxyMethod = new SystemProxyMethod(method);
      this.invocationArgs = new SystemInvocationArguments(args);
    }

    /** {@inheritDoc} */
    @Override
    public ExternalizedProperties externalizedProperties() {
      return externalizedProperties;
    }

    /** {@inheritDoc} */
    @Override
    public ProxyMethod method() {
      return proxyMethod;
    }

    /** {@inheritDoc} */
    @Override
    public InvocationArguments arguments() {
      return invocationArgs;
    }
  }

  /** Built-in proxy method. */
  private static class SystemProxyMethod implements ProxyMethod {

    private final Method method;

    /**
     * Constructor.
     *
     * @param method The method.
     */
    private SystemProxyMethod(Method method) {
      this.method = method;
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] annotations() {
      return method.getAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
      return Optional.ofNullable(method.getAnnotation(annotationClass));
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass) {
      return method.getAnnotation(annotationClass) != null;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> declaringClass() {
      return method.getDeclaringClass();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
      return method.getName();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> rawReturnType() {
      return method.getReturnType();
    }

    /** {@inheritDoc} */
    @Override
    public Type returnType() {
      return method.getGenericReturnType();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?>[] rawParameterTypes() {
      return method.getParameterTypes();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<?>> rawParameterTypeAt(int parameterIndex) {
      Class<?>[] rawParameterTypes = method.getParameterTypes();
      if (parameterIndex >= rawParameterTypes.length) {
        return Optional.empty();
      }

      return Optional.ofNullable(rawParameterTypes[parameterIndex]);
    }

    /** {@inheritDoc} */
    @Override
    public Type[] parameterTypes() {
      return method.getGenericParameterTypes();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Type> parameterTypeAt(int parameterIndex) {
      Type[] parameterTypes = method.getGenericParameterTypes();
      if (parameterIndex >= parameterTypes.length) {
        return Optional.empty();
      }

      return Optional.ofNullable(parameterTypes[parameterIndex]);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasReturnType(Class<?> type) {
      return rawReturnType().equals(type);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasReturnType(Type type) {
      return returnType().equals(type);
    }

    /** {@inheritDoc} */
    @Override
    public Type[] typeParametersOfReturnType() {
      return TypeUtilities.getTypeParameters(returnType());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Type> typeParameterOfReturnTypeAt(int typeParameterIndex) {
      Type[] genericTypeParameters = TypeUtilities.getTypeParameters(returnType());
      if (typeParameterIndex >= genericTypeParameters.length) {
        return Optional.empty();
      }

      return Optional.ofNullable(genericTypeParameters[typeParameterIndex]);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDefaultInterfaceMethod() {
      return method.isDefault();
    }

    /** {@inheritDoc} */
    @Override
    public String methodSignatureString() {
      return method.toGenericString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return methodSignatureString();
    }
  }

  /** Built-in invocation arguments. */
  private static class SystemInvocationArguments implements InvocationArguments {

    private final Object[] args;

    /**
     * Constructor.
     *
     * @param args The proxy method invocation arguments.
     */
    private SystemInvocationArguments(Object[] args) {
      this.args = args;
    }

    /** {@inheritDoc} */
    @Override
    public int count() {
      return args.length;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get() {
      return args.clone();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Object> get(int index) {
      return Optional.ofNullable(args.length > index ? args[index] : null);
    }

    /** {@inheritDoc} */
    @Override
    public Object getOrThrow(int index) {
      if (index >= args.length) {
        throw new IndexOutOfBoundsException("No invocation argument at index " + index + ".");
      }
      return args[index];
    }
  }
}
