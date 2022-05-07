package io.github.joeljeremy7.externalizedproperties.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a delimiter which can be used to override 
 * the default delimiter in collection/array converters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delimiter {
    /**
     * The property value delimiter.
     * 
     * @return The property value delimiter.
     */
    String value();
}
