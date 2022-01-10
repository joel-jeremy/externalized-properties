package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertiesException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyCollection;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * A {@link Resolver} implementation which resolves requested properties 
 * from classpath resources. 
 * 
 * @apiNote The resolution of properties will depend upon the prefix set 
 * in the property name. If a property name is prefixed by {@code classpath:},
 * the resolver will load the classpath resource using the property name as
 * resource name (stripping the {@code classpath:} prefix) and return its contents.
 * 
 * @implNote Classpath resources are located via 
 * {@link ClassLoader#getResource(String)} which means that the resolver
 * will locate and load resources based on the rules of the said API e.g. to
 * resolve a file on the classpath root, {@code classpath:app.properties} property
 * name may be used.
 */
public class ClasspathResolver implements Resolver {

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolve(String propertyName) {
        requireNonNullOrEmptyString(propertyName, "propertyName");
        
        if (!isClasspathProperty(propertyName)) {
            return Optional.empty();
        }

        try {
            return Optional.of(readClasspathResource(propertyName));
        } catch (Exception ex) {
            throw new UnresolvedPropertiesException(
                propertyName,
                "Failed to load property value from " + propertyName + ".",
                ex
            );
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResolverResult resolve(Collection<String> propertyNames) {
        requireNonNullOrEmptyCollection(propertyNames, "propertyNames");

        ResolverResult.Builder resultBuilder = ResolverResult.builder(propertyNames);
        for (String propertyName : propertyNames) {
            resolve(propertyName).ifPresent(
                value -> resultBuilder.add(propertyName, value)
            );
        }
        return resultBuilder.build();
    }

    private boolean isClasspathProperty(String propertyName) {
        return propertyName.startsWith("classpath:");
    }

    private String readClasspathResource(String propertyName) 
            throws URISyntaxException, IOException 
    {
        // Remove classpath: prefix.
        String resourceName = propertyName.substring("classpath:".length());
        URL classpathResource = getClass()
            .getClassLoader()
            .getResource(resourceName);
        if (classpathResource == null) {
            throw new UnresolvedPropertiesException(
                propertyName,
                "Classpath resource cannot be found: " + resourceName
            );
        }
        
        return readFileContents(Paths.get(classpathResource.toURI()));
    }

    private String readFileContents(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }
}
