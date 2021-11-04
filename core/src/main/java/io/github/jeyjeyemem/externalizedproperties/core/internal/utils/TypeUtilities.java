package io.github.jeyjeyemem.externalizedproperties.core.internal.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Type-related utility methods.
 */
public class TypeUtilities {

    // To prevent repeated creation of arrays just to get the array class.
    private static final ClassValue<Class<?>> ARRAY_TYPE_CACHE = 
        new ClassValue<Class<?>>() {
            @Override
            protected Class<?> computeValue(Class<?> type) {
                return Array.newInstance(type, 0).getClass();
            }
        };

    private TypeUtilities() {}

    /**
     * Extract raw class of the given type.
     * 
     * @param type The type to get the raw class from.
     * @return The raw class of the given type.
     */
    public static Class<?> getRawType(Type type) {
        return getRawType(type, false);
    }

    /**
     * Extract raw class of the given type.
     * 
     * @param type The type to get the raw class from.
     * @param isArray Indicates whether raw type should ba an array type or not.
     * @return The raw class of the given type.
     */
    private static Class<?> getRawType(Type type, boolean isArray) {
        Class<?> rawType = null;

        if (isClass(type)) {
            rawType = asClass(type);
        } 
        else if (isParameterizedType(type)) {
            // Return raw type.
            // For example, if type is List<String>, raw List shall be returned.
            ParameterizedType pt = asParameterizedType(type);

            rawType = getRawType(pt.getRawType());
        } 
        else if (isGenericArrayType(type)) {
            // Return generic component type of array.
            GenericArrayType gat = asGenericArrayType(type);
            
            Type genericArrayComponentType = gat.getGenericComponentType();

            rawType = getRawType(genericArrayComponentType, true);
        }
        else if (isTypeVariable(type)) {
            // Return type variable upper bound.
            // For example, if type is <T>, Object shall be returned.
            // If type is <T extends Number>, Number is returned.
            // If type upper bound is a generic type <T extends List<String>>, return raw List. 
            TypeVariable<?> tv = asTypeVariable(type);

            // Only get first because, Java doesn't allow multiple extends as of writing.
            rawType = getRawType(tv.getBounds()[0]);
        }
        else if (isWildcardType(type)) {
            // Return type variable lower bound or upper bound.
            // If type is <? extends String>, return upper bound which is String.
            // If type if <? super String>, return lower bound which is String.
            WildcardType wt = asWildcardType(type);

            // Only get first of the bounds because Java doesn't allow multiple extends/super
            // as of writing.

            // Return lower bounds if <T super String>, return String.
            if (wt.getLowerBounds().length > 0) {
                return getRawType(wt.getLowerBounds()[0]);
            }

            // Return upper bounds if <T extends String>, return String.
            rawType = getRawType(wt.getUpperBounds()[0]);
        }

        // Return an array class.
        if (rawType != null && isArray) {
            return ARRAY_TYPE_CACHE.get(rawType);
        }

        return rawType;
    }

    /**
     * Extract the list of generic type parameters if the given type has any.
     * 
     * @param type The type to extract type parameters from.
     * @return The list of generic type parameters if the given type has any.
     */
    public static List<Type> getTypeParameters(Type type) {
        ParameterizedType pt = asParameterizedType(type);
        if (pt != null) {
            // Return generic type parameters.
            // For example, if type is List<String>, String shall be returned.
            return Arrays.asList(
                pt.getActualTypeArguments()
            );
        }
        
        // Class has no generic type parameters.
        return Collections.emptyList();
    }

    /**
     * Check is type is a {@link Class} instance.
     * 
     * @param type The type to check.
     * @return {@code true}, if type is a {@link Class}. Otherwise, {@code false}. 
     */
    public static boolean isClass(Type type) {
        return type instanceof Class<?>;
    }

    /**
     * Attempt to cast the type to a {@link Class} if it's a {@link Class} instance.
     * Otherwise, {@code null} is returned.
     * 
     * @param type The type to cast.
     * @return The {@link Class} instance if the given type is a {@link Class} instance.
     * Otherwise, {@code null}.
     */
    public static Class<?> asClass(Type type) {
        if (isClass(type)) {
            return (Class<?>)type;
        }
        return null;
    }

    /**
     * Check is type is a {@link ParameterizedType} instance.
     * 
     * @param type The type to check.
     * @return {@code true}, if type is a {@link ParameterizedType}. Otherwise, {@code false}. 
     */
    public static boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }

    /**
     * Attempt to cast the type to a {@link ParameterizedType} if it's a {@link ParameterizedType} 
     * instance. Otherwise, {@code null} is returned.
     * 
     * @param type The type to cast.
     * @return The {@link ParameterizedType} instance if the given type is a {@link ParameterizedType} 
     * instance. Otherwise, {@code null}.
     */
    public static ParameterizedType asParameterizedType(Type type) {
        if (isParameterizedType(type)) {
            return (ParameterizedType)type;
        }
        return null;
    }

    /**
     * Check is type is a {@link GenericArrayType} instance.
     * 
     * @param type The type to check.
     * @return {@code true}, if type is a {@link GenericArrayType}. Otherwise, {@code false}. 
     */
    public static boolean isGenericArrayType(Type type) {
        return type instanceof GenericArrayType;
    }

    /**
     * Attempt to cast the type to a {@link GenericArrayType} if it's a {@link GenericArrayType} 
     * instance. Otherwise, {@code null} is returned.
     * 
     * @param type The type to cast.
     * @return The {@link GenericArrayType} instance if the given type is a {@link GenericArrayType} 
     * instance. Otherwise, {@code null}.
     */
    public static GenericArrayType asGenericArrayType(Type type) {
        if (isGenericArrayType(type)) {
            return (GenericArrayType)type;
        }
        return null;
    }

    /**
     * Check is type is a {@link TypeVariable} instance.
     * 
     * @param type The type to check.
     * @return {@code true}, if type is a {@link TypeVariable}. Otherwise, {@code false}. 
     */
    public static boolean isTypeVariable(Type type) {
        return type instanceof TypeVariable<?>;
    }

    /**
     * Attempt to cast the type to a {@link TypeVariable} if it's a {@link TypeVariable} 
     * instance. Otherwise, {@code null} is returned.
     * 
     * @param type The type to cast.
     * @return The {@link TypeVariable} instance if the given type is a {@link TypeVariable} 
     * instance. Otherwise, {@code null}.
     */
    public static TypeVariable<?> asTypeVariable(Type type) {
        if (isTypeVariable(type)) {
            return (TypeVariable<?>)type;
        }
        return null;
    }

    /**
     * Check is type is a {@link WildcardType} instance.
     * 
     * @param type The type to check.
     * @return {@code true}, if type is a {@link WildcardType}. Otherwise, {@code false}. 
     */
    public static boolean isWildcardType(Type type) {
        return type instanceof WildcardType;
    }

    /**
     * Attempt to cast the type to a {@link WildcardType} if it's a {@link WildcardType} 
     * instance. Otherwise, {@code null} is returned.
     * 
     * @param type The type to cast.
     * @return The {@link WildcardType} instance if the given type is a {@link WildcardType} 
     * instance. Otherwise, {@code null}.
     */
    public static WildcardType asWildcardType(Type type) {
        if (isWildcardType(type)) {
            return (WildcardType)type;
        }
        return null;
    }
}