package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Adapts a {@link Method} to a {@link ProxyMethod}. 
 */
public class ProxyMethodAdapter implements ProxyMethod {

    private final Method method;
    private final Object[] args;

    /**
     * Constructor.
     * 
     * @param method The method.
     * @param args The method arguments.
     */
    public ProxyMethodAdapter(Method method, Object... args) {
        this.method = requireNonNull(method, "method");
        this.args = requireNonNull(args, "args");
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> externalizedPropertyName() {
        ExternalizedProperty externalizedProperty = 
            method.getAnnotation(ExternalizedProperty.class);
        if (externalizedProperty == null) {
            return Optional.empty();
        }
        
        String value = externalizedProperty.value();
        if (!"".equals(value)) {
            return Optional.of(value);
        }

        // No need to check before casting. 
        // Should have been validated on proxy creation.
        return Optional.of((String)args[0]);
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
    public Type[] parameterTypes() {
        return method.getGenericParameterTypes();
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
        Type[] genericTypeParameters = typeParametersOfReturnType();
        if (genericTypeParameters.length == 0 || 
                typeParameterIndex >= genericTypeParameters.length) {
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