package io.github.joeljeremy7.externalizedproperties.core.processing.processors;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.ProcessorProvider;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.ProcessingException;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

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
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyCollection;
import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * Processor to apply decryption to a property.
 */
public class DecryptProcessor implements Processor {

    private final Map<String, Decryptor> decryptorsByName;

    /**
     * Constructor.
     * 
     * @param decryptors The {@link Decryptor}s to do the decrypting.
     */
    public DecryptProcessor(Decryptor... decryptors) {
        this(Arrays.asList(requireNonNull(decryptors, "decryptors")));
    }

    /**
     * Constructor.
     * 
     * @param decryptors The {@link Decryptor}s to do the decrypting.
     */
    public DecryptProcessor(Collection<Decryptor> decryptors) {
        requireNonNullOrEmptyCollection(decryptors, "decryptors");
        this.decryptorsByName = decryptors.stream().collect(
            Collectors.toMap(
                Decryptor::name, 
                Function.identity(), 
                DecryptProcessor::throwOnDuplicateDecryptors
            )
        );
    }

    /**
     * The {@link ProcessorProvider} for {@link DecryptProcessor}.
     * 
     * @param decryptor The {@link Decryptor} to do the decrypting.
     * @return The {@link ProcessorProvider} for {@link DecryptProcessor}.
     */
    public static ProcessorProvider<DecryptProcessor> provider(Decryptor decryptor) {
        requireNonNull(decryptor, "decryptor");
        return externalizedProperties -> new DecryptProcessor(decryptor);
    }

