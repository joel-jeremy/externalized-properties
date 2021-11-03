package io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a delimiters which can be used to override 
 * the default delimiter in collection/array conversion handlers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delimiters {
    /**
     * The property value delimiters.
     * 
     * @return The property value delimiters.
     */
    Delimiter[] value();
}
