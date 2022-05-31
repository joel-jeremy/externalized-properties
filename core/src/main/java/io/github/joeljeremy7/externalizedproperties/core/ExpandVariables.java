package io.github.joeljeremy7.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify that the annotated method will serve as a
 * variable expander method. Variable expander methods are expected to be with a 
 * specific signature. The first parameter should be the value to expand ({@code String})
 * and the return type must also be a {@code String}.
 * 
 * <p>Examples:</p>
 * 
 * <blockquote><pre> 
 * public interface ProxyInterface {
 *     {@code @}ExpandVariables
 *     String expandVariables(String value);
 *     
 *     {@code // Invalid method signature. Method return type must be String.}
 *     {@code @}ExpandVariables
 *     int expandVariables(String value);
 * 
 *     {@code // Invalid method signature. Method must only accept 1 String argument.}
 *     {@code @}ExpandVariables
 *     String expandVariables(int mustBeString);
 *
 *     {@code // Invalid method signature. Method must only accept 1 String argument.}
 *     {@code @}ExpandVariables
 *     String expandVariables(String value, String anotherArg);
 * } 
 * </pre></blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExpandVariables {}
