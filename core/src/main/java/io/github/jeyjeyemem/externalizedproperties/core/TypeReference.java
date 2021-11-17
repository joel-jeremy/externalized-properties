package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class can be used to specify a type (possibly be generic type) when resolving
 * properties via the {@link InternalExternalizedProperties#resolveProperty(String, TypeReference)}
 * method.
 * 
 * @apiNote This needs to be instantiated as an anonymous class in order for the 
 * type parameter to be detected e.g. {@code new TypeReference<List<Integer>>()&#123;&#125;}.
 * 
 */
public abstract class TypeReference<T> {
    private final Type type;
    private final Class<?> rawType;
    private final Type[] genericTypeParameters;

    /**
     * Constructor.
     */
    protected TypeReference()
    {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
        } else {
            type = null;
        }

        rawType = TypeUtilities.getRawType(type);
        genericTypeParameters = TypeUtilities.getTypeParameters(type);
    }

    /**
     * The referenced type.
     * @return The referenced type.
     */
    public Type type() { 
        return type; 
    }

    /**
     * The raw referenced type.
     * @return The raw referenced type.
     */
    public Class<?> rawType() {
        return rawType;
    }

    /**
     * The generic type parameters of the referenced type.
     * 
     * @return The generic type parameters of the referenced type.
     */
    public Type[] genericTypeParameters() {
        return genericTypeParameters;
    }
}
