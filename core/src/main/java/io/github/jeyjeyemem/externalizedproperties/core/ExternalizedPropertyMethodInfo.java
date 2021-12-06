package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Optional;

/**
 * Information about methods that go through Externalized Properties proxying.
 */
public interface ExternalizedPropertyMethodInfo {
    /**
     * The {@link ExternalizedProperty} annotation, if method is annotated.
     * 
     * @return The externalized property annotation.
     * Otherwise, an empty {@link Optional}.
     */
    Optional<ExternalizedProperty> externalizedPropertyAnnotation();

    /**
     * The externalized property name, if method is annotated with 
     * {@link ExternalizedProperty}. The property name is derived from
     * {@link ExternalizedProperty#value()}.
     * 
     * @return The externalized property name as specified in 
     * {@link ExternalizedProperty#value()}. Otherwise, an empty {@link Optional}.
     */
    Optional<String> externalizedPropertyName();

    /**
     * Find externalized property method annotation.
     * 
     * @param <T> The type of the annotation.
     * @param annotationClass Annotation class to find.
     * @return The annotation matching the specified annotation class.
     * Otherwise, an empty {@link Optional}.
     */
    <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass);

    /**
     * Check whether the externalized property method is annotated with 
     * the specified annotation.
     * 
     * @param <T> The type of the annonation.
     * @param annotationClass The annotation class to check.
     * @return {@code true}, if the externalized property method is annotated 
     * with the specified annotation. Otherwise, {@code false}.
     */
    <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass);

    /**
     * The externalized property method name.
     * 
     * @return The externalized property method name.
     */
    String name();

    /**
     * The externalized property method return type.
     * 
     * @return The externalized property method return type.
     */
    Class<?> returnType();

    /**
     * The externalized property method generic return type.
     * 
     * @return The externalized property method generic return type.
     */
    Type genericReturnType();

    /**
     * The externalized property method generic parameter types.
     * 
     * @return The externalized property method generic parameter types.
     */
    Class<?>[] parameterTypes();

    /**
     * The externalized property method generic parameter types.
     * 
     * @return The externalized property method generic parameter types.
     */
    Type[] genericParameterTypes();

    /**
     * Check if the externalized property method return type matches the given type. 
     * 
     * @param type The class to match against the externalized property method's return type.
     * @return {@code true}, if the externalized property method return type matches 
     * the given type. Otherwise, {@code false}.
     */
    boolean hasReturnType(Class<?> type);

    /**
     * Check if the externalized property method return type matches the given type. 
     * 
     * @param type The type to match against the externalized property method's return type.
     * @return {@code true}, if the externalized property method return type matches 
     * the given type. Otherwise, {@code false}.
     */
    boolean hasReturnType(Type type);

    /**
     * <p>The externalized property method return type's generic type parameters, 
     * if the return type is a generic parameterized type e.g. {@code List<String>}. 
     * 
     * <p>For example, if {@link #genericReturnType()} returns a parameterized type 
     * e.g. {@code List<String>}, this method shall return an array containing a {@code String} type/class.
     * 
     * <p>Another example is if {@link #genericReturnType()} returns an array type with a 
     * generic component type e.g. {@code Optional<Integer>[]}, this method shall return an 
     * array containing an {@code Integer} type/class.
     * 
     * <p>It is also possible to have {@link #genericReturnType()} return a parameterized type 
     * which contains another parameterized type parameter e.g. {@code Optional<List<String>>}, 
     * in this case, this method shall return an array containing a {@code List<String>} parameterized type.
     * 
     * @return The array of generic return type parameters, if the return type is a generic parameterized type 
     * e.g. {@code Optional<String>}.
     * @see ParameterizedType
     * @see GenericArrayType
     * @see WildcardType
     * @see TypeVariable
     */
    Type[] returnTypeGenericTypeParameters();

    /**
     * <p>The externalized property method return type's generic type parameter, if the return type is
     * a generic type e.g. {@code Optional<String>}. 
     * 
     * <p>For example, we have a property method: {@code Optional<String> awesomeMethod();},
     * {@code Class<String>} shall be returned when this method is invoked.
     * 
     * @param typeParameterIndex The type parameter index to get.
     * @return The generic return type parameter, if the return type is a generic type 
     * e.g. {@code Optional<String>}.
     */
    Optional<Type> returnTypeGenericTypeParameter(int typeParameterIndex);
    
    /**
     * Check whether the externalized property method is a default interface method.
     * 
     * @return {@code true}, if the externalized property method is a default interface method.
     * Otherwise, {@code false}.
     */
    boolean isDefaultInterfaceMethod();

    /**
     * The method signature string.
     * 
     * @return The method signature string.
     */
    String methodSignatureString();
}
