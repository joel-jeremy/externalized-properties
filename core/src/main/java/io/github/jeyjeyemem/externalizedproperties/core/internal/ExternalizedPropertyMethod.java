package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Contains information about an externalized property method.
 */
public class ExternalizedPropertyMethod implements ExternalizedPropertyMethodInfo {
    private final Object proxy;
    private final Method method;
    private final ExternalizedProperties externalizedProperties;
    private final MethodHandleFactory methodHandleFactory;

    private final String expandedPropertyName;
    
    /**
     * Constructor.
     * 
     * @param proxy The proxy instance.
     * @param method The externalized property method.
     * @param externalizedProperties The externalized properties.
     * @param methodHandleFactory The method handle factory.
     */
    private ExternalizedPropertyMethod(
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
        
        ExternalizedProperty externalizedPropertyAnnotation = 
            method.getAnnotation(ExternalizedProperty.class);
        this.expandedPropertyName = externalizedPropertyAnnotation != null ?
            externalizedProperties.expandVariables(externalizedPropertyAnnotation.value()) : 
            null;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ExternalizedProperty> externalizedPropertyAnnotation() {
        return findAnnotation(ExternalizedProperty.class);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> propertyName() {
        return Optional.ofNullable(expandedPropertyName);
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
        // No property name. Means not annotated with @ExternalizedProperty.
        if (expandedPropertyName == null) {
            return determineDefaultValue(args);
        }

        Optional<?> resolved = externalizedProperties.resolveProperty(
            expandedPropertyName, 
            genericReturnType()
        );

        if (resolved.isPresent()) {
            return resolved.get();
        }

        // Property not resolved.
        return determineDefaultValue(args);
    }

    /**
     * Determine a default value for the externalized property method.
     * This will attempt to do the following:
     * <ol>
     *  <li>
     *      Invoke the externalized property method if it's a default interface method
     *      and return the value.
     *  </li>
     *  <li>
     *      Return {@link Optional#empty()} if the externalized property method 
     *      return type is an {@link Optional}.
     *  </li>
     *  <li>
     *      Throw an exception if the externalized property method return type 
     *      is not an {@link Optional}.
     *  </li>
     * </ol> 
     * 
     * @param args The arguments passed to the externalized property method.
     * @return The default value that shall be returned by the externalized property method.
     */
    public Object determineDefaultValue(Object[] args) {
        if (isDefaultInterfaceMethod()) {
            return invokeDefaultInterfaceMethod(args);
        }

        if (hasReturnType(Optional.class)) {
            return Optional.empty();
        }

        // Non-optional properties will throw an exception if cannot be resolved.
        throw new UnresolvedPropertiesException(
            propertyName().orElse(null),
            String.format(
                "Failed to resolve property (%s) for externalized property method (%s). " + 
                "To prevent exceptions when a property cannot be resolved, " +
                "consider changing method's return type to an Optional.",
                propertyName().orElse(null),
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
                    "Externalized property method: %s.",
                    methodSignatureString()
                ), 
                ex
            );
        }
    }
    
    /**
     * Factory method.
     * 
     * @param proxy The proxy instance.
     * @param method The externalized property method.
     * @param externalizedProperties The externalized properties.
     * @param methodHandleFactory The method handle factory.
     * @return The externalized property method.
     */
    public static ExternalizedPropertyMethod create(
            Object proxy, 
            Method method,
            ExternalizedProperties externalizedProperties,
            MethodHandleFactory methodHandleFactory
    ) {
        return new ExternalizedPropertyMethod(
            proxy, 
            method, 
            externalizedProperties, 
            methodHandleFactory
        );
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
                "Externalized property method: %s.",
                methodSignatureString()
            ));
        };
    }

    // private WeakHashMap<Class<? extends Annotation>, Annotation> buildMethodAnnotationLookup(
    //         Method method
    // ) {
    //     Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
    //     WeakHashMap<Class<? extends Annotation>, Annotation> lookup = 
    //         new WeakHashMap<>(declaredAnnotations.length);
    //     for (Annotation annotation : declaredAnnotations) {
    //         lookup.put(annotation.annotationType(), annotation);
    //     }
    //     return lookup;
    // }

    private static interface DefaultInterfaceMethodHandler {
        Object invoke(Object... args) throws Throwable;
    }
}
