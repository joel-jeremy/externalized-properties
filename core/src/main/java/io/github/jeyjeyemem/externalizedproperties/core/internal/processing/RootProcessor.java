package io.github.jeyjeyemem.externalizedproperties.core.internal.processing;

import io.github.jeyjeyemem.externalizedproperties.core.ProcessingContext;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.processing.ProcessWith;
import io.github.jeyjeyemem.externalizedproperties.core.processing.ProcessingException;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The root {@link Processor} implementation.
 * This delegates to a configured collection of {@link Processor}s.
 */
public class RootProcessor implements Processor {

    private final ProcessorByAnnotationType processorByAnnotationType;

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
        this.processorByAnnotationType = new ProcessorByAnnotationType(
            requireNonNull(processors, "processors")
        );
    }

    /** {@inheritDoc} */
    @Override
    public String process(ProcessingContext context) {
        requireNonNull(context, "context");

        Annotation[] proxyMethodAnnotations = context.proxyMethod().annotations();
        for (Annotation annotation : proxyMethodAnnotations) {
            Processor processor = processorByAnnotationType.get(annotation.annotationType());
            if (processor == null) {
                // Annotation is not a processor annotation i.e. 
                // not annotated with @ProcessWith.
                continue;
            }
            String value = processor.process(context);
            context = context.with(value, processor.getClass());
        }
        
        return context.value();
    }

    private static class ProcessorByAnnotationType extends ClassValue<Processor> {

        private final Collection<Processor> registeredProcessors;

        /**
         * Constructor.
         * 
         * @param registeredProcessors The registered {@link Processor} instances.
         */
        public ProcessorByAnnotationType(Collection<Processor> registeredProcessors) {
            this.registeredProcessors = registeredProcessors;
        }

        /**
         * This method will return a processor instance based on the specified proxy method annotation type.
         * If the annotation is not annotated with {@link ProcessWith}, {@code null} will be returned.
         * If the annotation is annotated with {@link ProcessWith}, but no registered {@link Processor} 
         * instance of the specified type can be found, an exception will be thrown. 
         * 
         * @param annotationType The proxy method annotation type.
         * @return The {@link Processor} instance, or {@code null} if the annotation type is not
         * annotated with {@link ProcessWith} annotation. An exception will be thrown if the annotation
         * type is annotated with {@link ProcessWith} but no registered {@link Processor} of the specified
         * type can be found.
         */
        @Override
        protected Processor computeValue(Class<?> annotationType) {
            ProcessWith processWith = annotationType.getAnnotation(ProcessWith.class);
            if (processWith == null) {
                // Null if annotation type is not a processor annotation
                // i.e. not annotated with @ProcessWith.
                return null;
            }
                
            for (Processor processor : registeredProcessors) {
                if (Objects.equals(processor.getClass(), processWith.value())) {
                    return processor;
                }
            }
            throw new ProcessingException(
                "No processor registered for processor class: " + 
                processWith.value().getName() + ". Please make sure processor is " + 
                "registered while building ExternalizedProperties." 
            );
        }
    }
}
