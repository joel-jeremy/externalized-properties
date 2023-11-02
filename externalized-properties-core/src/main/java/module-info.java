module io.github.joeljeremy.externalizedproperties.core {
  exports io.github.joeljeremy.externalizedproperties.core;
  exports io.github.joeljeremy.externalizedproperties.core.conversion;
  exports io.github.joeljeremy.externalizedproperties.core.conversion.converters;
  exports io.github.joeljeremy.externalizedproperties.core.processing;
  exports io.github.joeljeremy.externalizedproperties.core.processing.processors;
  exports io.github.joeljeremy.externalizedproperties.core.resolvers;
  exports io.github.joeljeremy.externalizedproperties.core.variableexpansion;
  exports io.github.joeljeremy.externalizedproperties.core.internal to
      io.github.joeljeremy.externalizedproperties.git;

  uses io.github.joeljeremy.externalizedproperties.core.Resolver;

  requires java.logging;
  requires static org.checkerframework.checker.qual;
}
