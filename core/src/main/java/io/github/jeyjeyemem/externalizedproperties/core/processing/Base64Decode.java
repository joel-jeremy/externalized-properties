package io.github.jeyjeyemem.externalizedproperties.core.processing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Processor annotation to apply base64 decoding to the value.
 */
@ProcessWith(Base64DecodeProcessor.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Base64Decode {
    /**
     * The decoder to use.
     * 
     * @return The decoder to use.
     */
    String decoder() default "";
}
