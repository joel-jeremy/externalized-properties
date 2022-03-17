package io.github.jeyjeyemem.externalizedproperties.core.internal.processing;

import io.github.jeyjeyemem.externalizedproperties.core.ProcessingContext;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorClasses;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ProcessingException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The default {@link Processor} implementation.
 * This delegates to a configured collection of {@link Processor}s.
 */
public class RootProcessor implements Processor {

    private final ClassValue<Processor> processorClassValue;

    /**
     * Constructor.
     * 
     * @param processors The collection of {@link Processor}s
     * to handle the actual processing.
     */
    public RootProcessor(Processor... processors) {
        this(Arrays.asList(
            requireNonNull(processors, "processors")
        ));
    }

    /**
     * Constructor.
     * 
     * @param processors The collection of {@link Processor}s
     * to handle the actual processing.
     */
    public RootProcessor(Collection<Processor> processors) {
        requireNonNull(processors, "processors");

        this.processorClassValue = new ClassValue<Processor>() {
            @Override
            protected Processor computeValue(Class<?> processorClass) {
                for (Processor handler : processors) {
                    if (Objects.equals(processorClass, handler.getClass())) {
                        return handler;
                    }
                }
                throw new ProcessingException(
                    "No processor registered for processor class: " + 
                    processorClass.getName() + ". Please make sure processor is " + 
                    "registered while building ExternalizedProperties." 
                );
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public String process(ProcessingContext context) {
        requireNonNull(context, "context");

        ProcessorClasses processorClasses = 
            context.processorClasses().orElse(null);
        if (processorClasses == null) {
            return context.value();
        }

        for (Class<? extends Processor> processorClass : processorClasses.value()) {
            Processor processor = processorClassValue.get(processorClass);
            context = context.with(processor.process(context), processorClass);
        }
        
        return context.value();
    }
}
