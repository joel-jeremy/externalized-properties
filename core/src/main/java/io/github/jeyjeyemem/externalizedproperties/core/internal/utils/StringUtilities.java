package io.github.jeyjeyemem.externalizedproperties.core.internal.utils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String related utilities.
 */
public class StringUtilities {
    // Matches ${variable} pattern.
    public static final Pattern DEFAULT_VARIABLE_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    private StringUtilities() {}

    /**
     * Replace variables in the String which matches the default pattern: ${variable}
     * 
     * @param value The string whose variables are to be replaced.
     * @param valueProvider The variable values will be resolved via this provider function.
     * @return The processed String whose variables are replaced, if there were any that matched the pattern.
     */
    public static String replaceVariables(String value, Function<String, String> valueProvider) {
        return replaceVariables(value, DEFAULT_VARIABLE_PATTERN, valueProvider);
    }

    /**
     * Replace variables in the String which matches the provided pattern.
     * 
     * @param value The string whose variables are to be replaced.
     * @param variablePattern The pattern to match variables in the string.
     * @param valueProvider The variable values will be resolved via this provider function.
     * @return The processed String whose variables are replaced, if there were any that matched the pattern.
     */
    public static String replaceVariables(
            String value, 
            Pattern variablePattern,
            Function<String, String> valueProvider
    ) {
        StringBuilder output = new StringBuilder();

        Matcher matcher = variablePattern.matcher(value);
        int currentIndex = 0;
        while (matcher.find()) {
            // Get text before matched variable.
            String textBeforeMatchedVariable = 
                value.substring(currentIndex, matcher.start());
            
            // Resolve property from variable.
            String propertyNameVariable = matcher.group(1);
            
            if (isNullOrEmpty(propertyNameVariable)) {
                // e.g. for default variable pattern, an '${}' was matched.
                throw new IllegalStateException(
                    "Variable pattern matched with an null/empty capturing group value. " + 
                    "Variable pattern used in matching: " + variablePattern.pattern()
                );
            }

            String propertyValue = valueProvider.apply(propertyNameVariable);
            
            // Append text before matched variable and the replacement value.
            output.append(textBeforeMatchedVariable).append(propertyValue);
            
            // Move on to find next variable starting from the new currentIndex.
            currentIndex = matcher.end();
        }

        // Append any text after the variable if there are any.
        return output.append(value.substring(currentIndex)).toString();
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
