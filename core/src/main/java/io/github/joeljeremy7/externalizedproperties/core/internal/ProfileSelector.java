package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

import java.util.Optional;

/**
 * Proxy interface to query for the active Externalized Properties profile.
 */
public interface ProfileSelector {
    /**
     * The active Externalized Properties profile.
     * 
     * @return The active Externalized Properties profile. Otherwise, an
     * empty {@link Optional}.
     */
    @ExternalizedProperty("externalizedproperties.profile")
    Optional<String> activeProfile();
}
