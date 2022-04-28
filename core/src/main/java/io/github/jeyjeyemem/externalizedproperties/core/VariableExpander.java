package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

/**
 * The mechanism that allows expansion of variables in strings.
 */
public interface VariableExpander {
    /**
     * Expand any variables that is in the given string.
     * 
     * @param proxyMethod The proxy method.
     * @param value The string value. This may contain variables e.g. 
     * "${some.app.property}_property_name" which will be expanded by this method.
     * @return The string of which variables have been expanded.
     */
    String expandVariables(ProxyMethod proxyMethod, String value);
}
