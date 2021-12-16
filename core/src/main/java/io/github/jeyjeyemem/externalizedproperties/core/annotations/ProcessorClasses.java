package io.github.jeyjeyemem.externalizedproperties.core.annotations;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesBuilder;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with this annotation will undergo property processing
 * using the specified process classes.
 * 
 * @apiNote Processor classes defined in this annotation should be registered via 
 * {@link ExternalizedPropertiesBuilder#processors(Processor...)} or 
 * {@link ExternalizedPropertiesBuilder#processors(java.util.Collection)} in
 * order for processing to take place. Otherwise, {@link ExternalizedProperties} 
 * will not be able to find an instance for the processor class will throw an 
 * exception.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProcessorClasses {
    /**
     * The processor classes to process the externalized property.
     * 
     * @return The array of processor classes to process the externalized property.
     */
    Class<? extends Processor>[] value();
}
