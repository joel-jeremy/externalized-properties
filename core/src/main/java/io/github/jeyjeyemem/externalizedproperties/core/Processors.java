package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorClasses;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * API to hold an ordered list of processor classes that are to be applied to properties.
 */
public class Processors {
    /**
     * No processors to apply.
     */
    public static final Processors NONE = new Processors(Collections.emptyList());

    private final List<Class<? extends Processor>> processorClasses;

    private Processors(
            List<Class<? extends Processor>> processorClasses
    ) {
        this.processorClasses = requireNonNull(processorClasses, "processorClasses");
    }

    /**
     * Unmodifiable list of processor classes in the order they were added.
     * 
     * @return The unmodifiable list of processor classes in the order they were 
     * added.
     */
    public List<Class<? extends Processor>> list() {
        return Collections.unmodifiableList(processorClasses);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return processorClasses.toString();
    }

    /**
     * Create a {@link Processors} instance containing a single processor.
     * 
     * @param processorClass The processor class to add.
     * @return A {@link Processors} instance containing a single processor.
     */
    public static Processors of(
        Class<? extends Processor> processorClass
    ) {
        requireNonNull(processorClass, "processorClass");
        return of(Arrays.asList(processorClass));
    }

    /**
     * Create a {@link Processors} instance containing exactly two processors.
     * 
     * @param first The first processor class to add.
     * @param second The second processor class to add.
     * @return A {@link Processors} instance containing exactly two processors.
     */
    public static Processors of(
        Class<? extends Processor> first,
        Class<? extends Processor> second
    ) {
        requireNonNull(first, "first");
        requireNonNull(second, "second");
        return of(Arrays.asList(first, second));
    }

    /**
     * Create a {@link Processors} instance containing exactly three processors.
     * 
     * @param first The first processor class to add.
     * @param second The second processor class to add.
     * @param third The third processor class to add.
     * @return A {@link Processors} instance containing exactly three processors.
     */
    public static Processors of(
        Class<? extends Processor> first,
        Class<? extends Processor> second,
        Class<? extends Processor> third
    ) {
        requireNonNull(first, "first");
        requireNonNull(second, "second");
        requireNonNull(third, "third");
        return of(Arrays.asList(first, second, third));
    }

    /**
     * Create a {@link Processors} instance containing exactly four processors.
     * 
     * @param first The first processor class to add.
     * @param second The second processor class to add.
     * @param third The third processor class to add.
     * @param fourth The fourth processor class to add.
     * @return A {@link Processors} instance containing exactly four processors.
     */
    public static Processors of(
        Class<? extends Processor> first,
        Class<? extends Processor> second,
        Class<? extends Processor> third,
        Class<? extends Processor> fourth
    ) {
        requireNonNull(first, "first");
        requireNonNull(second, "second");
        requireNonNull(third, "third");
        requireNonNull(fourth, "fourth");
        return of(Arrays.asList(first, second, third, fourth));
    }

    /**
     * Create a {@link Processors} instance containing exactly five processors.
     * 
     * @param first The first processor class to add.
     * @param second The second processor class to add.
     * @param third The third processor class to add.
     * @param fourth The fourth processor class to add.
     * @param fifth The fifth processor class to add.
     * @return A {@link Processors} instance containing exactly five processors.
     */
    public static Processors of(
        Class<? extends Processor> first,
        Class<? extends Processor> second,
        Class<? extends Processor> third,
        Class<? extends Processor> fourth,
        Class<? extends Processor> fifth
    ) {
        requireNonNull(first, "first");
        requireNonNull(second, "second");
        requireNonNull(third, "third");
        requireNonNull(fourth, "fourth");
        requireNonNull(fifth, "fifth");
        return of(Arrays.asList(first, second, third, fourth, fifth));
    }

    /**
     * Create a {@link Processors} instance from a {@link ProcessorClasses} annotation 
     * instance.
     * 
     * @param processorClasses The {@link ProcessorClasses} annotation instance.
     * @return A {@link Processors} instance containing an ordered list of processors
     * that are to be applied to properties.
     */
    public static Processors of(ProcessorClasses processorClasses) {
        requireNonNull(processorClasses, "processorClasses");
        return of(Arrays.asList(processorClasses.value()));
    }

    /**
     * Create a {@link Processors} instance containing an ordered list of processors.
     * 
     * @param processorClasses The processor classes to add. The order of processors
     * in the list shall be maintained.
     * @return A {@link Processors} instance containing an ordered list of processors
     * that are to be applied to properties.
     */
    public static Processors of(
        List<Class<? extends Processor>> processorClasses
    ) {
        requireNonNull(processorClasses, "processorClasses");
        if (processorClasses.isEmpty()) {
            return NONE;
        }
        return new Processors(processorClasses);
    }
}
