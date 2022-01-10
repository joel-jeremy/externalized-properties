package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertiesException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyCollection;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * A {@link Resolver} implementation which resolves requested properties 
 * from files. 
 * 
 * @apiNote The resolution of properties will depend upon the prefix set 
 * in the property name. If a property name is prefixed by {@code file:},
 * the resolver will load the file using the property name as the file path
 * (stripping the {@code file:} prefix) and return its contents.
 * 
 * @implNote Files are located via {@link Paths#get(String, String...)} 
 * which means that the resolver will locate and load files based on the rules 
 * of the said API e.g. to resolve a file on the root directory, 
 * {@code file:/app.properties} property name may be used.
 */
public class FileResolver implements Resolver {

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolve(String propertyName) {
        requireNonNullOrEmptyString(propertyName, "propertyName");

        if (!isFileProperty(propertyName)) {
            return Optional.empty();
        }

        try {
            return Optional.of(readFile(propertyName));
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

    private boolean isFileProperty(String propertyName) {
        return propertyName.startsWith("file:");
    }

    private String readFile(String propertyName) throws IOException {
        // Remove file: prefix.
        String filePath = propertyName.substring("file:".length());
        return readFileContents(Paths.get(filePath));
    }

    private String readFileContents(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }
}
