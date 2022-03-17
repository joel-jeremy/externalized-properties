package io.github.jeyjeyemem.externalizedproperties.core.annotations;

import io.github.jeyjeyemem.externalizedproperties.core.Processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attributes that will be made accessible to processors during processing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProcessorAttribute {
    /**
     * The name of the attribute.
     * @return The name of the attribute.
     */
    String name();

    /**
     * The value of the attribute.
     * @return The value of the attribute.
     */
    String value();

    /**
     * The processors which this processor attribute is for.
     * By defining {@link #forProcessors}, the processor attribute
     * will only be avaiable to the specified processors. Otherwise,
     * the processor attribute will be made avaiable to all processors.
     * 
     * @return The processors which this processor attribute is for.
     */
    Class<? extends Processor>[] forProcessors() default {};
}
