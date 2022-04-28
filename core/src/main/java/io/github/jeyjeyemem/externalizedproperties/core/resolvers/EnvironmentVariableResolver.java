package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

import java.util.Optional;

/**
 * A {@link MapResolver} extension which resolves requested properties 
 * from environment variables.
 */
public class EnvironmentVariableResolver extends MapResolver {
    /**
     * Constructor.
     */
    public EnvironmentVariableResolver() {
        // For any unresolved property, try to resolve again from env var
        // in case new env vars were added after this resolver was constructed.
        super(System.getenv(), System::getenv);
    }

    /** 
     * {@inheritDoc} 
     * 
     * @implNote This resolver will format the property name to environment variable 
     * format such that if the property name is {@code java.home}, it will be converted 
     * to {@code JAVA_HOME} and attempt to resolve with that formatted name.
     * */
    @Override
    public Optional<String> resolve(ProxyMethod proxyMethod, String propertyName) {
        // Format to env var format 
        // i.e. my.awesome.property-name -> MY_AWESOME_PROPERTY_NAME
        return super.resolve(proxyMethod, format(propertyName));
    }

    /**
     * The {@link ResolverProvider} for {@link EnvironmentVariableResolver}.
     * 
     * @return The {@link ResolverProvider} for {@link EnvironmentVariableResolver}.
     */
    public static ResolverProvider<EnvironmentVariableResolver> provider() {
        return externalizedProperties -> new EnvironmentVariableResolver();
    }

    private static String format(String propertyName) {
        // Avoid String allocations.
        char[] propertyNameChars = propertyName.toCharArray();
        for (int i = 0; i < propertyNameChars.length; i++) {
            if (propertyNameChars[i] == '.') {
                propertyNameChars[i] = '_';
                continue;
            }

            if (propertyNameChars[i] == '-') {
                propertyNameChars[i] = '_';
                continue;
            }

            propertyNameChars[i] = Character.toUpperCase(propertyNameChars[i]);
        }
        return new String(propertyNameChars);
    }
}
