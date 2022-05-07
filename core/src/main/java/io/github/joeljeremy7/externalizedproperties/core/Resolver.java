package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.util.Optional;

/**
 * The mechanism to resolve properties from various external sources.
 */
public interface Resolver {
    /**
     * Resolve property from an external source.
     * 
     * @param proxyMethod The proxy method.
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    Optional<String> resolve(ProxyMethod proxyMethod, String propertyName);
}
