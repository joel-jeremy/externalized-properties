package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

/**
 * API for handling conversion of processing of resolved property values.
 */
public interface Processor {
    /**
     * Process property value.
     * 
     * @param proxyMethod The proxy method.
     * @param valueToProcess The value to process.
     * @return The processed value.
     */
    String process(ProxyMethod proxyMethod, String valueToProcess);
}
