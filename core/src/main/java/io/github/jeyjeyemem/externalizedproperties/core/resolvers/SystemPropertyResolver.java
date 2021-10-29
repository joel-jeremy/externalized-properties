package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

/**
 * A {@link MapPropertyResolver} extension which resolves requested properties 
 * from the Java system properties.
 */
public class SystemPropertyResolver extends MapPropertyResolver {
    /**
     * Constructor.
     */
    public SystemPropertyResolver() {
        super(System.getProperties(), propName -> System.getProperty(propName));
    }
}
