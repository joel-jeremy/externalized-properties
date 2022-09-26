package io.github.joeljeremy7.externalizedproperties.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.format.DateTimeFormatter;

/**
 * Specify a date/time format which can be used to override the default date/time format in
 * date/time converters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DateTimeFormat {
  /**
   * The date/time format to use in parsing date/time properties. See {@link DateTimeFormatter}
   * documentation for valid formats/patterns.
   *
   * @see DateTimeFormatter
   * @return The date/time format to use in parsing date/time properties.
   */
  String value();
}
