package io.github.joeljeremy7.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with this annotation will allow {@link ExternalizedProperties} 
 * to load property values from external sources. The name of the externalized property 
 * will be derived from {@link #value()}, if it is specified. Otherwise,
 * the property name will be derived from the annotated method's arguments.
 * Specifically, the first argument of the annotated method (the method must only have 
 * one {@code String} argument).
 * 
 * <p>Examples:</p>
 * 
 * <blockquote><pre> 
 * public interface ProxyInterface {
 *     {@code @}ExternalizedProperty("my.property.name")
 *     String property();
 * 
 *     {@code @}ExternalizedProperty("my.int.property.name")
 *     int intProperty();
 *
 *     {@code @}ExternalizedProperty("my.property.name")
 *     default String propertyOrElse(String someDefaultValue) {
 *         return someDefaultValue;
 *     }
 * 
 *     {@code @}ExternalizedProperty("my.int.property.name")
 *     default int intPropertyOrElse(int someDefaultValue) {
 *         return someDefaultValue;
 *     }
 * 
 *     {@code @}ExternalizedProperty
 *     String resolve(String propertyName);
 * 
 *     {@code @}ExternalizedProperty
 *     int resolveInt(String propertyName);
 * 
 *     {@code // Invalid method signature. Method must only accept 1 String argument.}
 *     {@code @}ExternalizedProperty
 *     String resolve(int mustBeString);
 *
 *     {@code // Invalid method signature. Method must only accept 1 String argument.}
 *     {@code @}ExternalizedProperty
 *     String resolve(String propertyName, String anotherArg); 
 * }
 * </pre></blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExternalizedProperty {
    /**
     * The name of the externalized property. If a {@link #value()} is not specified or
     * is empty ({@code ""}), the property name will be derived from the annotated method's 
     * arguments. Specifically, the first argument of the annotated method (the method must 
     * only have one {@code String} argument) e.g. 
     * <blockquote><pre>String resolve(String propertyName)</pre></blockquote>
     * 
     * @return The name of the externalized property. If a {@link #value()} is not specified
     * or is empty ({@code ""}), the property name will be derived from the annotated method's 
     * arguments. Specifically, the first argument of the annotated method (the method must 
     * only have one {@code String} argument) e.g. 
     * <blockquote><pre>String resolve(String propertyName)</pre></blockquote>
     */
    String value() default "";
}
