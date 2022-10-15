package io.github.joeljeremy.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate that the annotated method will serve as a facade to the
 * underlying registered Externalized Properties variable expander. Variable expander facades are
 * expected to be with a specific signature. The first parameter must be the value to expand ({@code
 * String}) and the return type must also be a {@code String}.
 *
 * <p>Examples:
 *
 * <blockquote>
 *
 * <pre>
 * public interface ProxyInterface {
 *   {@code @}VariableExpanderFacade
 *   String expandVariables(String value);
 *
 *   {@code // Invalid method signature. Method return type must be String.}
 *   {@code @}VariableExpanderFacade
 *   int expandVariables(String value);
 *
 *   {@code // Invalid method signature. Method must only accept 1 String argument.}
 *   {@code @}VariableExpanderFacade
 *   String expandVariables(int mustBeString);
 *
 *   {@code // Invalid method signature. Method must only accept 1 String argument.}
 *   {@code @}VariableExpanderFacade
 *   String expandVariables(String value, String anotherArg);
 * }
 * </pre>
 *
 * </blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface VariableExpanderFacade {}
