package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * The core API for Externalized Properties.
 */
public interface ExternalizedProperties {
    /**
     * Create a proxy that handles property resolution according to the invoked method.
     * 
     * @implNote The names specified in the {@link ExternalizedProperty} supports variables
     * e.g. "${my.variable}.property". These variables will be expanded accordingly.
     * 
     * @param <T> The type of the proxy interface.
     * @param proxyInterface The interface whose methods are annotated with 
     * {@link ExternalizedProperty}. Only an interface will be accepted. 
     * If a non-interface is given, an exception will be thrown. 
     * @return The proxy instance implementing the specified interface.
     */
    <T> T proxy(Class<T> proxyInterface);

    /**
     * Create a proxy that handles property resolution according to the invoked method.
     * 
     * @implNote The names specified in the {@link ExternalizedProperty} supports variables
     * e.g. "${my.variable}.property". These variables will be expanded accordingly.
     * 
     * @param <T> The type of the proxy interface.
     * @param proxyInterface The interface whose methods are annotated with 
     * {@link ExternalizedProperty}. Only an interface will be accepted. 
     * If a non-interface is given, an exception will be thrown. 
     * @param classLoader The class loader to define the proxy class.
     * @return The proxy instance implementing the specified interface.
     */
    <T> T proxy(Class<T> proxyInterface, ClassLoader classLoader);

    /**
     * Resolve property for the given the proxy method info.
     * The property value will automatically be according to the proxy method's 
     * return type.
     * 
     * @param proxyMethodInfo The externalized properties proxy method info.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    Optional<?> resolveProperty(ProxyMethodInfo proxyMethodInfo);

    /**
     * Resolve property from an external source.
     * 
     * @param propertyName The property name to resolve.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    Optional<String> resolveProperty(String propertyName);

    /**
     * Resolve property from an external source and apply some processing.
     * 
     * @param propertyName The property name to resolve.
     * @param processors The processors to apply to the resolved property.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    Optional<String> resolveProperty(
        String propertyName,
        Processors processors
    );

    /**
     * Resolve property from an external source and convert to the target type.
     * 
     * @param <T> The target property type.
     * @param propertyName The property name to resolve.
     * @param targetType The target property type.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    <T> Optional<T> resolveProperty(
        String propertyName,
        Class<T> targetType
    );

    /**
     * Resolve property from an external source, apply some processing, 
     * and convert to the target type.
     * 
     * @param <T> The target property type.
     * @param propertyName The property name to resolve.
     * @param processors The processors to apply to the resolved property.
     * @param targetType The target property type.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    <T> Optional<T> resolveProperty(
        String propertyName,
        Processors processors,
        Class<T> targetType
    );

    /**
     * Resolve property from an external source and convert to the target type.
     * 
     * @param <T> The target property type.
     * @param propertyName The property name to resolve.
     * @param targetType The target property type.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    <T> Optional<T> resolveProperty(
        String propertyName,
        TypeReference<T> targetType
    );

     /**
     * Resolve property from an external source, apply some processing, 
     * and convert to the target type.
     * 
     * @param <T> The target property type.
     * @param propertyName The property name to resolve.
     * @param processors The processors to apply to the resolved property.
     * @param targetType The target property type.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    <T> Optional<T> resolveProperty(
        String propertyName,
        Processors processors,
        TypeReference<T> targetType
    );

    /**
     * Resolve property from an external source and convert to the target type.
     * 
     * @param propertyName The property name to resolve.
     * @param targetType The target property type.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    Optional<?> resolveProperty(
        String propertyName,
        Type targetType
    );

    /**
     * Resolve property from an external source, apply some processing, 
     * and convert to the target type.
     * 
     * @param propertyName The property name to resolve.
     * @param processors The processors to apply to the resolved property.
     * @param targetType The target property type.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    Optional<?> resolveProperty(
        String propertyName,
        Processors processors,
        Type targetType
    );

    /**
     * Expand variable in the given string.
     * 
     * @param source The source string.
     * @return The resulting string where variables have been expanded.
     */
    String expandVariables(String source);
}
