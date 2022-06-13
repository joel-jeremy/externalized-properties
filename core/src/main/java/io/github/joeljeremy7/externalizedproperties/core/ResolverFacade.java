package io.github.joeljeremy7.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * This annotation is used to indicate that the annotated method will serve as a facade 
 * to the underlying registered Externalized Properties resolvers. 
 * 
 * Resolver facades are expected to be with a specific signature. The first parameter must 
 * be the property name ({@code String}). The return type can be any type. Conversion will 
 * automatically be done.
 * 
 * Optionally, it is also possible to specify a second parameter: the target type for the property
 * which may be represented by the following classes: {@link TypeReference}, {@link Class}, or 
 * {@link Type}. The method's return type can be a generic type variable e.g. {@code <T>} or any 
 * type as long as the target type is assignable to the it. 
 * 
 * If a property is converted to the target type and the converted value is not assignable to the 
 * method's return type, a {@link ClassCastException} will be thrown.
 * 
 * <p>Examples:</p>
 * 
 * <blockquote><pre> 
 * public interface ProxyInterface {
 *     {@code @}ResolverFacade
 *     String resolve(String propertyName);
 *     
 *     {@code @}ResolverFacade
 *     int resolveInt(String propertyName);
 *     
 *     {@code @}ResolverFacade
 *     {@code <T>} T resolve(String propertyName, TypeReference{@code <T>} targetType);
 * 
 *     {@code @}ResolverFacade
 *     {@code <T>} T resolve(String propertyName, Class{@code <T>} targetType);
 * 
 *     {@code @}ResolverFacade
 *     Object resolve(String propertyName, Type targetType);
 * 
 *     {@code // Invalid method signature.}
 *     {@code // Method must either accept 1 or 2 arguments: The property name (String) and the target type, respectively.}
 *     {@code @}ResolverFacade
 *     String resolve(int mustBeString);
 *
 *     {@code // Invalid method signature.}
 *     {@code // Method must either accept 1 or 2 arguments: The property name (String) and the target type, respectively.}
 *     {@code @}ResolverFacade
 *     String resolve(String propertyName, String anotherArg); 
 * }
 * </pre></blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResolverFacade {}
