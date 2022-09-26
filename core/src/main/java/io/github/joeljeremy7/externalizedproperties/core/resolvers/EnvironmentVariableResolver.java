package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import java.util.Optional;

/**
 * A {@link MapResolver} extension which resolves requested properties from environment variables.
 */
public class EnvironmentVariableResolver extends MapResolver {
  private static final char DOT = '.';
  private static final char DASH = '-';
  private static final char UNDERSCORE = '_';

  /** Constructor. */
  public EnvironmentVariableResolver() {
    // For any unresolved property, try to resolve again from env var
    // in case new env vars were added after this resolver was constructed.
    super(System.getenv(), System::getenv);
  }

  /**
   * {@inheritDoc}
   *
   * @implNote This resolver will format the property name to environment variable format such that
   *     if the property name is {@code java.home}, it will be converted to {@code JAVA_HOME} and
   *     attempt to resolve with that formatted name.
   */
  @Override
  public Optional<String> resolve(InvocationContext context, String propertyName) {
    // Format to env var format
    // i.e. my.awesome.property-name -> MY_AWESOME_PROPERTY_NAME
    return super.resolve(context, format(propertyName));
  }

  /**
   * Format property name to environment variables naming convention. Such that:
   *
   * <ul>
   *   <li>{@code path} will be formatted to {@code PATH}
   *   <li>{@code java.version} will be formatted to {@code JAVA_VERSION}
   *   <li>{@code java-home} will be formatted to {@code JAVA_HOME}
   * </ul>
   *
   * @param propertyName The property name to format.
   * @return The formatted property name.
   */
  private static String format(String propertyName) {
    // Avoid String allocations.
    char[] propertyNameChars = propertyName.toCharArray();
    for (int currentIndex = 0; currentIndex < propertyNameChars.length; currentIndex++) {
      char currentChar = propertyNameChars[currentIndex];

      if (currentChar == DOT || currentChar == DASH) {
        propertyNameChars[currentIndex] = UNDERSCORE;
        continue;
      }

      propertyNameChars[currentIndex] = Character.toUpperCase(currentChar);
    }
    return new String(propertyNameChars);
  }
}
