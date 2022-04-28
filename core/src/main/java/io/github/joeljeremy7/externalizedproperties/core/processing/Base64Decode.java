package io.github.joeljeremy7.externalizedproperties.core.processing;

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
     * The encoding of the Base 64 string.
     * 
     * Valid values are:
     * <ul>
     *  <li>"basic" (RFC4648)</li>
     *  <li>"url" (RFC4648 URL and file name safe)</li>
     *  <li>"mime" (RFC2045)</li>
     * </ul>
     * 
     * (Do not include the text enclosed in parenthesis)
     * 
     * <p>If no encoding if provided, {@link Base64DecodeProcessor} will
     * use the default configured decoder.
     * 
     * @return The encoding of the Base 64 string.
     */
    String encoding() default "";

    /**
     * The charset of the resulting base 64 decoded String.
     * 
     * @return The charset of the resulting base 64 decoded String.
     */
    String charset() default "";
}
