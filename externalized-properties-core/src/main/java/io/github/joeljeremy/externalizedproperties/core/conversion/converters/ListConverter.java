package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.TypeUtilities;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Supports conversion of values to a {@link List} or {@link Collection} instance.
 *
 * @apiNote This converter follows the same rules used by {@link ArrayConverter} when
 *     parsing/splitting resolved property values.
 */
public class ListConverter implements Converter<List<?>> {
  private final ListFactory listFactory;

  /** Internal array converter. */
  private final ArrayConverter arrayConverter = new ArrayConverter();

  /**
   * Default constructor. Instances constructed via this constructor will use {@link ArrayList} as
   * {@link List} or {@link Collection} implementation.
   */
  public ListConverter() {
    this(ArrayList::new);
  }

  /**
   * Constructor.
   *
   * @param listFactory The {@link List} factory. This must return a {@link List} instance
   *     (optionally with given the capacity). This function must not return null.
   */
  public ListConverter(ListFactory listFactory) {
    this.listFactory = requireNonNull(listFactory, "listFactory");
  }

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return List.class.equals(targetType) || Collection.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<List<?>> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    if (valueToConvert.isEmpty()) {
      return ConversionResult.of(newList(0));
    }

    Type targetArrayType = toTargetArrayType(targetType);

    Object[] array = arrayConverter.convert(context, valueToConvert, targetArrayType).value();

    return ConversionResult.of(newList(array));
  }

  private List<Object> newList(int capacity) {
    List<Object> list = listFactory.newList(capacity);
    if (list == null || !list.isEmpty()) {
      throw new IllegalStateException(
          "List factory implementation must not return null or a populated list.");
    }
    return list;
  }

  private List<Object> newList(Object[] values) {
    List<Object> list = newList(values.length);
    Collections.addAll(list, values);
    return list;
  }

  /**
   * Convert target type to an array type such that:
   *
   * <ul>
   *   <li>{@code List<String>} becomes {@code String[]}
   *   <li>{@code List<Integer>} becomes {@code Integer[]}
   *   <li>{@code List<Optional<Integer>>} becomes {@code Optional<Integer>[]}
   * </ul>
   *
   * @param targetType The target type.
   * @return The array target type to pass to {@link ArrayConverter} when requesting to convert to
   *     an array.
   */
  private static Type toTargetArrayType(Type targetType) {
    Type[] genericTypeParams = TypeUtilities.getTypeParameters(targetType);

    // Assume as List<String> when target type has no generic type parameters.
    final Type targetListType;
    if (genericTypeParams.length > 0) {
      targetListType = genericTypeParams[0];
    } else {
      targetListType = String.class;
    }

    return TypeUtilities.getArrayType(targetListType);
  }

  /** List factory. */
  public static interface ListFactory {
    /**
     * Create a new mutable {@link List} instance (optionally with given the capacity). This
     * function must not return null and must only return a list with no elements. Otherwise, an
     * exception will be thrown.
     *
     * @param capacity The requested capacity of the {@link List}.
     * @return A new mutable {@link List} instance (optionally with given the capacity).
     */
    List<Object> newList(int capacity);
  }
}
