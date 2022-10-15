package io.github.joeljeremy.externalizedproperties.core;

public interface VariableExpansionProxyInterface {
  @ExternalizedProperty("${test}")
  String test();
}
