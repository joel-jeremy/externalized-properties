package io.github.jeyjeyemem.externalizedproperties.core.testentities;

import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * A stub {@link ProxyMethodInfo} implementation.
 */
public class StubProxyMethodInfo implements ProxyMethodInfo {

    private final Method method;
    private final VariableExpander variableExpander;

    private StubProxyMethodInfo(Method method) {
        this(method, new InternalVariableExpander(new SystemPropertyResolver()));
    }

    private StubProxyMethodInfo(Method method, VariableExpander variableExpander) {
        if (method == null) {
            throw new IllegalArgumentException("method must not be null.");
        }
        if (variableExpander == null) {
            throw new IllegalArgumentException("variableExpander must not be null.");
        }
        this.method = method;
        this.variableExpander = variableExpander;
    }

    public Method method() {
        return method;
    }

    @Override
    public Optional<ExternalizedProperty> externalizedPropertyAnnotation() {
        return Optional.ofNullable(method.getAnnotation(ExternalizedProperty.class));
    }

    @Override
    public Optional<String> externalizedPropertyName() {
        return externalizedPropertyAnnotation().map(
            ep -> variableExpander.expandVariables(ep.value())
        );
    }

    @Override
    public Annotation[] annotations() {
        return method.getAnnotations();
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
    public String name() {
        return method.getName();
    }

    @Override
    public Class<?> returnType() {
        return method.getReturnType();
    }

    @Override
    public Type genericReturnType() {
        return method.getGenericReturnType();
    }

    @Override
    public boolean hasReturnType(Class<?> type) {
        return returnType().equals(type);
    }

    @Override
    public boolean hasReturnType(Type type) {
        return genericReturnType().equals(type);
    }

    @Override
    public Type[] returnTypeGenericTypeParameters() {
        Type returnType = method.getGenericReturnType();
        return TypeUtilities.getTypeParameters(returnType);
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
    public Optional<Type> returnTypeGenericTypeParameter(int typeParameterIndex) {
        Type[] genericTypeParameters = returnTypeGenericTypeParameters();
        if (genericTypeParameters.length == 0 || typeParameterIndex >= genericTypeParameters.length) {
            return Optional.empty();
        }

        return Optional.ofNullable(genericTypeParameters[typeParameterIndex]);
    }

    @Override
    public Class<?>[] parameterTypes() {
        return method.getParameterTypes();
    }

    @Override
    public Type[] genericParameterTypes() {
        return method.getGenericParameterTypes();
    }

    public static StubProxyMethodInfo fromMethod(
            Class<?> proxyInterface,
            String methodName,
            Class<?>... methodParameterTypes
    ) {
        Method method = getMethod(
            proxyInterface, 
            methodName, 
            methodParameterTypes
        );
        return new StubProxyMethodInfo(method);
    }

    public static StubProxyMethodInfo fromMethod(
            Method proxyInterfaceMethod
    ) {
        return new StubProxyMethodInfo(proxyInterfaceMethod);
    }

    public static StubProxyMethodInfo fromMethod(
        Method proxyInterfaceMethod,
        VariableExpander variableExpander
    ) {
        return new StubProxyMethodInfo(proxyInterfaceMethod, variableExpander);
    }
    
    public static Method getMethod(
            Class<?> clazz, 
            String name, 
            Class<?>... parameterTypes
    ) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find property method.", e);
        }
    }
}