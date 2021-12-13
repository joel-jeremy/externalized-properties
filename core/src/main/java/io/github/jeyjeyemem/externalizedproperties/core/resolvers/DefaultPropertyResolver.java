package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import java.util.Arrays;

/**
 * Default property converter which delegates to the following resolvers (in order):
 * <ol>
 *  <li>{@link SystemPropertyResolver}</li>
 *  <li>{@link EnvironmentPropertyResolver}</li>
 * </ol>
 */
public class DefaultPropertyResolver extends CompositePropertyResolver {
    /**
     * Construct a property converter which delegates to the following resolvers 
     * (in order):
     * <ol>
     *  <li>{@link SystemPropertyResolver}</li>
     *  <li>{@link EnvironmentPropertyResolver}</li>
     * </ol>
     */
    public DefaultPropertyResolver() {
        super(Arrays.asList(
            new SystemPropertyResolver(),
            new EnvironmentPropertyResolver()
        ));
    }
}