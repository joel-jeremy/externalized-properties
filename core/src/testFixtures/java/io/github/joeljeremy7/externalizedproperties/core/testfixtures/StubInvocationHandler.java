package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/** A stub {@link InvocationHandler} implementation. */
public class StubInvocationHandler implements InvocationHandler {

  /** The default delegate which always returns the name of the invoked method. */
  public static final Function<Invocation, Object> DEFAULT_DELEGATE =
      StubInvocationHandler::returnMethodName;

  /** A delegate which always returns {@code null}. */
  public static final Function<Invocation, Object> NULL_DELEGATE = invocation -> null;

  /** A delegate which always throws. */
  public static final Function<Invocation, Object> THROWING_DELEGATE =
      invocation -> {
        throw new RuntimeException("Oops!");
      };

  private final List<Invocation> invocations = new ArrayList<>();
  private final Function<Invocation, Object> delegate;

  /** Constructor. */
  public StubInvocationHandler() {
    // Always return the name of the invoked method.
    this(StubInvocationHandler::returnMethodName);
  }

  /**
   * Constructor.
   *
   * @param delegate The delegate to use when handling invocations.
   */
  public StubInvocationHandler(Function<Invocation, Object> delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Invocation invocation = new Invocation(proxy, method, args);
    // Add for tracking.
    invocations.add(invocation);
    return delegate.apply(invocation);
  }

  /**
   * Get the list of handled invocations.
   *
   * @return The list of handled invocations.
   */
  public List<Invocation> invocations() {
    return Collections.unmodifiableList(invocations);
  }

  private static Object returnMethodName(Invocation invocation) {
    return invocation.method().getName();
  }

  /** Represents a method invocation. */
  public static class Invocation {
    private final UUID id = UUID.randomUUID();
    private final Object proxy;
    private final Method method;
    private final Object[] args;

    /**
     * Constructor.
     *
     * @param proxy The proxy.
     * @param method The invoked method.
     * @param args The method invocation arguments.
     */
    public Invocation(Object proxy, Method method, Object[] args) {
      this.proxy = proxy;
      this.method = method;
      this.args = args;
    }

    /**
     * The invocation ID.
     *
     * @return The invocation ID.
     */
    public UUID id() {
      return id;
    }

    /**
     * The proxy.
     *
     * @return The proxy.
     */
    public Object proxy() {
      return proxy;
    }

    /**
     * The invoked method.
     *
     * @return The invoked method.
     */
    public Method method() {
      return method;
    }

    /**
     * The method invocation arguments.
     *
     * @return The method invocation arguments.
     */
    public Object[] args() {
      return args;
    }
  }
}
