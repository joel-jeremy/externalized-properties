package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

/**
 * A {@link MapPropertyResolver} extension which resolves requested properties 
 * from the Java system properties.
 */
public class SystemPropertyResolver extends PropertiesPropertyResolver {
    /**
     * Constructor.
     */
    public SystemPropertyResolver() {
        // For any unresolved property, try to resolve again from system properties
        // in case new system properties were added after this resolver was constructed.
        super(System.getProperties(), propName -> System.getProperty(propName));
    }
}
