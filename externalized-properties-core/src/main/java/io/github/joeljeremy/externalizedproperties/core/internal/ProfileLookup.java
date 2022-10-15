package io.github.joeljeremy.externalizedproperties.core.internal;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import java.util.Optional;

/** Proxy interface to lookup the active Externalized Properties profile. */
public interface ProfileLookup {
  /**
   * The active Externalized Properties profile.
   *
   * @return The active Externalized Properties profile. Otherwise, an empty {@link Optional}.
   */
  @ExternalizedProperty("externalizedproperties.profile")
  Optional<String> activeProfile();
}
