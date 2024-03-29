package io.github.joeljeremy.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * This annotation is used to indicate that the annotated method will serve as a facade to the
 * underlying registered Externalized Properties converters.
 *
 * <p>Converter facades are expected to be with a specific signature. The first parameter must be
 * the value to convert ({@code String}) and the second parameter must be the target type which may
 * be represented by the following classes: {@link TypeReference}, {@link Class}, or {@link Type}.
 * The method's return type can be a generic type variable e.g. {@code <T>} or any type as long as
 * the target type is assignable to it.
 *
 * <p>Optionally, the second parameter (target type) can be omitted. In doing so, The target type
 * will be derived from the method's return type.
 *
 * <p>If a value is converted to a target type and converted value is not assignable to the method's
 * return type, a {@link ClassCastException} will be thrown.
 *
 * <p>Examples:
 *
 * <blockquote>
 *
 * <pre>
 * public interface ProxyInterface {
 *   {@code @}ConverterFacade
 *   {@code <T>} T convert(String value, TypeReference{@code <T>} targetType);
 *
 *   {@code @}ConverterFacade
 *   {@code <T>} T convert(String value, Class{@code <T>} targetType);
 *
 *   {@code @}ConverterFacade
 *   Object convert(String value, Type targetType);
 *
 *   {@code @}ConverterFacade
 *   int convertToInt(String value);
 *
 *   {@code @}ConverterFacade
 *   List{@code <String>} convertToList(String value);
 *
 *   {@code // This is allowed but the recommended approach is to declare method with generic return type.}
 *   {@code @}ConverterFacade
 *   Object convertToObject(String value, TypeReference{@code <?>} targetType);
 *
 *   {@code // This is allowed but the recommended approach is to declare method with generic return type.}
 *   {@code @}ConverterFacade
 *   Object convertToObject(String value, Class{@code <?>} targetType);
 *
 *   {@code // This is allowed but the recommended approach is to declare method with generic return type.}
 *   {@code // Warning: Will result in a ClassCastException if target type reference is not int.}
 *   {@code @}ConverterFacade
 *   int convertToInt(String value, TypeReference{@code <?>} targetType);
 *
 *   {@code // This is allowed but the recommended approach is to declare method with generic return type.}
 *   {@code // Warning: Will result in a ClassCastException if target class is not int.}
 *   {@code @}ConverterFacade
 *   int convertToInt(String value, Class{@code <?>} targetType);
 *
 *   {@code // This is allowed but the recommended approach is to declare method with generic return type.}
 *   {@code // Warning: Will result in a ClassCastException if target type is not int.}
 *   {@code @}ConverterFacade
 *   int convertToInt(String value, Type targetType);
 *
 *   {@code // Invalid method signature.}
 *   {@code // Method must either accept 1 or 2 arguments: the value to convert (String) and the target type, respectively.}
 *   {@code @}ConverterFacade
 *   {@code <T>} T convertInvalidSignature(String value, String invalidArgType);
 *
 *   {@code // Invalid method signature.}
 *   {@code // Method must either accept 1 or 2 arguments: the value to convert (String) and the target type, respectively.}
 *   {@code @}ConverterFacade
 *   {@code <T>} T convertInvalidSignature(String value, Class{@code <?>} targetType, String anotherArg);
 *
 *   {@code // Invalid method signature.}
 *   {@code // Method must either accept 1 or 2 arguments: the value to convert (String) and the target type, respectively.}
 *   {@code @}ConverterFacade
 *   {@code <T>} T convertInvalidSignature();
 * }
 * </pre>
 *
 * </blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConverterFacade {}
