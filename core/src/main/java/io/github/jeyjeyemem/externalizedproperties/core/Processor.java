package io.github.jeyjeyemem.externalizedproperties.core;
/**
 * API for processing of properties.
 */
public interface Processor {
    /**
     * Process property value.
     * 
     * @param property The property value to process.
     * @return The processed property value.
     */
    String processProperty(String property);
}
