/** Git-backed Externalized Properties resolver. */
module io.github.joeljeremy.externalizedproperties.git {
  exports io.github.joeljeremy.externalizedproperties.git;

  requires transitive org.eclipse.jgit;
  requires transitive io.github.joeljeremy.externalizedproperties.core;
  requires static org.jspecify;
}
