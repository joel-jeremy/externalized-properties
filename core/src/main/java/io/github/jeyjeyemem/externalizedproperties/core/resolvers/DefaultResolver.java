package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;

import java.util.Arrays;

/**
 * Default property converter which delegates to the following resolvers (in order):
 * <ol>
 *  <li>{@link SystemPropertyResolver}</li>
 *  <li>{@link EnvironmentVariableResolver}</li>
 * </ol>
 */
public class DefaultResolver extends CompositeResolver {
    /**
     * Construct a property converter which delegates to the following resolvers 
     * (in order):
     * <ol>
     *  <li>{@link SystemPropertyResolver}</li>
     *  <li>{@link EnvironmentVariableResolver}</li>
     * </ol>
     */
    public DefaultResolver() {
        super(Arrays.asList(
            new SystemPropertyResolver(),
            new EnvironmentVariableResolver()
        ));
    }

    /**
     * The {@link ResolverProvider} for {@link DefaultResolver}.
     * 
     * @return The {@link ResolverProvider} for {@link DefaultResolver}.
     */
    public static ResolverProvider<DefaultResolver> provider() {
        return externalizedProperties -> new DefaultResolver();
    }
}