package io.github.joeljeremy.externalizedproperties.core.internal.processing;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Processor;
import io.github.joeljeremy.externalizedproperties.core.processing.ProcessWith;
import io.github.joeljeremy.externalizedproperties.core.processing.ProcessingException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The root {@link Processor}. All requests to process properties are routed through this processor
 * and delegated to the registered {@link Processor}s
 */
public class RootProcessor implements Processor {

  private final ProcessorByAnnotationType processorByAnnotationType;

  /**
   * Constructor.
   *
   * @param processors The collection of {@link Processor}s to handle the actual processing.
   */
  public RootProcessor(Processor... processors) {
    this(Arrays.asList(requireNonNull(processors, "processors")));
  }

  /**
   * Constructor.
   *
   * @param processors The collection of {@link Processor}s to handle the actual processing.
   */
  public RootProcessor(Collection<Processor> processors) {
    this.processorByAnnotationType =
        new ProcessorByAnnotationType(requireNonNull(processors, "processors"));
  }

  /** {@inheritDoc} */
  @Override
  public String process(InvocationContext context, String valueToProcess) {
    String value = valueToProcess;
    for (Annotation annotation : context.method().annotations()) {
      Processor processor = processorByAnnotationType.get(annotation.annotationType());
      if (processor == null) {
        // Annotation is not a processor annotation i.e.
        // not annotated with @ProcessWith.
        continue;
      }
      value = processor.process(context, value);
    }
    return value;
  }

  /**
   * Maps annotation type with processor instance based on the {@link ProcessWith} meta annotation.
   */
  private static class ProcessorByAnnotationType extends ClassValue<Processor> {
    private final Collection<Processor> registeredProcessors;

    /**
     * Constructor.
     *
     * @param registeredProcessors The registered {@link ProcessorProvider} instances.
     */
    ProcessorByAnnotationType(Collection<Processor> registeredProcessors) {
      this.registeredProcessors = registeredProcessors;
    }

    /**
     * This method will return a processor instance based on the specified proxy method annotation
     * type. If the annotation is not annotated with {@link ProcessWith}, {@code null} will be
     * returned. If the annotation is annotated with {@link ProcessWith}, but no registered {@link
     * Processor} instance of the specified type can be found, an exception will be thrown.
     *
     * @param annotationType The proxy method annotation type.
     * @return The {@link Processor} instance, or {@code null} if the annotation type is not
     *     annotated with {@link ProcessWith} annotation. An exception will be thrown if the
     *     annotation type is annotated with {@link ProcessWith} but no registered {@link Processor}
     *     of the specified type can be found.
     */
    @Override
    protected @Nullable Processor computeValue(Class<?> annotationType) {
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
          String.format(
              "No processor registered for required processor class: %s. "
                  + "Please make sure the processor is registered when building %s.",
              processWith.value().getName(), ExternalizedProperties.class.getSimpleName()));
    }
  }
}
