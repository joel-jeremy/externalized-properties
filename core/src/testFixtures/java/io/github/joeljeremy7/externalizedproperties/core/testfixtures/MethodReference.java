package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

/** Functional interface used in extracting methods with no arguments. */
public interface MethodReference<TProxyInterface, TReturn> {
  TReturn ref(TProxyInterface proxy);

  /** Functional interfaces used in extracting methods with one argument. */
  static interface WithOneArg<TProxyInterface, TArg1, TReturn> {
    TReturn ref(TProxyInterface proxy, TArg1 arg1);
  }

  /** Functional interfaces used in extracting methods with two arguments. */
  static interface WithTwoArgs<TProxyInterface, TArg1, TArg2, TReturn> {
    TReturn ref(TProxyInterface proxy, TArg1 arg1, TArg2 arg2);
  }

  /** Functional interfaces used in extracting methods with three arguments. */
  static interface WithThreeArgs<TProxyInterface, TArg1, TArg2, TArg3, TReturn> {
    TReturn ref(TProxyInterface proxy, TArg1 arg1, TArg2 arg2, TArg3 arg3);
  }
}
