package io.github.joeljeremy.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify which externalized property should be mapped to the target
 * method.
 *
 * <p>Examples:
 *
 * <blockquote>
 *
 * <pre>
 * public interface ProxyInterface {
 *   {@code @}ExternalizedProperty("my.property.name")
 *   String property();
 *
 *   {@code @}ExternalizedProperty("my.int.property.name")
 *   int intProperty();
 *
 *   {@code @}ExternalizedProperty("my.property.name")
 *   default String propertyOrElse(String someDefaultValue) {
 *     return someDefaultValue;
 *   }
 *
 *   {@code @}ExternalizedProperty("my.int.property.name")
 *   default int intPropertyOrElse(int someDefaultValue) {
 *     return someDefaultValue;
 *   }
 * }
 * </pre>
 *
 * </blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExternalizedProperty {
  /**
   * The name of the externalized property.
   *
   * @return The name of the externalized property.
   */
  String value();
}
