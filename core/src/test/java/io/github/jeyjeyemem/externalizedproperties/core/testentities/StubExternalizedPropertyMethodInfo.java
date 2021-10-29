package io.github.jeyjeyemem.externalizedproperties.core.testentities;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StubExternalizedPropertyMethodInfo
        implements ExternalizedPropertyMethodInfo {

    private final Method method;
    private final VariableExpander variableExpander;

    private StubExternalizedPropertyMethodInfo(Method method) {
        this(method, new InternalVariableExpander(new SystemPropertyResolver()));
    }

    private StubExternalizedPropertyMethodInfo(Method method, VariableExpander variableExpander) {
        if (method == null) {
            throw new IllegalArgumentException("method must not be null.");
        }
        if (variableExpander == null) {
            throw new IllegalArgumentException("variableExpander must not be null.");
        }
        this.method = method;
        this.variableExpander = variableExpander;
    }

    @Override
    public Optional<ExternalizedProperty> externalizedPropertyAnnotation() {
        return Optional.ofNullable(method.getAnnotation(ExternalizedProperty.class));
    }

    @Override
    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotation) {
        return Optional.ofNullable(method.getAnnotation(annotation));
    }

    @Override
    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass) {
        return findAnnotation(annotationClass).isPresent();
    }

    @Override
    public Optional<String> propertyName() {
        return externalizedPropertyAnnotation().map(
            ep -> variableExpander.expandVariables(ep.value())
        );
    }

    @Override
    public Class<?> returnType() {
        return method.getReturnType();
    }

    @Override
    public boolean hasReturnType(Class<?> type) {
        return returnType().equals(type);
    }

    @Override
    public List<Type> genericReturnTypeParameters() {
        Type returnType = method.getGenericReturnType();
        if (!(returnType instanceof ParameterizedType)) {
            return Collections.emptyList();
        }

        return getTypeParameters(returnType);
    }

    @Override
    public String methodSignatureString() {
        return method.toGenericString();
    }

    @Override
    public boolean isDefaultInterfaceMethod() {
        return method.isDefault();
    }
    
    @Override
    public Optional<Type> genericReturnTypeParameter(int typeParameterIndex) {
        List<Type> genericTypeParameters = genericReturnTypeParameters();
        if (genericTypeParameters.isEmpty() || typeParameterIndex >= genericTypeParameters.size()) {
            return Optional.empty();
        }

        return Optional.ofNullable(genericTypeParameters.get(typeParameterIndex));
    }

    @Override
    public Type genericReturnTypeParameterOrReturnType(int typeParameterIndex) {
        return genericReturnTypeParameter(typeParameterIndex).orElse(returnType());
    }

    public static StubExternalizedPropertyMethodInfo fromMethod(
            Class<?> proxyInterface,
            String methodName,
            Class<?>... methodParameterTypes
    ) {
        Method method = getProxyInterfaceMethod(
            proxyInterface, 
            methodName, 
            methodParameterTypes
        );
        return new StubExternalizedPropertyMethodInfo(method);
    }

    public static StubExternalizedPropertyMethodInfo fromMethod(
            Method propertyMethod
    ) {
        return new StubExternalizedPropertyMethodInfo(propertyMethod);
    }

    public static StubExternalizedPropertyMethodInfo fromMethod(
        Method propertyMethod,
        VariableExpander variableExpander
    ) {
        return new StubExternalizedPropertyMethodInfo(propertyMethod, variableExpander);
    }
    
    public static Method getProxyInterfaceMethod(
            Class<?> proxyInterface, 
            String name, 
            Class<?>... parameterTypes
    ) {
        try {
            return proxyInterface.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find property method.", e);
        }
    }

    private List<Type> getTypeParameters(Type type) {
        if (type instanceof ParameterizedType) {
            return Arrays.asList(((ParameterizedType)type).getActualTypeArguments());
        }
        return Collections.emptyList();
    }
}