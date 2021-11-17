package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertyException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Optional;
import java.util.WeakHashMap;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Contains information about an externalized property method.
 */
public class ExternalizedPropertyMethod implements ExternalizedPropertyMethodInfo {
    private final ExternalizedProperties externalizedProperties;

    // Cache method info so that we don't hold strong reference to the method object.
    private final ExternalizedProperty externalizedPropertyAnnotation;
    private final String expandedPropertyName;
    private final Class<?> returnType;
    private final Type genericReturnType;
    private final Type[] genericReturnTypeGenericTypeParameters;
    private final Class<?>[] parameterTypes;
    private final Type[] genericParameterTypes;
    private final boolean isDefaultInterfaceMethod;
    private final String methodSignatureString;
    private final DefaultInterfaceMethodHandler defaultInterfaceMethodHandler;
    private final WeakHashMap<Class<? extends Annotation>, Annotation> annotationLookup;
    
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
        
        // Set to fields to avoiding having a strong reference to the method object.
        this.externalizedProperties = externalizedProperties;
        this.externalizedPropertyAnnotation = method.getAnnotation(ExternalizedProperty.class);
        this.expandedPropertyName = externalizedPropertyAnnotation != null ?
            externalizedProperties.expandVariables(externalizedPropertyAnnotation.value()) : null;
        this.returnType = method.getReturnType();
        this.genericReturnType = method.getGenericReturnType();
        this.parameterTypes = method.getParameterTypes();
        this.genericParameterTypes = method.getGenericParameterTypes();
        this.genericReturnTypeGenericTypeParameters = TypeUtilities.getTypeParameters(genericReturnType);
        this.isDefaultInterfaceMethod = method.isDefault();
        this.methodSignatureString = method.toGenericString();
        this.defaultInterfaceMethodHandler = buildDefaultInterfaceMethodHandler(
            proxy, 
            method, 
            methodHandleFactory
        );
        this.annotationLookup = buildMethodAnnotationLookup(method);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ExternalizedProperty> externalizedPropertyAnnotation() {
        return Optional.ofNullable(externalizedPropertyAnnotation);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> propertyName() {
        return Optional.ofNullable(expandedPropertyName);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> returnType() {
        return returnType;
    }

    /** {@inheritDoc} */
    @Override
    public Type genericReturnType() {
        return genericReturnType;
    }

    /** {@inheritDoc} */
    @Override
    public Type[] genericReturnTypeGenericTypeParameters() {
        return genericReturnTypeGenericTypeParameters;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?>[] parameterTypes() {
        return parameterTypes;
    }

    /** {@inheritDoc} */
    @Override
    public Type[] genericParameterTypes() {
        return genericParameterTypes;
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
    public Optional<Type> genericReturnTypeGenericTypeParameter(int typeParameterIndex) {
        Type[] genericTypeParameters = genericReturnTypeGenericTypeParameters();
        if (genericTypeParameters.length == 0 || typeParameterIndex >= genericTypeParameters.length) {
            return Optional.empty();
        }

        return Optional.ofNullable(genericTypeParameters[typeParameterIndex]);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDefaultInterfaceMethod() {
        return isDefaultInterfaceMethod;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
        return Optional.ofNullable(
            annotationClass.cast(annotationLookup.get(annotationClass))
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
        return methodSignatureString;
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
        throw new UnresolvedPropertyException(
            Collections.singletonList(propertyName().orElse(null)),
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
            return defaultInterfaceMethodHandler.invoke(args);
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
        if (isDefaultInterfaceMethod) {
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

    private WeakHashMap<Class<? extends Annotation>, Annotation> buildMethodAnnotationLookup(
            Method method
    ) {
        Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
        WeakHashMap<Class<? extends Annotation>, Annotation> lookup = 
            new WeakHashMap<>(declaredAnnotations.length);
        for (Annotation annotation : declaredAnnotations) {
            lookup.put(annotation.annotationType(), annotation);
        }
        return lookup;
    }

    private static interface DefaultInterfaceMethodHandler {
        Object invoke(Object... args) throws Throwable;
    }
}
