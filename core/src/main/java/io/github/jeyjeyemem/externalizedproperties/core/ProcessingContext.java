package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Context object for {@link Processor}s.
 */
public class ProcessingContext {
    private final ProxyMethod proxyMethod;
    private final String value;
    private final List<Class<? extends Processor>> appliedProcessors;

    /**
     * Constructor.
     * 
     * @param proxyMethod The proxy method.
     * @param value The value to process.
     */
    public ProcessingContext(ProxyMethod proxyMethod, String value) {
        this(proxyMethod, value, Collections.emptyList());
    }

    /**
     * Constructor.
     * 
     * @param proxyMethod The proxy method.
     * @param value The value to process.
     * @param appliedProcessors The processors that were already applied to
     * the value.
     */
    private ProcessingContext(
            ProxyMethod proxyMethod, 
            String value,
            List<Class<? extends Processor>> appliedProcessors
    ) {
        this.proxyMethod = requireNonNull(proxyMethod, "proxyMethod");
        this.value = requireNonNull(value, "value");
        this.appliedProcessors = Collections.unmodifiableList(
            requireNonNull(appliedProcessors, "appliedProcessors")
        );
    }

    /**
     * The value to process.
     * 
     * @return The value to process.
     */
    public String value() {
        return value;
    }

    /**
     * The proxy method.
     * 
     * @return The proxy method
     */
    public ProxyMethod proxyMethod() {
        return proxyMethod;
    }

    /**
     * The processors that were already applied to the value.
     * 
     * @return The processors that were already applied to the value.
     */
    public List<Class<? extends Processor>> appliedProcessors() {
        return appliedProcessors;
    }

    /**
     * 
     * Create a new {@link ProcessingContext} based on this instance but
     * with updated value.
     * 
     * @param value The value to process.
     * @param appliedProcessor The processor which was applied to the value.
     * @return The new {@link ProcessingContext} based on this instance.
     */
    public ProcessingContext with(
            String value, 
            Class<? extends Processor> appliedProcessor
    ) {
        requireNonNull(value, "value");
        requireNonNull(appliedProcessor, "appliedProcessor");
        
        List<Class<? extends Processor>> updatedProcessors =
            new ArrayList<>(appliedProcessors.size() + 1);
        updatedProcessors.addAll(appliedProcessors);
        updatedProcessors.add(appliedProcessor);

        return new ProcessingContext(
            proxyMethod, 
            value,
            updatedProcessors
        );
    }
}
