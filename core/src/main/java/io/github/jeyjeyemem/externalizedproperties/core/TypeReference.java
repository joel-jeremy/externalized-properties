package io.github.jeyjeyemem.externalizedproperties.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class is used to reference a type (possibly a generic type).
 * 
 * @param <T> The referenced type.
 * @apiNote This needs to be instantiated as an anonymous class in order for the 
 * type parameter to be detected e.g. 
 * <code>new TypeReference{@literal<}List{@literal<}Integer{@literal>}{@literal>}(){}</code>.
 */
public abstract class TypeReference<T> {
    private final Type type;

    /**
     * Constructor.
     */
    protected TypeReference() {
        Type selfType = getClass().getGenericSuperclass();
        // Should not fail because TypeReference is a parameterized type.
        ParameterizedType parameterizedType = (ParameterizedType)selfType;
        type = parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * The referenced type.
     * 
     * @return The referenced type.
     */
    public Type type() { 
        return type; 
    }

    /**
     * The raw referenced type.
     * 
     * @return The raw referenced type.
     */
    public Class<?> rawType() {
        return TypeUtilities.getRawType(type);
    }

    /**
     * The generic type parameters of the referenced type.
     * 
     * @return The generic type parameters of the referenced type.
     */
    public Type[] genericTypeParameters() {
        return TypeUtilities.getTypeParameters(type);
    }
}
