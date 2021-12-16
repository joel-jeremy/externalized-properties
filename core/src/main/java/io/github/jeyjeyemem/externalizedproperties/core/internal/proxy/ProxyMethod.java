package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Contains information about a proxy method and handles the resolution of properties 
 * for the proxy method invocation.
 */
public class ProxyMethod implements ProxyMethodInfo {
    private final Object proxy;
    private final Method method;
    private final ExternalizedProperties externalizedProperties;
    private final MethodHandleFactory methodHandleFactory;

    private final ExternalizedProperty externalizedPropertyAnnotation;
    private final String expandedPropertyName;
    
    /**
     * Constructor.
     * 
     * @param proxy The proxy.
     * @param method The method.
     * @param externalizedProperties The externalized properties.
     * @param methodHandleFactory The method handle factory.
     */
    public ProxyMethod(
            Object proxy, 
            Method method,
            ExternalizedProperties externalizedProperties,
            MethodHandleFactory methodHandleFactory
    ) {
        requireNonNull(proxy, "proxy");
        requireNonNull(method, "method");
        requireNonNull(externalizedProperties, "externalizedProperties");
        requireNonNull(methodHandleFactory, "methodHandleFactory");

        this.proxy = proxy;
        this.method = method;
        this.externalizedProperties = externalizedProperties;
        this.methodHandleFactory = methodHandleFactory;
        
        this.externalizedPropertyAnnotation = method.getAnnotation(ExternalizedProperty.class);
        this.expandedPropertyName = externalizedPropertyAnnotation != null ?
            externalizedProperties.expandVariables(externalizedPropertyAnnotation.value()) : 
            null;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ExternalizedProperty> externalizedPropertyAnnotation() {
        return Optional.ofNullable(externalizedPropertyAnnotation);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> externalizedPropertyName() {
        return Optional.ofNullable(expandedPropertyName);
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
    public Class<?> returnType() {
        return method.getReturnType();
    }

    /** {@inheritDoc} */
    @Override
    public Type genericReturnType() {
        return method.getGenericReturnType();
    }

    /** {@inheritDoc} */
    @Override
    public Type[] returnTypeGenericTypeParameters() {
        return TypeUtilities.getTypeParameters(genericReturnType());
    }

    /** {@inheritDoc} */
    @Override
    public Class<?>[] parameterTypes() {
        return method.getParameterTypes();
    }

    /** {@inheritDoc} */
    @Override
    public Type[] genericParameterTypes() {
        return method.getGenericParameterTypes();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasReturnType(Class<?> type) {
        return returnType().equals(type);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasReturnType(Type type) {
        return genericReturnType().equals(type);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Type> returnTypeGenericTypeParameter(int typeParameterIndex) {
        Type[] genericTypeParameters = returnTypeGenericTypeParameters();
        if (genericTypeParameters.length == 0 || typeParameterIndex >= genericTypeParameters.length) {
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
    public Annotation[] annotations() {
        return method.getAnnotations();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
        return Optional.ofNullable(
            method.getAnnotation(annotationClass)
        );
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass) {
        return findAnnotation(annotationClass).isPresent();
    }

    /** {@inheritDoc} */
    @Override
    public String methodSignatureString() {
        return method.toGenericString();
    }

    /**
     * Returns the method's signature string.
     * 
     * @return The method's signature string.
     */
    @Override
    public String toString() {
        return methodSignatureString();
    }

    /**
     * Resolve externalized property.
     * 
     * @param args Proxy invocation method arguments.
     * @return The resolved property.
     */
    public Object resolveProperty(Object[] args) {
        if (expandedPropertyName != null) {
            Optional<?> resolved = externalizedProperties.resolveProperty(this);
            if (resolved.isPresent()) {
                return resolved.get();
            }
        }

        // Either there was no property name 
        // (means not annotated with @ExternalizedProperty)
        // or property cannot be resolved.
        return determineDefaultValueOrThrow(args);
    }

    /**
     * Determine a default value for the externalized property method or throw.
     * This will attempt to do the following:
     * <ol>
     *  <li>
     *      Invoke the method if it's a default interface method and return the value.
     *  </li>
     *  <li>
     *      Return {@link Optional#empty()} if the method return type is an {@link Optional}.
     *  </li>
     *  <li>
     *      Throw an exception if the method return type is not an {@link Optional}.
     *  </li>
     * </ol> 
     * 
     * @param args The arguments passed to the method.
     * @return The default value that shall be returned by the method.
     * @throws UnresolvedPropertiesException if a default value cannot be determined.
     */
    public Object determineDefaultValueOrThrow(Object[] args) {
        if (isDefaultInterfaceMethod()) {
            return invokeDefaultInterfaceMethod(args);
        }

        if (hasReturnType(Optional.class)) {
            return Optional.empty();
        }

        String propertyName = externalizedPropertyName().orElse(null);
        // Non-optional properties will throw an exception if cannot be resolved.
        throw new UnresolvedPropertiesException(
            propertyName,
            String.format(
                "Failed to resolve property '(%s)' for proxy method (%s). " + 
                "To prevent exceptions when a property cannot be resolved, " +
                "consider changing method's return type to an Optional.",
                propertyName,
                methodSignatureString()
            )
        );
    }

    /**
     * Invoke the default interface method.
     * 
     * @param args The arguments to pass to the default interface method.
     * @return The result of the default interface method.
     */
    public Object invokeDefaultInterfaceMethod(Object[] args) {
        try {
            DefaultInterfaceMethodHandler handler = buildDefaultInterfaceMethodHandler(
                proxy, 
                method, 
                methodHandleFactory
            );
            return handler.invoke(args);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new ExternalizedPropertiesException(
                String.format(
                    "Error occurred while invoking default interface method. " +
                    "Proxy method: %s.",
                    methodSignatureString()
                ), 
                ex
            );
        }
    }

    private DefaultInterfaceMethodHandler buildDefaultInterfaceMethodHandler(
            Object proxy, 
            Method method, 
            MethodHandleFactory methodHandleFactory
    ) {
        if (isDefaultInterfaceMethod()) {
            MethodHandle methodHandle = methodHandleFactory.createMethodHandle(method);
            return args -> {
                return methodHandle.bindTo(proxy).invokeWithArguments(args);
            };
        }

        // Just make it throw if method is not a default interface method.
        return args -> {
            throw new IllegalStateException(String.format(
                "Tried to invoke a non-default interface method. " +
                "Proxy method: %s.",
                methodSignatureString()
            ));
        };
    }

    private static interface DefaultInterfaceMethodHandler {
        Object invoke(Object... args) throws Throwable;
    }
}
