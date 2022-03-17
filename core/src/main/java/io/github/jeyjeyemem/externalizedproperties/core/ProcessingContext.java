package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorAttribute;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorClasses;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

public class ProcessingContext {
    private final ProxyMethod proxyMethod;
    private final String value;
    private final List<Class<? extends Processor>> appliedProcessors;
    /** Nullable */
    private final ProcessorClasses processorClasses;

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
        this.appliedProcessors = requireNonNull(appliedProcessors, "appliedProcessors");
        this.processorClasses = 
            proxyMethod.findAnnotation(ProcessorClasses.class).orElse(null);
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
     * The {@code ProcessorClasses} containing the classes of
     * the processors that need to be applied.
     * 
     * @return The {@code ProcessorClasses} containing the classes of
     * the processors that need to be applied.
     */
    public Optional<ProcessorClasses> processorClasses() {
        return Optional.ofNullable(processorClasses);
    }

    /**
     * Get the processor attributes for the specified processor.
     * 
     * @param processorClass The processor class to get attributes for.
     * @return The processor attributes for the specified processor.
     */
    public Map<String, String> getAttributesFor(
            Class<? extends Processor> processorClass
    ) {
        requireNonNull(processorClass, "processorClass");

        if (processorClasses == null) {
            return Collections.emptyMap();
        }

        Map<String, String> attributesForProcessor = 
            Arrays.stream(processorClasses.attributes())
                .filter(p ->
                    // If no forProcessors, add attribute for all processors
                    p.forProcessors().length == 0 ||
                    // If there is forProcessors, only add to those specific processors
                    Arrays.stream(p.forProcessors())
                        .anyMatch(fp -> fp.equals(processorClass))
                )
                .collect(Collectors.toMap(
                    ProcessorAttribute::name, 
                    ProcessorAttribute::value
                ));

        return Collections.unmodifiableMap(attributesForProcessor);
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
        List<Class<? extends Processor>> updatedProcessors =
            new ArrayList<>(this.appliedProcessors.size() + 1);
        updatedProcessors.addAll(appliedProcessors);
        updatedProcessors.add(appliedProcessor);

        return new ProcessingContext(
            proxyMethod, 
            value,
            updatedProcessors
        );
    }
}
