package io.github.joeljeremy7.externalizedproperties.core.internal.processing;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.ProcessorProvider;
import io.github.joeljeremy7.externalizedproperties.core.processing.ProcessWith;
import io.github.joeljeremy7.externalizedproperties.core.processing.ProcessingException;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The root {@link Processor}. All requests to process properties are routed through this processor
 * and delegated to the registered {@link Processor}s
 */
public class RootProcessor implements Processor {

    private final ProcessorByAnnotationType processorByAnnotationType;

    /**
     * Constructor.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @param processorProviders The collection of {@link ProcessorProvider}s
     * to provide processors that handle the actual processing.
     */
    public RootProcessor(
            ExternalizedProperties externalizedProperties,
            ProcessorProvider<?>... processorProviders
    ) {
        this(
            externalizedProperties,
            Arrays.asList(requireNonNull(processorProviders, "processorProviders"))
        );
    }

    /**
     * Constructor.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @param processorProviders The collection of {@link ProcessorProvider}s
     * to provide processors that handle the actual processing.
     */
    public RootProcessor(
            ExternalizedProperties externalizedProperties,
            Collection<ProcessorProvider<?>> processorProviders
    ) {
        this.processorByAnnotationType = new ProcessorByAnnotationType(
            requireNonNull(externalizedProperties, "externalizedProperties"),
            requireNonNull(processorProviders, "processorProviders")
        );
    }

    /**
     * The {@link ProcessorProvider} for {@link RootProcessor}.
     * 
     * @param processorProviders The registered {@link ProcessorProvider}s which provide 
     * {@link Processor} instances.
     * @return The {@link ProcessorProvider} for {@link RootProcessor}.
     */
    public static ProcessorProvider<RootProcessor> provider(
            ProcessorProvider<?>... processorProviders
    ) {
        requireNonNull(processorProviders, "processorProviders");
        return externalizedProperties -> new RootProcessor(
            externalizedProperties, 
            processorProviders
        );
    }

    /**
     * The {@link ProcessorProvider} for {@link RootProcessor}.
     * 
     * @param processorProviders The registered {@link ProcessorProvider}s which provide 
     * {@link Processor} instances.
     * @return The {@link ProcessorProvider} for {@link RootProcessor}.
     */
    public static ProcessorProvider<RootProcessor> provider(
            Collection<ProcessorProvider<?>> processorProviders
    ) {
        requireNonNull(processorProviders, "processorProviders");
        return externalizedProperties -> new RootProcessor(
            externalizedProperties, 
            processorProviders
        );
    }

    /** {@inheritDoc} */
    @Override
    public String process(ProxyMethod proxyMethod, String valueToProcess) {
        requireNonNull(proxyMethod, "proxyMethod");
        requireNonNull(valueToProcess, "valueToProcess");

        String value = valueToProcess;
        for (Annotation annotation : proxyMethod.annotations()) {
            Processor processor = processorByAnnotationType.get(annotation.annotationType());
            if (processor == null) {
                // Annotation is not a processor annotation i.e. 
                // not annotated with @ProcessWith.
                continue;
            }
            value = processor.process(proxyMethod, value);
        }
        return value;
    }

    /**
     * Maps annotation type with processor instance based on the {@link ProcessWith}
     * meta annotation.
     */
    private static class ProcessorByAnnotationType extends ClassValue<Processor> {        
        private final ExternalizedProperties externalizedProperties;
        private final List<ProcessorProvider<?>> registeredProcessorProviders;

        /**
         * Constructor.
         * 
         * @param externalizedProperties The {@link ExternalizedProperties} instance.
         * @param registeredProcessorProviders The registered {@link ProcessorProvider} instances.
         */
        ProcessorByAnnotationType(
                ExternalizedProperties externalizedProperties,
                Collection<ProcessorProvider<?>> registeredProcessorProviders
        ) {
            this.externalizedProperties = externalizedProperties;
            this.registeredProcessorProviders = memoizeAll(registeredProcessorProviders);
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
        protected @Nullable Processor computeValue(Class<?> annotationType) {
            ProcessWith processWith = annotationType.getAnnotation(ProcessWith.class);
            if (processWith == null) {
                // Null if annotation type is not a processor annotation
                // i.e. not annotated with @ProcessWith.
                return null;
            }
                
            for (ProcessorProvider<?> processorProvider : registeredProcessorProviders) {
                Processor processor = processorProvider.get(externalizedProperties);
                if (Objects.equals(processor.getClass(), processWith.value())) {
                    return processor;
                }
            }

            throw new ProcessingException(String.format(
                "No processor registered for processor class: %s. " +
                "Please make sure the processor is registered when " + 
                "building %s.",
                processWith.value().getName(),
                ExternalizedProperties.class.getSimpleName()
            ));
        }

        private static List<ProcessorProvider<?>> memoizeAll(
                Collection<ProcessorProvider<?>> processorProviders
        ) {
            return processorProviders.stream()
                .map(ProcessorProvider::memoize)
                .collect(Collectors.toList());
        }
    }
}
