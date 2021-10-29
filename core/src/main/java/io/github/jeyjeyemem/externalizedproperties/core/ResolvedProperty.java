package io.github.jeyjeyemem.externalizedproperties.core;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.Strings.requireNonNullOrEmptyString;

/**
 * The resolved property.
 */
public class ResolvedProperty {
    private final String name;
    private final String value;

    private ResolvedProperty(
            String name,
            String value
    ) {
        this.name = requireNonNullOrEmptyString(name, "name");
        this.value = requireNonNull(value, "value");
    }

    /**
     * Name of the resolved property.
     * 
     * @return The name of the resolved property. This will never be null or empty.
     */
    public String name() {
        return name;
    }

    /**
     * Value of the resolved property.
     * 
     * @return The value of the resolved property. This will never be null.
     */
    public String value() {
        return value;
    }

    /**
     * Returns a new {@link ResolvedProperty} instance with the updated value.
     * Null will not be accepted as value.
     * 
     * @param value The updated value.
     * @return A new {@link ResolvedProperty} instance with the updated value.
     */
    public ResolvedProperty withValue(String value) {
        return with(this.name, value);
    }

    /**
     * Returns a new {@link ResolvedProperty} instance with the given name and value.
     * Null will not be accepted for both name and value. An empty value is allowed.
     * 
     * @param name The property name.
     * @param value The property value.
     * @return The {@link ResolvedProperty} instance with the given name and value.
     */
    public static ResolvedProperty with(String name, String value) {
        return new ResolvedProperty(name, value);
    }
}
