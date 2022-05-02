package io.github.joeljeremy7.externalizedproperties.core.processing.processors;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ProcessorProvider;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.ProcessingException;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DecryptProcessorTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY = 
        new ProxyMethodFactory<>(ProxyInterface.class);

    // AES constants.
    private static final String AES_ALGORITHM = EncryptionUtils.AES_ALGORITHM;
    private static final String AES_GCM_ALGORITHM = EncryptionUtils.AES_GCM_ALGORITHM;
    private static final AlgorithmParameterSpec GCM_PARAMETER_SPEC = 
        EncryptionUtils.createGcmParameterSpec();
    private static final AlgorithmParameters GCM_PARAMETERS = 
        EncryptionUtils.createGcmParameters();
    private static final SecretKey AES_SECRET_KEY = EncryptionUtils.generateAesSecretKey();
    private static final Decryptor AES_GCM_DECRYPTOR = createSymmetricDecryptor();

    // RSA constants.

    private static final String RSA_ALGORITHM = "RSA";
    private static final KeyPair RSA_KEY_PAIR = EncryptionUtils.generateRsaKeyPair();
    private static final Decryptor RSA_DECRYPTOR = createAsymmetricDecryptor();

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
            assertThrows(
                IllegalArgumentException.class, 
                () -> new DecryptProcessor((Decryptor[])null)
            );
        }

        @Test
        @DisplayName("should throw when decryptors argument is empty")
        void withVarArgsOverloadTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new DecryptProcessor(new Decryptor[0])
            );
        }

        @Test
        @DisplayName("should throw when decryptors argument is null")
        void withCollectionOverloadTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new DecryptProcessor((Collection<Decryptor>)null)
            );
        }



        @Test
        @DisplayName("should throw when decryptors argument is empty")
        void withCollectionOverloadTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new DecryptProcessor(Collections.emptyList())
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should throw when decryptor argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> DecryptProcessor.provider(null)
            );
        }

        @Test
        @DisplayName("should not return null.")
        public void test2() {
            ProcessorProvider<DecryptProcessor> provider = 
                DecryptProcessor.provider(AES_GCM_DECRYPTOR);

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get")
        public void test3() {
            ProcessorProvider<DecryptProcessor> provider = 
                DecryptProcessor.provider(AES_GCM_DECRYPTOR);

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ProcessMethod {
        @Test
        @DisplayName("should throw when decryptor with specified name is not registered")
        void test1() {
            // AES decryptor is not registered.
            DecryptProcessor processor = processorToTest(RSA_DECRYPTOR);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::decryptAesGcm
            );

            String plainText = "plain-text";
            String aesEncryptedBase64 = EncryptionUtils.encryptAesBase64(
                plainText, 
                AES_GCM_ALGORITHM,
                AES_SECRET_KEY
            );
            
            assertThrows(
                ProcessingException.class, 
                () -> processor.process(proxyMethod, aesEncryptedBase64)
            );
        }

        @Test
        @DisplayName("should throw when proxy method is not annotated with @Decrypt")
        void test2() {
            DecryptProcessor processor = processorToTest(AES_GCM_DECRYPTOR);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::notAnnotatedWithDecrypt
            );

            String plainText = "plain-text";
            String aesEncryptedBase64 = EncryptionUtils.encryptAesBase64(
                plainText, 
                AES_GCM_ALGORITHM,
                AES_SECRET_KEY
            );
            
            assertThrows(
                ProcessingException.class, 
                () -> processor.process(proxyMethod, aesEncryptedBase64)
            );
        }
        
        @Test
        @DisplayName("should wrap exceptions when decrypting values")
        void test3() {
            DecryptProcessor processor = processorToTest(RSA_DECRYPTOR);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::decryptRsa
            );

            String plainText = "plain-text";
            // Oops! This is encrypted with AES, not RSA. 
            // Should throw when decrypting with RSA.
            String aesEncryptedBase64 = EncryptionUtils.encryptAesBase64(
                plainText, 
                AES_GCM_ALGORITHM, 
                AES_SECRET_KEY
            );
            
            assertThrows(
                ProcessingException.class, 
                () -> processor.process(proxyMethod, aesEncryptedBase64)
            );
        }
        
        @Test
        @DisplayName("should throw when property is not in Base64 format")
        void test4() {
            DecryptProcessor processor = processorToTest(RSA_DECRYPTOR);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::decryptRsa
            );

            String plainText = "plain-text";
            byte[] encrypted = EncryptionUtils.encryptAes(
                plainText, 
                AES_GCM_ALGORITHM, 
                AES_SECRET_KEY,
                GCM_PARAMETER_SPEC
            );

            // Encrypted bytes were not encoded in Base64.
            String notInBase64Format = new String(encrypted, StandardCharsets.UTF_8);

            assertThrows(
                IllegalArgumentException.class, 
                () -> processor.process(proxyMethod, notInBase64Format)
            );
        }

        @Test
        @DisplayName("should decrypt property using specified decryptor")
        void aesTest1() {
            DecryptProcessor processor = processorToTest(AES_GCM_DECRYPTOR);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::decryptAesGcm
            );

            String plainText = "plain-text";
            String aesEncryptedBase64 = EncryptionUtils.encryptAesBase64(
                plainText, 
                AES_GCM_ALGORITHM,
                AES_SECRET_KEY,
                GCM_PARAMETER_SPEC
            );
            String decrypted = processor.process(proxyMethod, aesEncryptedBase64);

            assertEquals(plainText, decrypted);
        }
        
        @Test
        @DisplayName("should decrypt property using specified decryptor")
        void rsaTest1() {
            DecryptProcessor processor = processorToTest(RSA_DECRYPTOR);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::decryptRsa
            );

            String plainText = "plain-text";
            String rsaEncryptedBase64 = 
                EncryptionUtils.encryptRsaBase64(plainText, RSA_KEY_PAIR.getPublic());
            String decrypted = processor.process(proxyMethod, rsaEncryptedBase64);

            assertEquals(plainText, decrypted);
        }
    }

    @Nested
    class JceDecryptorTests {
        @Nested
        class FactoryMethod {
            @Test
            @DisplayName("should never return null")
            void noArgOverloadTest1() {
                assertNotNull(JceDecryptor.factory());
            }

            @Test
            @DisplayName("should never return null")
            void withProviderStringOverloadTest1() {
                assertNotNull(JceDecryptor.factory("SunJCE"));
            }

            @Test
            @DisplayName("should throw when provider argument is null")
            void withProviderStringOverloadTest2() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> JceDecryptor.factory((String)null)
                );
            }

            @Test
            @DisplayName("should never return null")
            void withProviderOverloadTest1() {
                assertNotNull(JceDecryptor.factory(Security.getProvider("SunJCE")));
            }

            @Test
            @DisplayName("should throw when provider argument is null")
            void withProviderOverloadTest2() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> JceDecryptor.factory((Provider)null)
                );
            }
        }

        @Nested
        class FactoryTests {
            @Nested
            class AsymmetricMethodWithAlgorithmAndPrivateKeyOverload {
                @Test
                @DisplayName("should throw when algorithm argument is null")
                void validationTest1() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.asymmetric(null, RSA_KEY_PAIR.getPrivate())
                    );
                }

                @Test
                @DisplayName("should throw when private key argument is null")
                void validationTest2() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.asymmetric(RSA_ALGORITHM, null)
                    );
                }

                @Test
                @DisplayName("should create a JCE decryptor")
                void test1() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    JceDecryptor decryptor = 
                        factory.asymmetric(RSA_ALGORITHM, RSA_KEY_PAIR.getPrivate());
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory("BC");
                    JceDecryptor decryptor = factory.asymmetric(
                        RSA_ALGORITHM, 
                        RSA_KEY_PAIR.getPrivate()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory(
                        new BouncyCastleProvider()
                    );
                    JceDecryptor decryptor = factory.asymmetric(
                        RSA_ALGORITHM, 
                        RSA_KEY_PAIR.getPrivate()
                    );
                    assertNotNull(decryptor);
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
                        () -> factory.asymmetric(
                            null, 
                            RSA_KEY_PAIR.getPrivate(),
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should throw when private key argument is null")
                void validationTest2() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.asymmetric(
                            RSA_ALGORITHM, 
                            null,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should create a JCE decryptor")
                void test1() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    JceDecryptor decryptor = factory.asymmetric(
                        RSA_ALGORITHM, 
                        RSA_KEY_PAIR.getPrivate(),
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory("BC");
                    JceDecryptor decryptor = factory.asymmetric(
                        RSA_ALGORITHM, 
                        RSA_KEY_PAIR.getPrivate(),
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory(
                        new BouncyCastleProvider()
                    );
                    JceDecryptor decryptor = factory.asymmetric(
                        RSA_ALGORITHM, 
                        RSA_KEY_PAIR.getPrivate(),
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }
            }

            @Nested
            class SymmetricMethodWithAlgorithmAndSecretKeyOverload {
                @Test
                @DisplayName("should throw when algorithm argument is null")
                void validationTest1() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            null, 
                            AES_SECRET_KEY
                        )
                    );
                }

                @Test
                @DisplayName("should throw when secret key argument is null")
                void validationTest2() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_ALGORITHM, 
                            null
                        )
                    );
                }

                @Test
                @DisplayName("should create a JCE decryptor")
                void test1() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    JceDecryptor decryptor = factory.symmetric(
                        AES_ALGORITHM, 
                        AES_SECRET_KEY
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory("BC");
                    JceDecryptor decryptor = factory.symmetric(
                        AES_ALGORITHM, 
                        AES_SECRET_KEY
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory(
                        new BouncyCastleProvider()
                    );
                    JceDecryptor decryptor = factory.symmetric(
                        AES_ALGORITHM, 
                        AES_SECRET_KEY
                    );
                    assertNotNull(decryptor);
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
                        () -> factory.symmetric(
                            null, 
                            AES_SECRET_KEY,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should throw when secret key argument is null")
                void validationTest2() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_ALGORITHM, 
                            null,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should create a JCE decryptor")
                void test1() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    JceDecryptor decryptor = factory.symmetric(
                        AES_ALGORITHM, 
                        AES_SECRET_KEY,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory("BC");
                    JceDecryptor decryptor = factory.symmetric(
                        AES_ALGORITHM, 
                        AES_SECRET_KEY,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test3() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
                    JceDecryptor.Factory factory = JceDecryptor.factory(
                        new BouncyCastleProvider()
                    );
                    JceDecryptor decryptor = factory.symmetric(
                        AES_ALGORITHM, 
                        AES_SECRET_KEY,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
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
                        () -> factory.symmetric(
                            null, 
                            AES_SECRET_KEY,
                            GCM_PARAMETER_SPEC
                        )
                    );
                }

                @Test
                @DisplayName("should throw when secret key argument is null")
                void validationTest2() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM, 
                            null,
                            GCM_PARAMETER_SPEC
                        )
                    );
                }

                @Test
                @DisplayName("should throw when algorithm parameter spec argument is null")
                void validationTest3() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM, 
                            AES_SECRET_KEY,
                            (AlgorithmParameterSpec)null
                        )
                    );
                }

                @Test
                @DisplayName("should create a JCE decryptor")
                void test1() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETER_SPEC
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test2() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException {
                    JceDecryptor.Factory factory = JceDecryptor.factory("BC");
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETER_SPEC
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test3() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException {
                    JceDecryptor.Factory factory = JceDecryptor.factory(
                        new BouncyCastleProvider()
                    );
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETER_SPEC
                    );
                    assertNotNull(decryptor);
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
                        () -> factory.symmetric(
                            null, 
                            AES_SECRET_KEY,
                            GCM_PARAMETER_SPEC,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should throw when secret key argument is null")
                void validationTest2() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM, 
                            null,
                            GCM_PARAMETER_SPEC,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should throw when algorithm parameter spec argument is null")
                void validationTest3() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM, 
                            AES_SECRET_KEY,
                            (AlgorithmParameterSpec)null,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should throw when secure random argument is null")
                void validationTest4() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM,
                            AES_SECRET_KEY,
                            GCM_PARAMETER_SPEC,
                            null
                        )
                    );
                }

                @Test
                @DisplayName("should create a JCE decryptor")
                void test1() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETER_SPEC,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test2() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException {
                    JceDecryptor.Factory factory = JceDecryptor.factory("BC");
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETER_SPEC,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test3() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException {
                    JceDecryptor.Factory factory = JceDecryptor.factory(
                        new BouncyCastleProvider()
                    );
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETER_SPEC,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
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
                        () -> factory.symmetric(
                            null, 
                            AES_SECRET_KEY,
                            GCM_PARAMETERS
                        )
                    );
                }

                @Test
                @DisplayName("should throw when secret key argument is null")
                void validationTest2() throws NoSuchAlgorithmException, InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM, 
                            null,
                            GCM_PARAMETERS
                        )
                    );
                }

                @Test
                @DisplayName("should throw when algorithm parameter spec argument is null")
                void validationTest3() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM, 
                            AES_SECRET_KEY,
                            (AlgorithmParameters)null
                        )
                    );
                }

                @Test
                @DisplayName("should create a JCE decryptor")
                void test1() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException, 
                        InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETERS
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test2() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException, 
                        InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory("BC");
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETERS
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test3() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException,
                        InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory(
                        new BouncyCastleProvider()
                    );
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETERS
                    );
                    assertNotNull(decryptor);
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
                        () -> factory.symmetric(
                            null, 
                            AES_SECRET_KEY,
                            GCM_PARAMETERS,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should throw when secret key argument is null")
                void validationTest2() throws NoSuchAlgorithmException, InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM, 
                            null,
                            GCM_PARAMETERS,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should throw when algorithm parameters argument is null")
                void validationTest3() {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM, 
                            AES_SECRET_KEY,
                            (AlgorithmParameters)null,
                            SecureRandom.getInstanceStrong()
                        )
                    );
                }

                @Test
                @DisplayName("should throw when secure random argument is null")
                void validationTest4() throws NoSuchAlgorithmException, InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    assertThrows(
                        IllegalArgumentException.class, 
                        () -> factory.symmetric(
                            AES_GCM_ALGORITHM,
                            AES_SECRET_KEY,
                            GCM_PARAMETERS,
                            null
                        )
                    );
                }

                @Test
                @DisplayName("should create a JCE decryptor")
                void test1() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException, 
                        InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory();
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETERS,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test2() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException, 
                        InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory("BC");
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETERS,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }

                @Test
                @DisplayName("should create a JCE decryptor from the security provider")
                void test3() throws InvalidKeyException, 
                        NoSuchAlgorithmException, 
                        NoSuchPaddingException, 
                        InvalidAlgorithmParameterException,
                        InvalidParameterSpecException {
                    JceDecryptor.Factory factory = JceDecryptor.factory(
                        new BouncyCastleProvider()
                    );
                    
                    JceDecryptor decryptor = factory.symmetric(
                        AES_GCM_ALGORITHM, 
                        AES_SECRET_KEY,
                        GCM_PARAMETERS,
                        SecureRandom.getInstanceStrong()
                    );
                    assertNotNull(decryptor);
                }
            }
        }
    }

    private static DecryptProcessor processorToTest(Decryptor... decryptors) {
        return new DecryptProcessor(decryptors);
    }

    private static Decryptor createSymmetricDecryptor() {
        try {
            return JceDecryptor.factory().symmetric(
                AES_GCM_ALGORITHM, 
                AES_SECRET_KEY, 
                GCM_PARAMETER_SPEC
            );
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Cannot instantiate decryptor.", e);
        }
    }

    private static Decryptor createAsymmetricDecryptor() {
        try {
            return JceDecryptor.factory().asymmetric(RSA_ALGORITHM, RSA_KEY_PAIR.getPrivate());
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("Cannot instantiate decryptor.", e);
        }
    }

    public static interface ProxyInterface {
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
