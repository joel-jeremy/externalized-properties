package io.github.joeljeremy7.externalizedproperties.core;

/**
 * API for handling conversion of processing of resolved property values.
 */
public interface Processor {
    /**
     * Process property value.
     * 
     * @param context The proxy method invocation context.
     * @param valueToProcess The value to process.
     * @return The processed value.
     */
    String process(InvocationContext context, String valueToProcess);
}
