package io.github.joeljeremy7.externalizedproperties.core.processing;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.Processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation types annotated with this will be processed using the specified processor class. 
 * 
 * @apiNote Processor classes defined in this annotation should be registered via 
 * {@link ExternalizedProperties.Builder#processors(Processor...)} in
 * order for processing to take place. Otherwise, {@link ExternalizedProperties} 
 * will not be able to find an instance for the processor class will throw an 
 * exception.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProcessWith {
    /**
     * The type of the processor to process the annotated type.
     * 
     * @return The type of the processor to process the annotated type.
     */
    Class<? extends Processor> value();
}
