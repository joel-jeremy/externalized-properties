package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverterContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedExternalizedPropertyException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.MethodHandleUtilities;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Contains information about an externalized property method.
 */
public class ExternalizedPropertyMethod implements ExternalizedPropertyMethodInfo {
    private final Object proxy;
    private final Method method;
    private final ExternalizedPropertyResolver externalizedPropertyResolver;
    private final VariableExpander variableExpander;
    private final ResolvedPropertyConverter resolvedPropertyConverter;

    /**
     * Constructor.
     * 
     * @param proxy The proxy instance.
     * @param method The externalized property method.
     * @param externalizedPropertyResolver The externalized property resolver.
     * @param variableExpander The externalized property name variable expander.
     * @param resolvedPropertyConverter The resolved property converter.
     */
    public ExternalizedPropertyMethod(
            Object proxy, 
            Method method,
            ExternalizedPropertyResolver externalizedPropertyResolver,
            VariableExpander variableExpander,
            ResolvedPropertyConverter resolvedPropertyConverter
    ) {
        this.proxy = requireNonNull(proxy, "proxy");
        this.method = requireNonNull(method, "method");
        this.externalizedPropertyResolver = requireNonNull(
            externalizedPropertyResolver, 
            "externalizedPropertyResolver"
        );
        this.variableExpander = requireNonNull(variableExpander, "variableExpander");
        this.resolvedPropertyConverter = requireNonNull(
            resolvedPropertyConverter, 
            "resolvedPropertyConverter"
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ExternalizedProperty> externalizedPropertyAnnotation() {
        return Optional.ofNullable(method.getAnnotation(ExternalizedProperty.class));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> propertyName() {
        return externalizedPropertyAnnotation().map(annotation -> 
            variableExpander.expandVariables(annotation.value())
        );
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> returnType() {
        return method.getReturnType();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasReturnType(Class<?> type) {
        return returnType().equals(type);
    }

    /** {@inheritDoc} */
    @Override
    public List<Type> genericReturnTypeParameters() {
        Type returnType = method.getGenericReturnType();
        return getTypeParameters(returnType);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Type> genericReturnTypeParameter(int typeParameterIndex) {
        List<Type> genericTypeParameters = genericReturnTypeParameters();
        if (genericTypeParameters.isEmpty() || typeParameterIndex >= genericTypeParameters.size()) {
            return Optional.empty();
        }

        return Optional.ofNullable(genericTypeParameters.get(typeParameterIndex));
    }

    /** {@inheritDoc} */
    @Override
    public Type genericReturnTypeParameterOrReturnType(int typeParameterIndex) {
        return genericReturnTypeParameter(typeParameterIndex).orElse(returnType());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDefaultInterfaceMethod() {
        return method.isDefault();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
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
        // Get property name from @ExternalizedProperty if method is annotated.
        Optional<String> propertyAnnotationName = propertyName();
        if (!propertyAnnotationName.isPresent()) {
            return determineDefaultValue(args);
        }

        String propertyName = propertyAnnotationName.get();

        ExternalizedPropertyResolverResult result = 
            externalizedPropertyResolver.resolve(propertyName);
        
        Optional<ResolvedProperty> resolvedProperty = result.findResolvedProperty(propertyName);
        if (resolvedProperty.isPresent()) {
            // Non-string return type handling.
            if (!hasReturnType(String.class)) {
                return resolvedPropertyConverter.convert(
                    new ResolvedPropertyConverterContext(
                        this,
                        resolvedProperty.get(), 
                        returnType(),
                        genericReturnTypeParameters()
                    )
                );
            }

            // String return type. Return value as is.
            return resolvedProperty.map(ResolvedProperty::value).get();
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
        throw new UnresolvedExternalizedPropertyException(
            Collections.singletonList(propertyName().orElse(null)),
            String.format(
                "Failed to resolve property (%s) for externalized property method (%s). " + 
                "To prevent exception when a property cannot be resolved, " +
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
        if (!isDefaultInterfaceMethod()) {
            throw new IllegalStateException(String.format(
                "Tried to invoke a non-default interface method. " +
                "Externalized property method: %s.",
                methodSignatureString()
            ));
        }

        try {
            MethodHandle defaultInterfaceMethodHandle = MethodHandleUtilities.buildMethodHandle(proxy, method);
            return defaultInterfaceMethodHandle.invokeWithArguments(args);
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

    private List<Type> getTypeParameters(Type type) {
        if (type instanceof ParameterizedType) {
            return Arrays.asList(((ParameterizedType)type).getActualTypeArguments());
        }
        return Collections.emptyList();
    }
    
}
