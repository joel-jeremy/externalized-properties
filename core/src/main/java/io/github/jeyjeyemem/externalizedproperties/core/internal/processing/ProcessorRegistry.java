package io.github.jeyjeyemem.externalizedproperties.core.internal.processing;

import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.Processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The registry for {@link Processor}s.
 */
public class ProcessorRegistry {

    private final ClassValue<Processor> processorClassValue;

    /**
     * Constructor.
     * 
     * @param processors The collection of processors to register. 
     */
    public ProcessorRegistry(Collection<Processor> processors) {
        requireNonNull(processors, "processors");

        this.processorClassValue = new ClassValue<Processor>() {
            @Override
            protected Processor computeValue(Class<?> processorClass) {
                for (Processor processor : processors) {
                    if (processorClass.equals(processor.getClass())) {
                        return processor;
                    }
                }
                throw new IllegalArgumentException(
                    "No processor registered for processor class: " + 
                    processorClass + ". Please make sure processor is registered " + 
                    "while building ExternalizedProperties." 
                );
            }
        };
    }

    /**
     * Get registered processor instances for the given processor classes. The resulting 
     * list should be in the same order as the list of processors in the {@link Processors}
     * instance.
     * 
     * @param processors The processors to get instances for.
     * @return The registered processor instances which matches the processor classes.
     * This will return an empty list if no instances were registered for any of the 
     * processor classes.
     */
    public List<Processor> getProcessors(Processors processors) {
        requireNonNull(processors, "processors");

        if (processors == Processors.NONE) {
            return Collections.emptyList();
        }

        List<Class<? extends Processor>> processorClasses = 
            processors.list();

        List<Processor> found = new ArrayList<>(
            processorClasses.size()
        );

        for (Class<?> processorClass : processorClasses) {
            Processor processor = 
                processorClassValue.get(processorClass);
            found.add(processor);
        }

        return found;
    }
}
