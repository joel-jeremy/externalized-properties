package io.github.jeyjeyemem.externalizedproperties.core.annotations;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with this annotation will allow {@link ExternalizedProperties} 
 * to load their values from external sources.
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
