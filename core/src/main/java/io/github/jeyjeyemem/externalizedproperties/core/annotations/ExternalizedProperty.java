package io.github.jeyjeyemem.externalizedproperties.core.annotations;

import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalExternalizedProperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields marked with this annotation will allow {@link InternalExternalizedProperties} 
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
