package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Context object for {@link Converter}s.
 * This contains information such as the value to be converted and the target type.
 */
public class ConversionContext {
    private final Converter<?> converter;
    private final ProxyMethod proxyMethod;
    private final String value;
    private final Type targetType;

    /**
     * Constructor.
     * 
     * @param converter The converter. This is here to allow for sub-conversions 
     * in {@link Converter} implementations.
     * @param proxyMethod The proxy method info.
     * @param value The value to convert.
     * @param targetType The target type to convert to. For generic types, the
     * {@link TypeReference} class can be used to build the type and pass it here
     * e.g. {@code Type targetType = 
     * new TypeReference<List<Integer>>()&#123;&#125;.type()}.
     * 
     * @see TypeReference
     */
    public ConversionContext(
            Converter<?> converter,
            ProxyMethod proxyMethod,
            String value,
            Type targetType
    ) {
        this.proxyMethod = requireNonNull(proxyMethod, "proxyMethod");
        this.converter = requireNonNull(converter, "converter");
        this.value = requireNonNull(value, "value");
        this.targetType = requireNonNull(targetType, "targetType");
    }

    /**
     * Constructor which constructs a context object to convert to whatever the 
     * return type of the proxy method is.
     * 
     * @param converter The converter. This is here to allow for sub-conversions 
     * in {@link Converter} implementations.
     * @param proxyMethod The proxy method info.
     * @param value The value to convert.
     */
    public ConversionContext(
            Converter<?> converter,
            ProxyMethod proxyMethod,
            String value
    ) {
        this.converter = requireNonNull(converter, "converter");
        this.proxyMethod = requireNonNull(proxyMethod, "proxyMethod");
        this.targetType = proxyMethod.genericReturnType();
        this.value = requireNonNull(value, "value");
    }

    /**
     * Constructor to create a new {@link ConversionContext} based on a previous/existing
     * conversion context.
     * 
     * @param context The conversion context.
     * @param proxyMethod The proxy method info.
     * @param value The value to convert.
     * @param targetType The target type to convert to. For generic types, the
     * {@link TypeReference} class can be used to build the type and pass it here
     * e.g. {@code Type targetType = 
     * new TypeReference<List<Integer>>()&#123;&#125;.type()}.
     * 
     * @see TypeReference
     */
    private ConversionContext(
            ConversionContext context,
            ProxyMethod proxyMethod,
            String value,
            Type targetType
    ) {
        requireNonNull(context, "context");
        this.converter = context.converter;
        this.proxyMethod = context.proxyMethod;
        this.value = requireNonNull(value, "value");
        this.targetType = requireNonNull(targetType, "targetType");
    }

    /**
     * The proxy method info.
     * 
     * @return The proxy method info.
     */
    public ProxyMethod proxyMethod() {
        return proxyMethod;
    }

    /**
     * The converter.
     * 
     * @return The converter.
     */
    public Converter<?> converter() {
        return converter;
    }

    /**
     * The value to convert.
     * 
     * @return The value to convert.
     */
    public String value() {
        return value;
    }

    /**
     * The raw target type to convert the value to.
     * 
     * @apiNote The target type may differ than the proxy method's 
     * return type since converters can pass in a different target type 
     * when doing conversions to honor generic type parameters.
     * 
     * @implNote This calculates raw type every time the method is called.
     * If the raw type needs to used several times, it's best to cache the
     * result of this method in a variable and use that instead.
     * 
     * @return The raw type to convert resolved property to.
     */
    public Class<?> rawTargetType() {
        return TypeUtilities.getRawType(targetType);
    }

    /**
     * The target type to convert the value to.
     * 
     * @apiNote The target type may differ than the proxy method's 
     * return type since converters can pass in a different target type 
     * when doing conversions to honor generic type parameters.
     * 
     * @return The target type to convert the value to.
     */
    public Type targetType() {
        return targetType;
    }

    /**
     * The generic type parameters of the class returned by {@link #targetType()}, if there are any.
     * 
     * @implNote This calculates the target type's generic type parameter every time the method 
     * is called. If the generic type parameter needs to used several times, it's best to cache the
     * result of this method in a variable and use that instead.
     * 
     * @return The generic type parameters of the class returned by {@link #targetType()}, 
     * if there are any. Otherwise, this shall return an empty array.
     */
    public Type[] targetTypeGenericTypeParameters() {
        return TypeUtilities.getTypeParameters(targetType);
    }

    /**
     * Create a new {@link ConversionContext} based on this instance but
     * with updated value and target type.
     * 
     * @param value The value to convert.
     * @param targetType The target type to convert to. For generic types, the
     * {@link TypeReference} class can be used to build the type and pass it here
     * e.g. {@code Type targetType = 
     * new TypeReference<List<Integer>>()&#123;&#125;.type()}.
     * @return The new {@link ConversionContext} based on this instance.
     * 
     * @see TypeReference
     */
    public ConversionContext with(String value, Type targetType) {
        return new ConversionContext(this, proxyMethod, value, targetType);
    }
}
