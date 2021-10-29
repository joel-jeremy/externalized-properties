package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

/**
 * A {@link MapPropertyResolver} extension which resolves requested properties 
 * from environment variables.
 */
public class EnvironmentVariablePropertyResolver extends MapPropertyResolver {
    /**
     * Constructor.
     */
    public EnvironmentVariablePropertyResolver() {
        super(System.getenv(), propName -> System.getenv(propName));
    }
}
