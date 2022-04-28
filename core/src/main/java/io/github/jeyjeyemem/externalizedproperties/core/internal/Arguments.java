package io.github.jeyjeyemem.externalizedproperties.core.internal;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * Arguments-related utility methods.
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
    public static <T> T requireNonNull(@Nullable T arg, String argName) {
        if (arg == null) {
            throw new IllegalArgumentException(argName + " must not be null.");
        }
        return arg;
    }

    /**
     * Require argument to not be {@code null} or an empty {@link String}.
     * 
     * @param arg The {@link String} argument.
     * @param argName The name of the {@link String} argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The {@link String} argument.
     */
    public static String requireNonNullOrEmptyString(@Nullable String arg, String argName) {
        if (arg == null || "".equals(arg)) {
            throw new IllegalArgumentException(argName + " must not be null or empty.");
        }
        return arg;
    }

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
            @Nullable Collection<T> arg, 
            String argName
    ) {
        if (arg == null || arg.isEmpty()) {
            throw new IllegalArgumentException(argName + " must not be null or empty.");
        }
        return arg;
    }

    /**
     * Require argument to not be {@code null} or an empty {@link Collection}.
     * 
     * @param <T> The type of the collection argument.
     * @param arg The {@link Collection} argument.
     * @param argName The name of the {@link Collection} argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The {@link Collection} argument.
     */
    public static <T> T[] requireNonNullOrEmptyArray(
            @Nullable T[] arg, 
            String argName
    ) {
        if (arg == null || arg.length == 0) {
            throw new IllegalArgumentException(argName + " must not be null or empty.");
        }
        return arg;
    }
}
