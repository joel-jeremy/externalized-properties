package io.github.joeljeremy7.externalizedproperties.core.internal;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Thread factory which create daemon threads. */
public class DaemonThreadFactory implements ThreadFactory {
  private final AtomicInteger threadCount = new AtomicInteger(1);
  private final String threadNamePrefix;

  /**
   * Constructor.
   *
   * @param threadNamePrefix Prefix to the thread name.
   */
  public DaemonThreadFactory(String threadNamePrefix) {
    this.threadNamePrefix = requireNonNull(threadNamePrefix, "threadNamePrefix");
  }

  /**
   * Creates a new daemon thread.
   *
   * @param runnable The runnable to run.
   * @return The new daemon thread.
   */
  @Override
  public Thread newThread(Runnable runnable) {
    Thread thread =
        new Thread(runnable, threadNamePrefix + "-daemon-thread-" + threadCount.getAndIncrement());

    thread.setDaemon(true);

    return thread;
  }
}
