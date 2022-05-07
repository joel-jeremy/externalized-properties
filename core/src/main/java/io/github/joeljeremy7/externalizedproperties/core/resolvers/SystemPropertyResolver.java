package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;

/**
 * A {@link MapResolver} extension which resolves requested properties 
 * from the Java system properties.
 */
public class SystemPropertyResolver extends PropertiesResolver {
    /**
     * Constructor.
     */
    public SystemPropertyResolver() {
        // For any unresolved property, try to resolve again from system properties
        // in case new system properties were added after this resolver was constructed.
        super(System.getProperties(), System::getProperty);
    }

    /**
     * The {@link ResolverProvider} for {@link SystemPropertyResolver}.
     * 
     * @return The {@link ResolverProvider} for {@link SystemPropertyResolver}.
     */
    public static ResolverProvider<SystemPropertyResolver> provider() {
        return externalizedProperties -> new SystemPropertyResolver();
    }
}
