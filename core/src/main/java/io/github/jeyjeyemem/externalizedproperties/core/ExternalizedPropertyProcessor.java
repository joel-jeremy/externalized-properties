package io.github.jeyjeyemem.externalizedproperties.core;
/**
 * API for processing of resolved properties.
 */
public interface ExternalizedPropertyProcessor {
    /**
     * Process property value.
     * 
     * @param property The property value to process.
     * @return The processed property value.
     */
    String processProperty(String property);
}
