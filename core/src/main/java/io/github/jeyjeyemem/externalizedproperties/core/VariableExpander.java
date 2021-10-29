package io.github.jeyjeyemem.externalizedproperties.core;

/**
 * The mechanism that allows expansion of variables in strings.
 */
public interface VariableExpander {
    /**
     * Expand any variables that is in the given string.
     * 
     * @param value The string value. This may contain variables  e.g. 
     * "${some.app.property}_property_name"  which will be expanded by this method.
     * @return The property name whose variables have been expanded.
     */
    String expandVariables(String value);
}
