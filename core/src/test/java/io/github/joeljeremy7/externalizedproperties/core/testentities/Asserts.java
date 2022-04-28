package io.github.joeljeremy7.externalizedproperties.core.testentities;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Asserts {
    public static <T> void assertUnmodifiableCollection(
            Collection<T> collectionToVerify, 
            Supplier<T> itemSupplier
    ) {
        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.add(itemSupplier.get())
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.remove(itemSupplier.get())
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.addAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.clear()
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.removeAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.removeIf(r -> true)
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.retainAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.iterator().remove()
        );
    }

    public static <K, V> void assertUnmodifiableMap(
            Map<K, V> mapToVerify,
            Supplier<K> keySupplier,
            Function<K, V> valueFactory
    ) {
        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                mapToVerify.compute(key, (k,v) -> valueFactory.apply(k));
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                mapToVerify.computeIfAbsent(key, k -> valueFactory.apply(k));
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                mapToVerify.computeIfPresent(key, (k,v) -> valueFactory.apply(k));
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                V value = valueFactory.apply(key);
                mapToVerify.merge(key, value, (ov, nv) -> nv);
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                V value = valueFactory.apply(key);
                mapToVerify.put(key, value);
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                V value = valueFactory.apply(key);
                mapToVerify.putAll(Collections.singletonMap(key, value));
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                V value = valueFactory.apply(key);
                mapToVerify.putIfAbsent(key, value);
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                mapToVerify.remove(key);
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                V value = valueFactory.apply(key);
                mapToVerify.remove(key, value);
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                V value = valueFactory.apply(key);
                mapToVerify.replace(key, value);
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                K key = keySupplier.get();
                V value = valueFactory.apply(key);
                mapToVerify.replace(key, value, value);
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> {
                mapToVerify.replaceAll((k, v) -> valueFactory.apply(k));
            }
        );

        assertThrows(
            UnsupportedOperationException.class, 
            () -> mapToVerify.clear()
        );
    }
}
