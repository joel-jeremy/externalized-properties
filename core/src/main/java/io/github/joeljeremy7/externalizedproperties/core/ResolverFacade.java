package io.github.joeljeremy7.externalizedproperties.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate that the annotated method will serve as a facade 
 * to the underlying registered Externalized Properties resolvers. Resolver facades are 
 * expected to be with a specific signature. The first parameter must be the property 
 * name ({@code String}). The return type can be any type. Conversion will automatically 
 * be done.
 * 
 * <p>Examples:</p>
 * 
 * <blockquote><pre> 
 * public interface ProxyInterface {
 *     {@code @}ResolverFacade
 *     String resolve(String propertyName);
 *     
 *     {@code @}ResolverFacade
 *     int resolveInt(String propertyName);
 * 
 *     {@code // Invalid method signature. Method must only accept 1 String argument.}
 *     {@code @}ResolverFacade
 *     String resolve(int mustBeString);
 *
 *     {@code // Invalid method signature. Method must only accept 1 String argument.}
 *     {@code @}ResolverFacade
 *     String resolve(String propertyName, String anotherArg); 
 * }
 * </pre></blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResolverFacade {}
