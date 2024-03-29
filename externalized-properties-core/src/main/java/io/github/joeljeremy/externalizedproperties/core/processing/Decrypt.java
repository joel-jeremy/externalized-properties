package io.github.joeljeremy.externalizedproperties.core.processing;

import io.github.joeljeremy.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Processor annotation to apply decryption to the property. Property must be in Base64 format. */
@ProcessWith(DecryptProcessor.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Decrypt {
  /**
   * The name of the {@link Decryptor} instance to do the decryption.
   *
   * <p>Built-in decryptors prefer to use the algorithms used as decryptor names but it is not a
   * requirement. Arbitrary decryptor names may be used, if desired.
   *
   * @return The name of the {@link Decryptor} instance to do the decryption.
   */
  String value();
}
