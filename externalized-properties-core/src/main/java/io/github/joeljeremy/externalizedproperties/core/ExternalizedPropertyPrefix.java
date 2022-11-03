package io.github.joeljeremy.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify a value to be used as prefix to the names of externalized
 * properties that are declared on the annotated proxy interface.
 *
 * <p>Examples:
 *
 * <blockquote>
 *
 * <pre>
 *
 * {@code @ExternalizedPropertyPrefix("myprefix")}
 * public interface DataSourceProperties {
 *   {@code @ExternalizedProperty("datasource.connectionString")}
 *   String connectionString();
 *
 *   {@code @ExternalizedProperty("datasource.username")}
 *   String username();
 *
 *   {@code @ExternalizedProperty("datasource.password")}
 *   String password();
 * }
 *
 * </pre>
 *
 * The above proxy interface will look for the following properties:
 *
 * <ul>
 *   <li>myprefix.datasource.connectionString
 *   <li>myprefix.datasource.username
 *   <li>myprefix.datasource.password
 * </ul>
 *
 * </blockquote>
 *
 * <p>Example with custom delimiter:
 *
 * <blockquote>
 *
 * <pre>
 *
 * {@code @ExternalizedPropertyPrefix(value = "/myprefix", delimiter = "/")}
 * public interface DataSourceProperties {
 *   {@code @ExternalizedProperty("datasource/connectionString")}
 *   String connectionString();
 *
 *   {@code @ExternalizedProperty("datasource/username")}
 *   String username();
 *
 *   {@code @ExternalizedProperty("datasource/password")}
 *   String password();
 * }
 *
 * </pre>
 *
 * The above proxy interface will look for the following properties:
 *
 * <ul>
 *   <li>/myprefix/datasource/connectionString
 *   <li>/myprefix/datasource/username
 *   <li>/myprefix/datasource/password
 * </ul>
 *
 * </blockquote>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExternalizedPropertyPrefix {
  /**
   * The prefix to prepend to the names of externalized properties that belong to the annotated
   * proxy interface.
   *
   * @return The prefix to prepend to the names of externalized properties that belong to the
   *     annotated proxy interface.
   */
  String value();

  /**
   * The delimiter to use to delimit the prefix and the externalized property name.
   *
   * @return The delimiter to use to delimit the prefix and the externalized property name.
   */
  String delimiter() default ".";
}