    /**
     * {@inheritDoc}
     * 
     * @param proxyMethod The proxy method.
     * @param base64EncodedValue The value to decrypt. This is expected to be the 
     * encrypted data in Base64 format.
     * @return The decrypted String.
     */
    @Override
    public String process(ProxyMethod proxyMethod, String base64EncodedValue) {
        // Encrypted string is expected to be in Base64.
        byte[] encryptedValue = base64Decode(base64EncodedValue);
        
        try {
            Decryptor decryptor = determineDecryptor(proxyMethod);
            byte[] decrypted = decryptor.decrypt(proxyMethod, encryptedValue);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (RuntimeException e) {
            throw new ProcessingException(
                "Processor resulted in an exception while trying to decrypt.",
                e
            );
        }
    }

    private static byte[] base64Decode(String base64EncodedValue) {
        try {
            return Base64.getDecoder().decode(base64EncodedValue);
        } catch (IllegalArgumentException e) {
            // Use a more meaningful error.
            throw new IllegalArgumentException(
                "Value to decrypt is expected to be in Base64 format.",
                e
            );
        }
    }

    private Decryptor determineDecryptor(ProxyMethod proxyMethod) {
        Decrypt decrypt = proxyMethod.findAnnotation(Decrypt.class)
            // Should not happen because Decrypt is annotated with 
            // @ProcessWith(DecryptProcessor.class)
            .orElseThrow(() -> new ProcessingException(String.format(
                "Proxy method %s is not annotated with %s.",
                proxyMethod.methodSignatureString(),
                Decrypt.class
            )));
        
        String decryptorName = decrypt.value();
        Decryptor decryptor = decryptorsByName.get(decryptorName);
        if (decryptor == null) {
            throw new ProcessingException(
                String.format(
                    "No decryptor registered with name: %s. " +
                    "Please make sure the decryptor is registered to %s " + 
                    "building %s.",
                    decryptorName, 
                    DecryptProcessor.class.getName(), 
                    ExternalizedProperties.class.getSimpleName()
                )
            );
        }
        return decryptor;
    }
    private static Decryptor throwOnDuplicateDecryptors(
            Decryptor existing,
            Decryptor toMerge
    ) {
        throw new IllegalArgumentException(String.format(
            "Duplicate decryptors (Names: %s).",
            existing.name()
        ));
    }

    /**
     * Used by {@link DecryptProcessor} to decrypt properties. This extension point
     * is provided to let clients choose how to decrypt values. Clients may choose to use 
     * the built-in {@link Decryptor} implementations, use a proprietary implementation,
     * or use services such as KMS, Key Vault, etc.
     */
    public static interface Decryptor {
        /**
         * The name of this decryptor.
         * 
         * @return The name of this decryptor.
         */
        String name();

        /**
         * Decrypt value.
         * 
         * @param proxyMethod The proxy method.
         * @param valueToDecrypt The value to decrypt.
         * @return The decrypted value.
         */
        byte[] decrypt(ProxyMethod proxyMethod, byte[] valueToDecrypt);
    }

    /**
     * A {@link Decryptor} which uses {@link Cipher} from Java Cryptography Extension (JCE) 
     * to decrypt values. If custom providers are desired, create a {@link JceDecryptor} by
     * providing an initialized {@link Cipher} from the provider.
     */
    public static class JceDecryptor implements Decryptor {

        private final String name;
        private final Cipher initializedCipher;

         /**
         * Constructor. Using this constructor will set {@link Decryptor#name()} to the cipher's
         * algorithm ({@link Cipher#getAlgorithm()}).
         * 
         * @param initializedCipher The {@link Cipher} to do the decrypting. This must already be
         * initialized and ready to use. Re-initializing the {@link Cipher} after constructing
         * this processor may result in unexpected behaviors.
         */
        public JceDecryptor(Cipher initializedCipher) {
            this(
                requireNonNull(initializedCipher, "initializedCipher").getAlgorithm(), 
                initializedCipher
            );
        }


         /**
         * Constructor.
         * 
         * @param name The name of this decryptor.
         * @param initializedCipher The {@link Cipher} to do the decrypting. This must already be
         * initialized and ready to use. Re-initializing the {@link Cipher} after constructing
         * this processor may result in unexpected behaviors.
         */
        public JceDecryptor(String name, Cipher initializedCipher) {
            this.name = requireNonNullOrEmptyString(name, "name");
            this.initializedCipher = requireNonNull(initializedCipher, "initializedCipher");
        }

        /**
         * The {@link Factory} which creates {@link JceDecryptor} instances.
         * 
         * @return The {@link Factory} which creates {@link JceDecryptor} instances.
         */
        public static Factory factory() {
            return new Factory();
        }

        /**
         * The {@link Factory} which creates {@link JceDecryptor} instances using the specified
         * provider.
         * 
         * @param provider The name of the Java Cryptography Extension (JCE) provider.
         * @return The {@link Factory} which creates {@link JceDecryptor} instances using the 
         * specified provider.
         */
        public static Factory factory(String provider) {
            return new Factory(Security.getProvider(provider));
        }

        /**
         * The {@link Factory} which creates {@link JceDecryptor} instances using the specified
         * provider.
         * 
         * @param provider The Java Cryptography Extension (JCE) provider.
         * @return The {@link Factory} which creates {@link JceDecryptor} instances using the 
         * specified provider.
         */
        public static Factory factory(Provider provider) {
            return new Factory(provider);
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return name;
        }

        /**
         * Decrypt the value using the configured {@link Cipher}.
         * 
         * @param proxyMethod The proxy method.
         * @param valueToDecrypt The value to decrypt.
         * @return The decrypted value.
         */
        @Override
        public byte[] decrypt(ProxyMethod proxyMethod, byte[] valueToDecrypt) {
            try {
                return initializedCipher.doFinal(valueToDecrypt);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new ProcessingException(
                    "Exception occurred while trying to decrypt.",
                    e
                );
            }
        }

        public static class Factory {
            private final @Nullable Provider provider;

            /**
             * Constructor.
             */
            private Factory() {
                this.provider = null;
            }

            /**
             * Constructor.
             * 
             * @param provider The Java Cryptography Extension (JCE) provider.
             */
            private Factory(Provider provider) {
                this.provider = requireNonNull(provider, "provider");
            }

            /**
             * Create a {@link JceDecryptor} which decrypts using the specified algorithm
             * and private key.
             * 
             * @param algorithm The algorithm.
             * @param privateKey The private key to use in decryption.
             * @return A {@link JceDecryptor} which decrypts using the specified algorithm
             * and private key.
             * @throws NoSuchAlgorithmException if algorithm is invalid or not avaiable in 
             * the environment.
             * @throws NoSuchPaddingException if padding mechanism is invalid or not avaiable in 
             * the environment.
             * @throws InvalidKeyException if key is invalid.
             */
            public JceDecryptor asymmetric(
                    String algorithm, 
                    PrivateKey privateKey
            ) throws NoSuchAlgorithmException,
                    NoSuchPaddingException, 
                    InvalidKeyException {
                requireNonNull(algorithm, "algorithm");
                requireNonNull(privateKey, "privateKey");
                Cipher cipher = getCipher(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                return new JceDecryptor(cipher);
            }

            /**
             * Create a {@link JceDecryptor} which decrypts using the specified algorithm
             * and private key.
             * 
             * @param algorithm The algorithm.
             * @param privateKey The private key to use in decryption.
             * @param secureRandom The source of randomness.
             * @return A {@link JceDecryptor} which decrypts using the specified algorithm
             * and private key.
             * @throws NoSuchAlgorithmException if algorithm is invalid or not avaiable in 
             * the environment.
             * @throws NoSuchPaddingException if padding mechanism is invalid or not avaiable in 
             * the environment.
             * @throws InvalidKeyException if key is invalid.
             */
            public JceDecryptor asymmetric(
                    String algorithm, 
                    PrivateKey privateKey,
                    SecureRandom secureRandom
            ) throws NoSuchAlgorithmException,
                    NoSuchPaddingException, 
                    InvalidKeyException {
                requireNonNull(algorithm, "algorithm");
                requireNonNull(privateKey, "privateKey");
                requireNonNull(secureRandom, "secureRandom");
                Cipher cipher = getCipher(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, privateKey, secureRandom);
                return new JceDecryptor(cipher);
            }

            /**
             * Create a {@link JceDecryptor} which decrypts using the specified algorithm and
             * secret key.
             * 
             * @param algorithm The algorithm.
             * @param secretKey The secret key to use in decryption.
             * @return A {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameter spec.
             * @throws NoSuchAlgorithmException if algorithm is invalid or not avaiable in 
             * the environment.
             * @throws NoSuchPaddingException if padding mechanism is invalid or not avaiable in 
             * the environment.
             * @throws InvalidKeyException if key is invalid.
             */
            public JceDecryptor symmetric(
                    String algorithm, 
                    SecretKey secretKey
            ) throws NoSuchAlgorithmException, 
                    NoSuchPaddingException, 
                    InvalidKeyException {
                requireNonNull(algorithm, "algorithm");
                requireNonNull(secretKey, "secretKey");
                Cipher cipher = getCipher(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return new JceDecryptor(cipher);
            }

            /**
             * Create a {@link JceDecryptor} which decrypts using the specified algorithm and
             * secret key.
             * 
             * @param algorithm The algorithm.
             * @param secretKey The secret key to use in decryption.
             * @param secureRandom The source of randomness.
             * @return A {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameter spec.
             * @throws NoSuchAlgorithmException if algorithm is invalid or not avaiable in 
             * the environment.
             * @throws NoSuchPaddingException if padding mechanism is invalid or not avaiable in 
             * the environment.
             * @throws InvalidKeyException if key is invalid.
             */
            public JceDecryptor symmetric(
                    String algorithm, 
                    SecretKey secretKey,
                    SecureRandom secureRandom
            ) throws NoSuchAlgorithmException, 
                    NoSuchPaddingException, 
                    InvalidKeyException {
                requireNonNull(algorithm, "algorithm");
                requireNonNull(secretKey, "secretKey");
                requireNonNull(secureRandom, "secureRandom");
                Cipher cipher = getCipher(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, secureRandom);
                return new JceDecryptor(cipher);
            }

            /**
             * Create a {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameter spec.
             * 
             * @param algorithm The algorithm.
             * @param secretKey The secret key to use in decryption.
             * @param algorithmParameterSpec The algorithm parameter spec.
             * @return A {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameter spec.
             * @throws NoSuchAlgorithmException if algorithm is invalid or not avaiable in 
             * the environment.
             * @throws NoSuchPaddingException if padding mechanism is invalid or not avaiable in 
             * the environment.
             * @throws InvalidKeyException if key is invalid.
             * @throws InvalidAlgorithmParameterException if algorithm parameters are invalid.
             */
            public JceDecryptor symmetric(
                    String algorithm, 
                    SecretKey secretKey, 
                    AlgorithmParameterSpec algorithmParameterSpec
            ) throws NoSuchAlgorithmException, 
                    NoSuchPaddingException, 
                    InvalidKeyException, 
                    InvalidAlgorithmParameterException {
                requireNonNull(algorithm, "algorithm");
                requireNonNull(secretKey, "secretKey");
                requireNonNull(algorithmParameterSpec, "algorithmParameterSpec");
                Cipher cipher = getCipher(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, algorithmParameterSpec);
                return new JceDecryptor(cipher);
            }

            /**
             * Create a {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameter spec.
             * 
             * @param algorithm The algorithm.
             * @param secretKey The secret key to use in decryption.
             * @param algorithmParameterSpec The algorithm parameter spec.
             * @param secureRandom The source of randomness.
             * @return A {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameter spec.
             * @throws NoSuchAlgorithmException if algorithm is invalid or not avaiable in 
             * the environment.
             * @throws NoSuchPaddingException if padding mechanism is invalid or not avaiable in 
             * the environment.
             * @throws InvalidKeyException if key is invalid.
             * @throws InvalidAlgorithmParameterException if algorithm parameters are invalid.
             */
            public JceDecryptor symmetric(
                    String algorithm, 
                    SecretKey secretKey, 
                    AlgorithmParameterSpec algorithmParameterSpec,
                    SecureRandom secureRandom
            ) throws NoSuchAlgorithmException, 
                    NoSuchPaddingException, 
                    InvalidKeyException, 
                    InvalidAlgorithmParameterException {
                requireNonNull(algorithm, "algorithm");
                requireNonNull(secretKey, "secretKey");
                requireNonNull(algorithmParameterSpec, "algorithmParameterSpec");
                requireNonNull(secureRandom, "secureRandom");
                Cipher cipher = getCipher(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, algorithmParameterSpec, secureRandom);
                return new JceDecryptor(cipher);
            }
            
            /**
             * Create a {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameters.
             * 
             * @param algorithm The algorithm.
             * @param secretKey The secret key to use in decryption.
             * @param algorithmParameters The algorithm parameters.
             * @return A {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameter spec.
             * @throws NoSuchAlgorithmException if algorithm is invalid or not avaiable in 
             * the environment.
             * @throws NoSuchPaddingException if padding mechanism is invalid or not avaiable in 
             * the environment.
             * @throws InvalidKeyException if key is invalid.
             * @throws InvalidAlgorithmParameterException if algorithm parameters are invalid.
             */
            public JceDecryptor symmetric(
                    String algorithm, 
                    SecretKey secretKey, 
                    AlgorithmParameters algorithmParameters
            ) throws NoSuchAlgorithmException, 
                    NoSuchPaddingException, 
                    InvalidKeyException, 
                    InvalidAlgorithmParameterException {
                requireNonNull(algorithm, "algorithm");
                requireNonNull(secretKey, "secretKey");
                requireNonNull(algorithmParameters, "algorithmParameters");
                Cipher cipher = getCipher(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, algorithmParameters);
                return new JceDecryptor(cipher);
            }
            
            /**
             * Create a {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameters.
             * 
             * @param algorithm The algorithm.
             * @param secretKey The secret key to use in decryption.
             * @param algorithmParameters The algorithm parameters.
             * @param secureRandom The source of randomness.
             * @return A {@link JceDecryptor} which decrypts using the specified algorithm,
             * secret key, and algorithm parameter spec.
             * @throws NoSuchAlgorithmException if algorithm is invalid or not avaiable in 
             * the environment.
             * @throws NoSuchPaddingException if padding mechanism is invalid or not avaiable in 
             * the environment.
             * @throws InvalidKeyException if key is invalid.
             * @throws InvalidAlgorithmParameterException if algorithm parameters are invalid.
             */
            public JceDecryptor symmetric(
                    String algorithm, 
                    SecretKey secretKey, 
                    AlgorithmParameters algorithmParameters,
                    SecureRandom secureRandom
            ) throws NoSuchAlgorithmException, 
                    NoSuchPaddingException, 
                    InvalidKeyException, 
                    InvalidAlgorithmParameterException {
                requireNonNull(algorithm, "algorithm");
                requireNonNull(secretKey, "secretKey");
                requireNonNull(algorithmParameters, "algorithmParameters");
                requireNonNull(secureRandom, "secureRandom");
                Cipher cipher = getCipher(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, algorithmParameters, secureRandom);
                return new JceDecryptor(cipher);
            }

            private Cipher getCipher(String algorithm) 
                    throws NoSuchAlgorithmException, NoSuchPaddingException {
                if (provider != null) {
                    return Cipher.getInstance(algorithm, provider);
                }
                return Cipher.getInstance(algorithm);
            }
        }
    }
}
