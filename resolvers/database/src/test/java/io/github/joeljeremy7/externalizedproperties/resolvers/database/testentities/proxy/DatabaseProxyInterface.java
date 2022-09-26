package io.github.joeljeremy7.externalizedproperties.resolvers.database.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

public interface DatabaseProxyInterface {
  @ExternalizedProperty("test.property.1")
  String property1();

  @ExternalizedProperty("test.property.2")
  String property2();

  @ExternalizedProperty("non.existent.property")
  String nonExistentProperty();
}
