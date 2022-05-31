package io.github.joeljeremy7.externalizedproperties.core.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Optional;

/**
 * Represents a method which has been invoked via an Externalized Properties proxy.
 */
public interface ProxyMethod {

    /**
     * The {@link ExternalizedProperties} instance which created the proxy declaring this 
     * method.
     * 
     * @return The {@link ExternalizedProperties} instance which created the proxy declaring 
     * this method.
     */
    ExternalizedProperties externalizedProperties();

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
     * The proxy method's raw return type. This does not contain
     * generic type information.
     * 
     * @return The proxy method's raw return type.
     */
    Class<?> rawReturnType();

    /**
     * The proxy method's generic return type. This does contain
     * generic type information.
     * 
     * @return The proxy method's generic return type.
     */
    Type returnType();

    /**
     * The proxy method's parameter types. These do not contain
     * generic type information.
     * 
     * @return The proxy method's parameter types.
     */
    Class<?>[] rawParameterTypes();

    /**
     * The proxy method's generic parameter types. These do contain
     * generic type information.
     * 
     * @return The proxy method's generic parameter types.
     */
    Type[] parameterTypes();

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
     * <p>For example, if {@link #returnType()} returns a parameterized type 
     * e.g. {@code List<String>}, this method shall return an array containing a 
     * {@code String} type/class.
     * 
     * <p>Another example is if {@link #returnType()} returns an array type with a 
     * generic component type e.g. {@code Optional<Integer>[]}, this method shall return an 
     * array containing an {@code Integer} type/class.
     * 
     * <p>It is also possible to have {@link #returnType()} return a parameterized type 
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
    Type[] typeParametersOfReturnType();

    /**
     * <p>The proxy method return type's generic type parameter on the given index, 
     * if the return type is a generic type e.g. {@code Optional<String>}. 
     * 
     * <p>For example, we have a property method: {@code Optional<String> awesomeMethod();},
     * {@code returnTypeGenericTypeParameter(0)} shall return a {@code String} type/class.
     * 
     * @param typeParameterIndex The index of the type parameter to get.
     * @return The generic return type parameter, if the return type is a generic type 
     * e.g. {@code Optional<String>}.
     */
    Optional<Type> typeParameterOfReturnTypeAt(int typeParameterIndex);
    
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
