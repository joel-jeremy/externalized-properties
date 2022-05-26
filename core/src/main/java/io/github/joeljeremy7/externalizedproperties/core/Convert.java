package io.github.joeljeremy7.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * This annotation is used to specify that the annotated method will serve as a
 * converter method. Converter methods are expected to be with a specific signature. 
 * The first parameter should be the value to convert (String) and the second 
 * parameter should be the target type which may be represented by the following 
 * classes: {@link TypeReference}, {@link Class}, or {@link Type}. The return type
 * can be a generic type variable e.g. {@code <T>} or any type as long as the target
 * type is assignable to it. If a value is converted to a target type and converted
 * value is not assignable to the method's return type, a {@link ClassCastException}
 * will be thrown.
 * 
 * <p>Examples:</p>
 * 
 * <blockquote><pre> 
 * public interface ProxyInterface {
 *     {@code @}Convert
 *     {@code <T>} T convert(String value, TypeReference{@code <T>} targetType);
 * 
 *     {@code @}Convert
 *     {@code <T>} T convert(String value, Class{@code <T>} targetType);
 * 
 *     {@code // Warning: May throw if return value is assigned to a variable that is not assignable from the target type.}
 *     {@code // It may be better to use Object as return type when working Type to avoid possible ClassCastExceptions.}
 *     {@code @}Convert
 *     {@code <T>} T convert(String value, Type targetType);
 * 
 *     {@code @}Convert
 *     Object convertToObject(String value, TypeReference{@code <?>} targetType);
 * 
 *     {@code @}Convert
 *     Object convertToObject(String value, Class{@code <?>} targetType);
 * 
 *     {@code @}Convert
 *     Object convertToObject(String value, Type targetType);
 * 
 *     {@code // Will result in a ClassCastException if target type reference is not Integer.}
 *     {@code @}Convert
 *     Integer convertToObject(String value, TypeReference{@code <?>} targetType);
 * 
 *     {@code // Will result in a ClassCastException if target class is not Integer.}
 *     {@code @}Convert
 *     Integer convertToObject(String value, Class{@code <?>} targetType);
 * 
 *     {@code // Will result in a ClassCastException if target type is not Integer.}
 *     {@code @}Convert
 *     Integer convertToObject(String value, Type targetType);
 * } 
 * </pre></blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Convert {}
