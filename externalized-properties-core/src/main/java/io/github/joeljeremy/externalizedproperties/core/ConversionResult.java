package io.github.joeljeremy.externalizedproperties.core;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The conversion result object containing the result of conversion.
 *
 * <p>This can either contain a result containing a converted value created via {@link
 * ConversionResult#of(Object)} factory method, or a skip result created via {@link
 * ConversionResult#skip()} factory method.
 *
 * <p>A result can be determined as a skip result by checking reference against {@link
 * ConversionResult#skip()} e.g. {@code ConversionResult.skip() == conversionResult} or {@code
 * ConversionResult.skip().equals(conversionResult)}.
 */
public class ConversionResult<T> {
  /** Singleton instance returned for every {@link ConversionResult#skip()} invocations. */
  private static final ConversionResult<?> SKIP = new ConversionResult<>();

  private final @Nullable T value;

  /** For {@link ConversionResult#SKIP}. */
  private ConversionResult() {
    this.value = null;
  }

  /**
   * Constructor.
   *
   * @param value The result value.
   */
  private ConversionResult(T value) {
    this.value = requireNonNull(value, "value");
  }

  /**
   * Create an instance of {@link ConversionResult} with the given value.
   *
   * @param <T> The type of the result value.
   * @param value The result value.
   * @return The conversion result containing the given value.
   */
  public static <T> ConversionResult<T> of(T value) {
    return new ConversionResult<>(value);
  }

  /**
   * Returns an instance of {@link ConversionResult} which indicates that the converter cannot
   * handle conversion to the target type and that the converter should skip/move to the next
   * registered converter in the conversion pipeline.
   *
   * @implSpec This always returns the same object reference which means that {@code
   *     ConversionResult.skip() == ConversionResult.skip()} should evaluate to {@code true}.
   * @param <T> The type of the result value.
   * @return An instance of {@link ConversionResult} which indicates that the converter cannot
   *     handle conversion to the target type and that the converter should skip/move to the next
   *     registered converter in the conversion pipeline.
   */
  public static <T> ConversionResult<T> skip() {
    @SuppressWarnings("unchecked")
    ConversionResult<T> skipResult = (ConversionResult<T>) SKIP;
    return skipResult;
  }

  /**
   * The conversion result value.
   *
   * @apiNote If invoked in an instance that was obtained from {@link ConversionResult#skip()}
   *     factory method, this method will throw an {@link IllegalStateException}. One can check if
   *     instance is a skip result by checking reference against {@link ConversionResult#skip()}
   *     e.g. {@code ConversionResult.skip() == conversionResult} or {@code
   *     ConversionResult.skip().equals(conversionResult)}.
   * @return The conversion result value. Otherwise, an {@link IllegalStateException} is thrown.
   * @throws IllegalStateException if instance was obtained from {@link ConversionResult#skip()}
   *     factory method and have no value.
   */
  public T value() {
    if (value == null) {
      throw new IllegalStateException("Conversion result does not contain a valid value.");
    }
    return value;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof ConversionResult<?>)) {
      return false;
    }

    ConversionResult<?> other = (ConversionResult<?>) obj;
    return Objects.equals(this.value, other.value);
  }
}
