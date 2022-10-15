package io.github.joeljeremy.externalizedproperties.core.testentities;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Asserts {
  public static <T> void assertUnmodifiableCollection(
      Collection<T> collectionToVerify, Supplier<T> itemSupplier) {
    T item = itemSupplier.get();
    Collection<T> itemCollection = Collections.singletonList(item);

    assertThrows(UnsupportedOperationException.class, () -> collectionToVerify.add(item));

    assertThrows(UnsupportedOperationException.class, () -> collectionToVerify.remove(item));

    assertThrows(
        UnsupportedOperationException.class, () -> collectionToVerify.addAll(itemCollection));

    assertThrows(UnsupportedOperationException.class, () -> collectionToVerify.clear());

    assertThrows(
        UnsupportedOperationException.class, () -> collectionToVerify.removeAll(itemCollection));

    assertThrows(UnsupportedOperationException.class, () -> collectionToVerify.removeIf(r -> true));

    assertThrows(
        UnsupportedOperationException.class, () -> collectionToVerify.retainAll(itemCollection));

    Iterator<T> iterator = collectionToVerify.iterator();
    assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
  }

  public static <K, V> void assertUnmodifiableMap(
      Map<K, V> mapToVerify, Supplier<K> keySupplier, Function<K, V> valueFactory) {
    K key = keySupplier.get();
    V value = valueFactory.apply(key);

    assertThrows(
        UnsupportedOperationException.class,
        () -> mapToVerify.compute(key, (k, v) -> valueFactory.apply(k)));

    assertThrows(
        UnsupportedOperationException.class,
        () -> mapToVerify.computeIfAbsent(key, k -> valueFactory.apply(k)));

    assertThrows(
        UnsupportedOperationException.class,
        () -> mapToVerify.computeIfPresent(key, (k, v) -> valueFactory.apply(k)));

    assertThrows(
        UnsupportedOperationException.class, () -> mapToVerify.merge(key, value, (ov, nv) -> nv));

    assertThrows(UnsupportedOperationException.class, () -> mapToVerify.put(key, value));

    Map<K, V> map = Collections.singletonMap(key, value);
    assertThrows(UnsupportedOperationException.class, () -> mapToVerify.putAll(map));

    assertThrows(UnsupportedOperationException.class, () -> mapToVerify.putIfAbsent(key, value));

    assertThrows(UnsupportedOperationException.class, () -> mapToVerify.remove(key));

    assertThrows(UnsupportedOperationException.class, () -> mapToVerify.remove(key, value));

    assertThrows(UnsupportedOperationException.class, () -> mapToVerify.replace(key, value));

    assertThrows(UnsupportedOperationException.class, () -> mapToVerify.replace(key, value, value));

    assertThrows(
        UnsupportedOperationException.class,
        () -> mapToVerify.replaceAll((k, v) -> valueFactory.apply(k)));

    assertThrows(UnsupportedOperationException.class, () -> mapToVerify.clear());
  }
}
