package io.github.jeyjeyemem.externalizedproperties.core.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

public class ProxyMethodAdapter implements ProxyMethod {

    private final Method method;

    /**
     * Constructor.
     * 
     * @param method The method to adapt.
     */
    public ProxyMethodAdapter(Method method) {
        this.method = requireNonNull(method, "method");
    }

    /**
     * The adapated method.
     * @return The adapted method.
     */
    public Method method() {
        return method;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ExternalizedProperty> externalizedPropertyAnnotation() {
        return findAnnotation(ExternalizedProperty.class);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> externalizedPropertyName() {
        return externalizedPropertyAnnotation().map(ExternalizedProperty::value);
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
    public Type[] returnTypeGenericTypeParameters() {
        return TypeUtilities.getTypeParameters(genericReturnType());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Type> returnTypeGenericTypeParameter(int typeParameterIndex) {
        Type[] genericTypeParameters = returnTypeGenericTypeParameters();
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
