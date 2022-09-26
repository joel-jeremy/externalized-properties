package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.StripEmptyValues;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Supports conversion of values to a {@link Set} instance.
 *
 * @apiNote By default, this uses ',' as default delimiter when splitting resolved property values.
 *     This can overriden by annotating the proxy interface method with {@link Delimiter} annotation
 *     in which case the {@link Delimiter#value()} attribute will be used as the delimiter.
 * @apiNote If stripping of empty values from the array is desired, the proxy interface method can
 *     be annotated with the {@link StripEmptyValues} annotation.
 */
public class SetConverter implements Converter<Set<?>> {
  private final SetFactory setFactory;
  /** Internal array converter. */
  private final ArrayConverter arrayConverter = new ArrayConverter();

  /**
   * Default constructor. Instances constructed via this constructor will use {@link LinkedHashSet}
   * as {@link Set} implementation.
   */
  public SetConverter() {
    // Prevent hashmap resizing.
    // 0.75 is HashMap's default load factor.
    this(size -> new LinkedHashSet<>((int) (size / 0.75f) + 1));
  }

  /**
   * Constructor.
   *
   * @param setFactory The {@link Set} factory. This must return a mutable {@link Set} instance
   *     (optionally with given the capacity). This function must not return null.
   */
  public SetConverter(SetFactory setFactory) {
    this.setFactory = requireNonNull(setFactory, "setFactory");
  }

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return Set.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Set<?>> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    if (valueToConvert.isEmpty()) {
      return ConversionResult.of(newSet(0));
    }

    Type targetArrayType = toTargetArrayType(targetType);

    Object[] array = arrayConverter.convert(context, valueToConvert, targetArrayType).value();

    return ConversionResult.of(newSet(array));
  }

  private Set<Object> newSet(int capacity) {
    Set<Object> set = setFactory.newSet(capacity);
    if (set == null || !set.isEmpty()) {
      throw new IllegalStateException(
          "Set factory implementation must not return null or a populated set.");
    }
    return set;
  }

  private Set<Object> newSet(Object[] values) {
    Set<Object> set = newSet(values.length);
    Collections.addAll(set, values);
    return set;
  }

  /**
   * Convert target type to an array type such that:
   *
   * <ul>
   *   <li>{@code Set<String>} becomes {@code String[]}
   *   <li>{@code Set<Integer>} becomes {@code Integer[]}
   *   <li>{@code Set<Optional<Integer>>} becomes {@code Optional<Integer>[]}
   * </ul>
   *
   * @param targetType The target type.
   * @return The array target type to pass to {@link ArrayConverter} when requesting to convert to
   *     an array.
   */
  private static Type toTargetArrayType(Type targetType) {
    Type[] genericTypeParams = TypeUtilities.getTypeParameters(targetType);

    // Assume as Set<String> when target type has no generic type parameters.
    final Type targetSetType;
    if (genericTypeParams.length > 0) {
      targetSetType = genericTypeParams[0];
    } else {
      targetSetType = String.class;
    }

    return TypeUtilities.getArrayType(targetSetType);
  }

  /** Set factory. */
  public static interface SetFactory {
    /**
     * Create a new mutable {@link Set} instance (optionally with given the capacity). This function
     * must not return null and must only return a set with no elements. Otherwise, an exception
     * will be thrown.
     *
     * @param capacity The requested capacity of the {@link Set}.
     * @return A new mutable {@link Set} instance (optionally with given the capacity).
     */
    Set<Object> newSet(int capacity);
  }
}
