package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

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
     * The configured {@code Resolver} for this {@link ExternalizedProperties} instance.
     * @return The configured {@code Resolver} for this {@link ExternalizedProperties} 
     * instance.
     */
    Resolver resolver();

    /**
     * The configured {@code Converter} for this {@link ExternalizedProperties} instance.
     * @return The configured {@code Converter} for this {@link ExternalizedProperties} 
     * instance.
     */
    Converter<?> converter();

    /**
     * The configured {@code VariableExpander} for this {@link ExternalizedProperties} 
     * instance.
     * @return The configured {@code VariableExpander} for this 
     * {@link ExternalizedProperties} instance.
     */
    VariableExpander variableExpander();

    /**
     * The configured {@code Processor} for this {@link ExternalizedProperties} instance.
     * @return The configured {@code Processor} for this {@link ExternalizedProperties} 
     * instance.
     */
    Processor processor();
}
