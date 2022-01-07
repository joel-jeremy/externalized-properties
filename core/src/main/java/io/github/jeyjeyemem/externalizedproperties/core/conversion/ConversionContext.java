package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;

import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Context object for {@link ConversionHandler}s.
 * This contains information such as the value to be converted and the target type.
 */
public class ConversionContext {
    /** This is nullable. */
    private final ProxyMethodInfo proxyMethodInfo;

    private final Converter converter;
    private final String value;
    private final Type targetType;

    /**
     * Constructor.
     * 
     * @param converter The converter. 
     * This is here to allow for recursive conversion in {@link ConversionHandler}
     * implementations.
     * @param targetType The target type to convert to. For generic types, the
     * {@link TypeReference} class can be used to build the type and pass it here
     * e.g. {@code Type targetType = 
     * new TypeReference<List<Integer>>()&#123;&#125;.type()}.
     * @param value The value to convert.
     * @see TypeReference
     */
    public ConversionContext(
            Converter converter,
            Type targetType,
            String value
    ) {
        this.proxyMethodInfo = null;
        this.converter = requireNonNull(converter, "converter");
        this.value = requireNonNull(value, "value");
        this.targetType = requireNonNull(targetType, "targetType");
    }

    /**
     * Constructor which constructs a context object to convert to whatever the 
     * return type of the proxy method is.
     * 
     * @param converter The converter. 
     * This is here to allow for recursive conversion in {@link ConversionHandler}
     * implementations.
     * @param proxyMethodInfo The proxy method info.
     * @param value The value to convert.
     */
    public ConversionContext(
            Converter converter,
            ProxyMethodInfo proxyMethodInfo,
            String value
    ) {
        this.converter = requireNonNull(converter, "converter");
        this.proxyMethodInfo = requireNonNull(proxyMethodInfo, "proxyMethodInfo");
        this.value = requireNonNull(value, "value");
        this.targetType = proxyMethodInfo.genericReturnType();
    }

    /**
     * Constructor to create a new {@link ConversionContext} based on a previous/existing
     * conversion context.
     * 
     * @param context The conversion context.
     * @param value The value to convert.
     * @param targetType The target type to convert to. For generic types, the
     * {@link TypeReference} class can be used to build the type and pass it here
     * e.g. {@code Type targetType = 
     * new TypeReference<List<Integer>>()&#123;&#125;.type()}.
     * @see TypeReference
     */
    private ConversionContext(
            ConversionContext context,
            Type targetType,
            String value
    ) {
        requireNonNull(context, "context");
        this.converter = context.converter;
        this.proxyMethodInfo = context.proxyMethodInfo;
        this.value = requireNonNull(value, "value");
        this.targetType = requireNonNull(targetType, "targetType");
    }

    /**
     * The proxy method info. This may be empty {@code Optional} 
     * if the conversion did not originate from a proxy method invocation.
     * 
     * @return The proxy method info. 
     * Otherwise, an empty {@link Optional}.
     */
    public Optional<ProxyMethodInfo> proxyMethodInfo() {
        return Optional.ofNullable(proxyMethodInfo);
    }

    /**
     * The converter.
     * 
     * @return The converter.
     */
    public Converter converter() {
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
     * @param targetType The target type to convert to. For generic types, the
     * {@link TypeReference} class can be used to build the type and pass it here
     * e.g. {@code Type targetType = 
     * new TypeReference<List<Integer>>()&#123;&#125;.type()}.
     * @param value The value to convert.
     * @return The new {@link ConversionContext} based on this instance.
     * @see TypeReference
     */
    public ConversionContext with(Type targetType, String value) {
        return new ConversionContext(this, targetType, value);
    }
}
