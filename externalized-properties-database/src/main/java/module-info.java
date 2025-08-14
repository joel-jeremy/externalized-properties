/** Database-backed Externalized Properties resolver. */
module io.github.joeljeremy.externalizedproperties.database {
  exports io.github.joeljeremy.externalizedproperties.database;
  exports io.github.joeljeremy.externalizedproperties.database.queryexecutors;

  requires transitive io.github.joeljeremy.externalizedproperties.core;
  requires transitive java.sql;
  requires static org.jspecify;
}
