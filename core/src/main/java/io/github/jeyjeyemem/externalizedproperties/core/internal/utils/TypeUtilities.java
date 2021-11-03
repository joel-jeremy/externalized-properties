package io.github.jeyjeyemem.externalizedproperties.core.internal.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Type-related utility methods.
 */
public class TypeUtilities {
    private TypeUtilities() {}

    /**
     * Extract raw class of the given type.
     * 
     * @param type The type to get the raw class from.
     * @return The raw class of the given type.
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>)type;
        } 
        else if (type instanceof ParameterizedType) {
            // For example, if type is List<String>, List shall be returned.
            ParameterizedType pt = (ParameterizedType)type;
            return (Class<?>)pt.getRawType();
        } 
        else if (type instanceof GenericArrayType) {
            // For example, if type is Optional<String>[], Optional[] shall be returned.
            GenericArrayType gat = (GenericArrayType)type;
            Type genericArrayComponentType = gat.getGenericComponentType();
            if (genericArrayComponentType instanceof ParameterizedType) {
                Class<?> rawType = ((Class<?>)((ParameterizedType)genericArrayComponentType).getRawType());
                // Get array class.
                return Array.newInstance(rawType, 0).getClass();
            }
        }

        // Wildcard types/Type variables go here.
        return null;
    }

    /**
     * Extract the list of generic type parameters if the given type has any.
     * 
     * @param type The type to extract type parameters from.
     * @return The list of generic type parameters if the given type has any.
     */
    public static List<Type> getTypeParameters(Type type) {
        if (type instanceof ParameterizedType) {
            // Return generic type parameters.
            // For example, if type is List<String>, String shall be returned.
            return Arrays.asList(((ParameterizedType)type).getActualTypeArguments());
        } 
        else if (type instanceof GenericArrayType) {
            // Return generic component type of arrays.
            // For example, if type is Optional<String>[], String shall be returned.
            Type arrayGenericComponentType = ((GenericArrayType)type).getGenericComponentType();

            if (arrayGenericComponentType instanceof Class<?>) {
                // Component type is not parameterized.
                return Collections.emptyList();
            }

            // Need to get the actual type parameter of the generic component type.
            return getTypeParameters(arrayGenericComponentType);
        }
        
        // Wildcard types/Type variables go here.
        return Collections.emptyList();
    }
}