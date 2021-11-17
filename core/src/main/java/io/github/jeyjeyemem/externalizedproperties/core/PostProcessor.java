package io.github.jeyjeyemem.externalizedproperties.core;
/**
 * API for post processing of resolved properties.
 */
public interface PostProcessor {
    /**
     * Post-process resolved property.
     * 
     * @param externalizedPropertyMethodInfo The externalized property method info.
     * @param resolvedProperty The resolved property to post-process.
     * @return The post-processed property.
     */
    ResolvedProperty postProcess(
        ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
        ResolvedProperty resolvedProperty
    );
}
