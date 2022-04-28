package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

public interface ProxyMethodReference<TProxyInterface, TReturn> {
    TReturn ref(TProxyInterface proxy);

    static interface WithOneArg<TProxyInterface, TArg1, TReturn> {
        TReturn ref(TProxyInterface proxy, TArg1 arg1);
    }

    static interface WithTwoArgs<TProxyInterface, TArg1, TArg2, TReturn> {
        TReturn ref(
            TProxyInterface proxy, 
            TArg1 arg1, 
            TArg2 arg2
        );
    }

    static interface WithThreeArgs<TProxyInterface, TArg1, TArg2, TArg3, TReturn> {
        TReturn ref(
            TProxyInterface proxy, 
            TArg1 arg1, 
            TArg2 arg2, 
            TArg3 arg3
        );
    }
}