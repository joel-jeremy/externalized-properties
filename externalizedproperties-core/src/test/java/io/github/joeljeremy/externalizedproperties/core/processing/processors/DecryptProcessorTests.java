package io.github.joeljeremy.externalizedproperties.core.processing.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Processor;
import io.github.joeljeremy.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy.externalizedproperties.core.processing.ProcessingException;
import io.github.joeljeremy.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import io.github.joeljeremy.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DecryptProcessorTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  // AES constants.
  private static final String AES_ALGORITHM = EncryptionUtils.AES_ALGORITHM;
  private static final String AES_GCM_ALGORITHM = EncryptionUtils.AES_GCM_ALGORITHM;
  private static final AlgorithmParameterSpec GCM_PARAMETER_SPEC =
      EncryptionUtils.DEFAULT_GCM_PARAMETER_SPEC;
  private static final AlgorithmParameters GCM_PARAMETERS = EncryptionUtils.DEFAULT_GCM_PARAMETERS;
  private static final SecretKey AES_SECRET_KEY = EncryptionUtils.DEFAULT_AES_SECRET_KEY;
  private static final Decryptor AES_GCM_DECRYPTOR = createSymmetricDecryptor();

  // RSA constants.
  private static final String RSA_ALGORITHM = EncryptionUtils.RSA_ALGORITHM;
  private static final PrivateKey RSA_PRIVATE_KEY = EncryptionUtils.DEFAULT_RSA_PRIVATE_KEY;
  private static final Decryptor RSA_DECRYPTOR = createAsymmetricDecryptor();

  private static final String BOUNCY_CASTLE_PROVIDER_NAME = "BC";

  private static final SecureRandom STRONG_SECURE_RANDOM = getStrongSecureRandom();

  @BeforeAll
  static void setup() {
    // Add custom JCE provider.
    Security.addProvider(new BouncyCastleProvider());
  }

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when decryptors argument is null")
    void withVarArgsOverloadTest1() {
      assertThrows(IllegalArgumentException.class, () -> new DecryptProcessor((Decryptor[]) null));
    }

    @Test
    @DisplayName("should throw when decryptors argument is empty")
    void withVarArgsOverloadTest2() {
      assertThrows(IllegalArgumentException.class, () -> new DecryptProcessor(new Decryptor[0]));
    }

    @Test
    @DisplayName("should throw when decryptors argument is null")
    void withCollectionOverloadTest1() {
      assertThrows(
          IllegalArgumentException.class, () -> new DecryptProcessor((Collection<Decryptor>) null));
    }

    @Test
    @DisplayName("should throw when decryptors argument is empty")
    void withCollectionOverloadTest2() {
      List<Decryptor> empty = Collections.emptyList();

      assertThrows(IllegalArgumentException.class, () -> new DecryptProcessor(empty));
    }
  }

  @Nested
  class ProcessMethod {
    @Test
    @DisplayName("should throw when decryptor with specified name is not registered")
    void test1() {
      // AES decryptor is not registered.
      DecryptProcessor processor = processorToTest(RSA_DECRYPTOR);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::decryptAesGcm, externalizedProperties(processor));

      String plainText = "plain-text";
      String aesEncryptedBase64 = EncryptionUtils.encryptAesBase64(plainText);

      assertThrows(ProcessingException.class, () -> processor.process(context, aesEncryptedBase64));
    }

    @Test
    @DisplayName("should throw when proxy method is not annotated with @Decrypt")
    void test2() {
      DecryptProcessor processor = processorToTest(AES_GCM_DECRYPTOR);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::notAnnotatedWithDecrypt, externalizedProperties(processor));

      String plainText = "plain-text";
      String aesEncryptedBase64 = EncryptionUtils.encryptAesBase64(plainText);

      assertThrows(ProcessingException.class, () -> processor.process(context, aesEncryptedBase64));
    }

    @Test
    @DisplayName("should wrap exceptions when decrypting values")
    void test3() {
      DecryptProcessor processor = processorToTest(RSA_DECRYPTOR);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::decryptRsa, externalizedProperties(processor));

      String plainText = "plain-text";
      // Oops! This is encrypted with AES, not RSA.
      // Should throw when decrypting with RSA.
      String aesEncryptedBase64 = EncryptionUtils.encryptAesBase64(plainText);

      assertThrows(ProcessingException.class, () -> processor.process(context, aesEncryptedBase64));
    }

    @Test
    @DisplayName("should throw when property is not in Base64 format")
    void test4() {
      DecryptProcessor processor = processorToTest(RSA_DECRYPTOR);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::decryptRsa, externalizedProperties(processor));

      String plainText = "plain-text";
      byte[] encrypted = EncryptionUtils.encryptAes(plainText);

      // Encrypted bytes were not encoded in Base64.
      String notInBase64Format = new String(encrypted, StandardCharsets.UTF_8);

      assertThrows(
          IllegalArgumentException.class, () -> processor.process(context, notInBase64Format));
    }

    @Test
    @DisplayName("should throw when decryptor with the same names are registered")
    void test5() {
      assertThrows(
          IllegalArgumentException.class, () -> processorToTest(RSA_DECRYPTOR, RSA_DECRYPTOR));
    }

    @Test
    @DisplayName("should decrypt property using specified decryptor")
    void aesTest1() {
      DecryptProcessor processor = processorToTest(AES_GCM_DECRYPTOR, RSA_DECRYPTOR);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::decryptAesGcm, externalizedProperties(processor));

      String plainText = "plain-text";
      String aesEncryptedBase64 =
          EncryptionUtils.encryptAesBase64(
              plainText, AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
      String decrypted = processor.process(context, aesEncryptedBase64);

      assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("should decrypt property using specified decryptor")
    void rsaTest1() {
      DecryptProcessor processor = processorToTest(AES_GCM_DECRYPTOR, RSA_DECRYPTOR);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::decryptRsa, externalizedProperties(processor));

      String plainText = "plain-text";
      String rsaEncryptedBase64 = EncryptionUtils.encryptRsaBase64(plainText);
      String decrypted = processor.process(context, rsaEncryptedBase64);

      assertEquals(plainText, decrypted);
    }
  }

  @Nested
  class JceDecryptorTests {

    @Nested
    class Constructor {
      @Test
      @DisplayName("should throw when cipher argument is null")
      void cipherOverloadTest1() {
        assertThrows(IllegalArgumentException.class, () -> new JceDecryptor(null));
      }

      @Test
      @DisplayName("should use cipher argument's algorithm as decryptor name")
      void cipherOverloadTest2()
          throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
              InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
        JceDecryptor decryptor = new JceDecryptor(cipher);
        assertEquals(cipher.getAlgorithm(), decryptor.name());
      }

      @Test
      @DisplayName("should throw when name argument is null")
      void nameAndCipherOverloadTest1()
          throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
              InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
        assertThrows(IllegalArgumentException.class, () -> new JceDecryptor(null, cipher));
      }

      @Test
      @DisplayName("should throw when cipher argument is null")
      void nameAndCipherOverloadTest2()
          throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        assertThrows(IllegalArgumentException.class, () -> new JceDecryptor("MyDecryptor", null));
      }

      @Test
      @DisplayName("should use specified name as decryptor name")
      void nameAndCipherOverloadTest3()
          throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
              InvalidAlgorithmParameterException {
        String decryptorName = "MyDecryptor";
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
        JceDecryptor decryptor = new JceDecryptor(decryptorName, cipher);
        assertEquals(decryptorName, decryptor.name());
      }
    }

    @Nested
    class FactoryMethod {
      @Test
      @DisplayName("should never return null")
      void noArgOverloadTest1() {
        assertNotNull(JceDecryptor.factory());
      }

      @Test
      @DisplayName("should throw when provider argument is null")
      void withProviderStringOverloadTest1() {
        assertThrows(IllegalArgumentException.class, () -> JceDecryptor.factory((String) null));
      }

      @Test
      @DisplayName("should never return null")
      void withProviderStringOverloadTest2() {
        assertNotNull(JceDecryptor.factory("SunJCE"));
      }

      @Test
      @DisplayName("should throw when provider argument is null")
      void withProviderOverloadTest1() {
        assertThrows(IllegalArgumentException.class, () -> JceDecryptor.factory((Provider) null));
      }

      @Test
      @DisplayName("should never return null")
      void withProviderOverloadTest2() {
        assertNotNull(JceDecryptor.factory(Security.getProvider("SunJCE")));
      }
    }

    @Nested
    class FactoryTests {
      @Nested
      class AsymmetricMethodWithAlgorithmPrivateKeyOverload {
        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class, () -> factory.asymmetric(null, RSA_PRIVATE_KEY));
        }

        @Test
        @DisplayName("should throw when private key argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class, () -> factory.asymmetric(RSA_ALGORITHM, null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor = factory.asymmetric(RSA_ALGORITHM, RSA_PRIVATE_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor = factory.asymmetric(RSA_ALGORITHM, RSA_PRIVATE_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor = factory.asymmetric(RSA_ALGORITHM, RSA_PRIVATE_KEY);
          assertNotNull(decryptor);
        }
      }

      @Nested
      class AsymmetricMethodWithNameAndAlgorithmAndPrivateKeyOverload {
        @Test
        @DisplayName("should throw when name argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.asymmetric(null, RSA_ALGORITHM, RSA_PRIVATE_KEY));
        }

        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.asymmetric("CustomName", null, RSA_PRIVATE_KEY));
        }

        @Test
        @DisplayName("should throw when private key argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.asymmetric("CustomName", RSA_ALGORITHM, null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor = factory.asymmetric("CustomName", RSA_ALGORITHM, RSA_PRIVATE_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor = factory.asymmetric("CustomName", RSA_ALGORITHM, RSA_PRIVATE_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor = factory.asymmetric("CustomName", RSA_ALGORITHM, RSA_PRIVATE_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor with the specified name")
        void test4() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor = factory.asymmetric("CustomName", RSA_ALGORITHM, RSA_PRIVATE_KEY);
          assertNotNull(decryptor);
          assertEquals("CustomName", decryptor.name());
        }
      }

      @Nested
      class AsymmetricMethodWithAlgorithmAndPrivateKeyAndSecureRandomOverload {
        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.asymmetric(null, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when private key argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.asymmetric(RSA_ALGORITHM, null, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.asymmetric(RSA_ALGORITHM, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.asymmetric(RSA_ALGORITHM, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.asymmetric(RSA_ALGORITHM, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }
      }

      @Nested
      class AsymmetricMethodWithNameAndAlgorithmAndPrivateKeyAndSecureRandomOverload {
        @Test
        @DisplayName("should throw when name argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.asymmetric(null, RSA_ALGORITHM, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.asymmetric("CustomName", null, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when private key argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.asymmetric("CustomName", RSA_ALGORITHM, null, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.asymmetric(
                  "CustomName", RSA_ALGORITHM, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.asymmetric(
                  "CustomName", RSA_ALGORITHM, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.asymmetric(
                  "CustomName", RSA_ALGORITHM, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor with the specified name")
        void test4() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.asymmetric(
                  "CustomName", RSA_ALGORITHM, RSA_PRIVATE_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
          assertEquals("CustomName", decryptor.name());
        }
      }

      @Nested
      class SymmetricMethodWithAlgorithmAndSecretKeyOverload {
        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class, () -> factory.symmetric(null, AES_SECRET_KEY));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class, () -> factory.symmetric(AES_ALGORITHM, null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor = factory.symmetric(AES_ALGORITHM, AES_SECRET_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor = factory.symmetric(AES_ALGORITHM, AES_SECRET_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor = factory.symmetric(AES_ALGORITHM, AES_SECRET_KEY);
          assertNotNull(decryptor);
        }
      }

      @Nested
      class SymmetricMethodWithNameAndAlgorithmAndSecretKeyOverload {
        @Test
        @DisplayName("should throw when name argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(null, AES_ALGORITHM, AES_SECRET_KEY));
        }

        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric("CustomName", null, AES_SECRET_KEY));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric("CustomName", AES_ALGORITHM, null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor = factory.symmetric("CustomName", AES_ALGORITHM, AES_SECRET_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor = factory.symmetric("CustomName", AES_ALGORITHM, AES_SECRET_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor = factory.symmetric("CustomName", AES_ALGORITHM, AES_SECRET_KEY);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor with the specified name")
        void test4() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor = factory.symmetric("CustomName", AES_ALGORITHM, AES_SECRET_KEY);
          assertNotNull(decryptor);
          assertEquals("CustomName", decryptor.name());
        }
      }

      @Nested
      class SymmetricMethodWithAlgorithmAndSecretKeyAndSecureRandomOverload {
        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(null, AES_SECRET_KEY, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(AES_ALGORITHM, null, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(AES_ALGORITHM, AES_SECRET_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric(AES_ALGORITHM, AES_SECRET_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric(AES_ALGORITHM, AES_SECRET_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }
      }

      @Nested
      class SymmetricMethodWithNameAndAlgorithmAndSecretKeyAndSecureRandomOverload {
        @Test
        @DisplayName("should throw when name argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(null, AES_ALGORITHM, AES_SECRET_KEY, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric("CustomName", null, AES_SECRET_KEY, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric("CustomName", AES_ALGORITHM, null, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric("CustomName", AES_ALGORITHM, AES_SECRET_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric("CustomName", AES_ALGORITHM, AES_SECRET_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric("CustomName", AES_ALGORITHM, AES_SECRET_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor with the specified name")
        void test4() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric("CustomName", AES_ALGORITHM, AES_SECRET_KEY, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
          assertEquals("CustomName", decryptor.name());
        }
      }

      @Nested
      class SymmetricMethodWithAlgorithmAndSecretKeyAndAlgorithmParameterSpecOverload {
        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(null, AES_SECRET_KEY, GCM_PARAMETER_SPEC));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(AES_GCM_ALGORITHM, null, GCM_PARAMETER_SPEC));
        }

        @Test
        @DisplayName("should throw when algorithm parameter spec argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      AES_GCM_ALGORITHM, AES_SECRET_KEY, (AlgorithmParameterSpec) null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
          assertNotNull(decryptor);
        }
      }

      @Nested
      class SymmetricMethodWithNameAndAlgorithmAndSecretKeyAndAlgorithmParameterSpecOverload {
        @Test
        @DisplayName("should throw when name argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(null, AES_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC));
        }

        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric("CustomName", null, AES_SECRET_KEY, GCM_PARAMETER_SPEC));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric("CustomName", AES_GCM_ALGORITHM, null, GCM_PARAMETER_SPEC));
        }

        @Test
        @DisplayName("should throw when algorithm parameter spec argument is null")
        void validationTest4() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName",
                      AES_GCM_ALGORITHM,
                      AES_SECRET_KEY,
                      (AlgorithmParameterSpec) null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor with specified name")
        void test4()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
          assertNotNull(decryptor);
          assertEquals("CustomName", decryptor.name());
        }
      }

      @Nested
      class SymmetricMethodWithAlgorithmAndSecretKeyAndAlgorithmParameterSpecAndSecureRandomOverload {
        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      null, AES_SECRET_KEY, GCM_PARAMETER_SPEC, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      AES_GCM_ALGORITHM, null, GCM_PARAMETER_SPEC, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when algorithm parameter spec argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      AES_GCM_ALGORITHM,
                      AES_SECRET_KEY,
                      (AlgorithmParameterSpec) null,
                      STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secure random argument is null")
        void validationTest4() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC, null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(
                  AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric(
                  AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric(
                  AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }
      }

      @Nested
      class SymmetricMethodWithNameAndAlgorithmAndSecretKeyAndAlgorithmParameterSpecAndSecureRandomOverload {
        @Test
        @DisplayName("should throw when name argument is null")
        void validationTest1() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      null,
                      AES_ALGORITHM,
                      AES_SECRET_KEY,
                      GCM_PARAMETER_SPEC,
                      STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest2() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName",
                      null,
                      AES_SECRET_KEY,
                      GCM_PARAMETER_SPEC,
                      STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName",
                      AES_GCM_ALGORITHM,
                      null,
                      GCM_PARAMETER_SPEC,
                      STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when algorithm parameter spec argument is null")
        void validationTest4() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName",
                      AES_GCM_ALGORITHM,
                      AES_SECRET_KEY,
                      (AlgorithmParameterSpec) null,
                      STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secure random argument is null")
        void validationTest5() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC, null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName",
                  AES_GCM_ALGORITHM,
                  AES_SECRET_KEY,
                  GCM_PARAMETER_SPEC,
                  STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName",
                  AES_GCM_ALGORITHM,
                  AES_SECRET_KEY,
                  GCM_PARAMETER_SPEC,
                  STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName",
                  AES_GCM_ALGORITHM,
                  AES_SECRET_KEY,
                  GCM_PARAMETER_SPEC,
                  STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor with the specified name")
        void test4()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName",
                  AES_GCM_ALGORITHM,
                  AES_SECRET_KEY,
                  GCM_PARAMETER_SPEC,
                  STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
          assertEquals("CustomName", decryptor.name());
        }
      }

      @Nested
      class SymmetricMethodWithAlgorithmAndSecretKeyAndAlgorithmParameterOverload {
        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest1() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(null, AES_SECRET_KEY, GCM_PARAMETERS));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest2() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(AES_GCM_ALGORITHM, null, GCM_PARAMETERS));
        }

        @Test
        @DisplayName("should throw when algorithm parameter spec argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, (AlgorithmParameters) null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS);
          assertNotNull(decryptor);
        }
      }

      @Nested
      class SymmetricMethodWithNameAndAlgorithmAndSecretKeyAndAlgorithmParameterOverload {
        @Test
        @DisplayName("should throw when name argument is null")
        void validationTest1() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(null, AES_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS));
        }

        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest2() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric("CustomName", null, AES_SECRET_KEY, GCM_PARAMETERS));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest3() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric("CustomName", AES_GCM_ALGORITHM, null, GCM_PARAMETERS));
        }

        @Test
        @DisplayName("should throw when algorithm parameter spec argument is null")
        void validationTest4() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, (AlgorithmParameters) null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric("CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric("CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());
          JceDecryptor decryptor =
              factory.symmetric("CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor with the specified name")
        void test4()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric("CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS);
          assertNotNull(decryptor);
          assertEquals("CustomName", decryptor.name());
        }
      }

      @Nested
      class SymmetricMethodWithAlgorithmAndSecretKeyAndAlgorithmParameterAndSecureRandomOverload {
        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest1() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(null, AES_SECRET_KEY, GCM_PARAMETERS, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest2() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(AES_GCM_ALGORITHM, null, GCM_PARAMETERS, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when algorithm parameters argument is null")
        void validationTest3() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      AES_GCM_ALGORITHM,
                      AES_SECRET_KEY,
                      (AlgorithmParameters) null,
                      STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secure random argument is null")
        void validationTest4() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () -> factory.symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS, null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(
                  AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric(
                  AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());

          JceDecryptor decryptor =
              factory.symmetric(
                  AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS, STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }
      }

      @Nested
      class SymmetricMethodWithNameAndAlgorithmAndSecretKeyAndAlgorithmParameterAndSecureRandomOverload {
        @Test
        @DisplayName("should throw when name argument is null")
        void validationTest1() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      null, AES_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when algorithm argument is null")
        void validationTest2() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName", null, AES_SECRET_KEY, GCM_PARAMETERS, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secret key argument is null")
        void validationTest3() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName", AES_GCM_ALGORITHM, null, GCM_PARAMETERS, STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when algorithm parameters argument is null")
        void validationTest4() {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName",
                      AES_GCM_ALGORITHM,
                      AES_SECRET_KEY,
                      (AlgorithmParameters) null,
                      STRONG_SECURE_RANDOM));
        }

        @Test
        @DisplayName("should throw when secure random argument is null")
        void validationTest5() throws NoSuchAlgorithmException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  factory.symmetric(
                      "CustomName", AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETERS, null));
        }

        @Test
        @DisplayName("should create a JCE decryptor")
        void test1()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName",
                  AES_GCM_ALGORITHM,
                  AES_SECRET_KEY,
                  GCM_PARAMETERS,
                  STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test2()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory(BOUNCY_CASTLE_PROVIDER_NAME);
          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName",
                  AES_GCM_ALGORITHM,
                  AES_SECRET_KEY,
                  GCM_PARAMETERS,
                  STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor from the security provider")
        void test3()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory(new BouncyCastleProvider());

          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName",
                  AES_GCM_ALGORITHM,
                  AES_SECRET_KEY,
                  GCM_PARAMETERS,
                  STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
        }

        @Test
        @DisplayName("should create a JCE decryptor with the specified name")
        void test4()
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                InvalidAlgorithmParameterException, InvalidParameterSpecException {
          JceDecryptor.Factory factory = JceDecryptor.factory();

          JceDecryptor decryptor =
              factory.symmetric(
                  "CustomName",
                  AES_GCM_ALGORITHM,
                  AES_SECRET_KEY,
                  GCM_PARAMETERS,
                  STRONG_SECURE_RANDOM);
          assertNotNull(decryptor);
          assertEquals("CustomName", decryptor.name());
        }
      }
    }
  }

  private static DecryptProcessor processorToTest(Decryptor... decryptors) {
    return new DecryptProcessor(decryptors);
  }

  private static ExternalizedProperties externalizedProperties(Processor... processors) {
    return ExternalizedProperties.builder().processors(processors).build();
  }

  private static Decryptor createSymmetricDecryptor() {
    try {
      return JceDecryptor.factory()
          .symmetric(AES_GCM_ALGORITHM, AES_SECRET_KEY, GCM_PARAMETER_SPEC);
    } catch (InvalidKeyException
        | NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidAlgorithmParameterException e) {
      throw new IllegalStateException("Cannot instantiate decryptor.", e);
    }
  }

  private static Decryptor createAsymmetricDecryptor() {
    try {
      return JceDecryptor.factory().asymmetric(RSA_ALGORITHM, RSA_PRIVATE_KEY);
    } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new IllegalStateException("Cannot instantiate decryptor.", e);
    }
  }

  private static SecureRandom getStrongSecureRandom() {
    try {
      return SecureRandom.getInstanceStrong();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Cannot obtain strong secure random instance.", e);
    }
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("proeprty.aes.gcm.encrypted")
    @Decrypt(AES_GCM_ALGORITHM)
    String decryptAesGcm();

    @ExternalizedProperty("proeprty.rsa.encrypted")
    @Decrypt(RSA_ALGORITHM)
    String decryptRsa();

    @ExternalizedProperty("proeprty.not.annotated.with.decrypt")
    String notAnnotatedWithDecrypt();
  }
}
