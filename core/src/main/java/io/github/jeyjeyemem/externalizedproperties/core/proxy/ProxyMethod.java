package io.github.jeyjeyemem.externalizedproperties.core.proxy;

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
public interface ProxyMethod {
    /**
     * The {@link ExternalizedProperty} annotation, if proxy method is annotated.
     * 
     * @return The externalized property annotation.
     * Otherwise, an empty {@link Optional}.
     */
    Optional<ExternalizedProperty> externalizedPropertyAnnotation();

    /**
     * The externalized property name, if proxy method is annotated with 
     * {@link ExternalizedProperty}. The property name is derived from
     * {@link ExternalizedProperty#value()}.
     * 
     * @return The externalized property name as specified in 
     * {@link ExternalizedProperty#value()}. Otherwise, an empty {@link Optional}.
     */
    Optional<String> externalizedPropertyName();

    /**
     * The array of annotations the proxy method is annotated with.
     * @return The array of annotations the proxy method is annotated with.
     */
    Annotation[] annotations();

    /**
     * Find proxy method annotation.
     * 
     * @param <T> The type of the annotation.
     * @param annotationClass Annotation class to find.
     * @return The annotation matching the specified annotation class.
     * Otherwise, an empty {@link Optional}.
     */
    <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass);

    /**
     * Check whether the proxy method is annotated with the specified annotation.
     * 
     * @param <T> The type of the annonation.
     * @param annotationClass The annotation class to check.
     * @return {@code true}, if the proxy method is annotated with the specified 
     * annotation. Otherwise, {@code false}.
     */
    <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass);

    /**
     * The class or interface that declares the method.
     * 
     * @return The class or interface that declares the method.
     */
    Class<?> declaringClass();

    /**
     * The proxy method's name.
     * 
     * @return The proxy method's name.
     */
    String name();

    /**
     * The proxy method's return type.
     * 
     * @return The proxy method's return type.
     */
    Class<?> returnType();

    /**
     * The proxy method's generic return type.
     * 
     * @return The proxy method's generic return type.
     */
    Type genericReturnType();

    /**
     * The proxy method's parameter types.
     * 
     * @return The proxy method's parameter types.
     */
    Class<?>[] parameterTypes();

    /**
     * The proxy method's generic parameter types.
     * 
     * @return The proxy method's generic parameter types.
     */
    Type[] genericParameterTypes();

    /**
     * Check if the proxy method's return type matches the given type. 
     * 
     * @param type The class to match against the proxy method's return type.
     * @return {@code true}, if the proxy method's return type matches 
     * the given type. Otherwise, {@code false}.
     */
    boolean hasReturnType(Class<?> type);

    /**
     * Check if the proxy method's return type matches the given type. 
     * 
     * @param type The type to match against the proxy method's return type.
     * @return {@code true}, if the proxy method's return type matches 
     * the given type. Otherwise, {@code false}.
     */
    boolean hasReturnType(Type type);

    /**
     * <p>The proxy method return type's generic type parameters, 
     * if the return type is a generic parameterized type e.g. {@code List<String>}. 
     * 
     * <p>For example, if {@link #genericReturnType()} returns a parameterized type 
     * e.g. {@code List<String>}, this method shall return an array containing a 
     * {@code String} type/class.
     * 
     * <p>Another example is if {@link #genericReturnType()} returns an array type with a 
     * generic component type e.g. {@code Optional<Integer>[]}, this method shall return an 
     * array containing an {@code Integer} type/class.
     * 
     * <p>It is also possible to have {@link #genericReturnType()} return a parameterized type 
     * which contains another parameterized type parameter e.g. {@code Optional<List<String>>}, 
     * in this case, this method shall return an array containing a {@code List<String>} 
     * parameterized type.
     * 
     * @return The array of generic return type parameters, if the return type is a generic 
     * parameterized type e.g. {@code Optional<String>}.
     * @see ParameterizedType
     * @see GenericArrayType
     * @see WildcardType
     * @see TypeVariable
     */
    Type[] returnTypeGenericTypeParameters();

    /**
     * <p>The proxy method return type's generic type parameter on the given index, 
     * if the return type is a generic type e.g. {@code Optional<String>}. 
     * 
     * <p>For example, we have a property method: {@code Optional<String> awesomeMethod();},
     * {@code returnTypeGenericTypeParameter(0)} shall return a {@code String} type/class.
     * 
     * @param typeParameterIndex The type parameter index to get.
     * @return The generic return type parameter, if the return type is a generic type 
     * e.g. {@code Optional<String>}.
     */
    Optional<Type> returnTypeGenericTypeParameter(int typeParameterIndex);
    
    /**
     * Check whether the proxy method is a default interface method.
     * 
     * @return {@code true}, if the proxy method is a default interface method.
     * Otherwise, {@code false}.
     */
    boolean isDefaultInterfaceMethod();

    /**
     * The proxy method signature string.
     * 
     * @return The proxy method signature string.
     */
    String methodSignatureString();
}
