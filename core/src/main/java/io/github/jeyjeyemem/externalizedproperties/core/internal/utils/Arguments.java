package io.github.jeyjeyemem.externalizedproperties.core.internal.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Argument utilities.
 */
public class Arguments {
    private Arguments(){}

    /**
     * Require argument to not be {@code null}.
     * 
     * @param <T> The type of the argument.
     * @param arg The argument.
     * @param argName The name of the argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The argument.
     */
    public static <T> T requireNonNull(T arg, String argName) {
        if (isNullOrEmpty(argName))
            throw new IllegalArgumentException("argName must not be null or empty.");

        return require(arg, Objects::nonNull, argName + " must not be null.");
    }

    /**
     * Require argument to satisfy the requirement.
     * 
     * @param <T> The type of the argument.
     * @param arg The argument.
     * @param requirement The requirement predicate.
     * @param exceptionMessage The exception message to be used in building the 
     * {@link IllegalArgumentException} if the argument failed validation.
     * @return The argument.
     */
    public static <T> T require(T arg, Predicate<T> requirement, String exceptionMessage) {
        if (requirement == null)
            throw new IllegalArgumentException("requirement must not be null.");

        if (isNullOrEmpty(exceptionMessage))
            throw new IllegalArgumentException("exceptionMessage must not be null or empty.");

        if (!requirement.test(arg)) {
            throw new IllegalArgumentException(exceptionMessage);
        }

        return arg;
    }

    /**
     * String argument utilities.
     */
    public static class Strings {
        private Strings(){}

        /**
         * Require argument to not be {@code null} or an empty {@link String}.
         * 
         * @param arg The {@link String} argument.
         * @param argName The name of the {@link String} argument to be used in building the 
         * {@link IllegalArgumentException} message if the argument failed validation.
         * @return The {@link String} argument.
         */
        public static String requireNonNullOrEmptyString(String arg, String argName) {
            if (isNullOrEmpty(argName))
                throw new IllegalArgumentException("argName must not be null or empty.");

            return require(
                arg, 
                a -> a != null && !a.isEmpty(), 
                argName + " must not be null or empty."
            );
        }
    }

    /**
     * Collection argument utilities.
     */
    public static class Collections {
        private Collections(){}

        /**
         * Require argument to not be {@code null} or an empty {@link Collection}.
         * 
         * @param <T> The type of the collection argument.
         * @param arg The {@link Collection} argument.
         * @param argName The name of the {@link Collection} argument to be used in building the 
         * {@link IllegalArgumentException} message if the argument failed validation.
         * @return The {@link Collection} argument.
         */
        public static <T> Collection<T> requireNonNullOrEmptyCollection(
                Collection<T> arg, String argName
        ) {
            if (isNullOrEmpty(argName))
                throw new IllegalArgumentException("argName must not be null or empty.");

            return require(
                arg, 
                a -> a != null && !a.isEmpty(), 
                argName + " must not be null or empty."
            );
        }
    }

    private static boolean isNullOrEmpty(String string) {
        return string == null || "".equals(string);
    }
}
