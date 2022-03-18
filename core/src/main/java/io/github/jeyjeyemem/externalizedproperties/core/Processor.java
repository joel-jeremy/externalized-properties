package io.github.jeyjeyemem.externalizedproperties.core;

/**
 * API for handling conversion of processing of resolved property values.
 */
public interface Processor {
    /**
     * Process property value.
     * 
     * @param context The processing context.
     * @return The processed value.
     */
    String process(ProcessingContext context);
}
