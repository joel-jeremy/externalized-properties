package io.github.joeljeremy7.externalizedproperties.core.testentities;

import java.security.AlgorithmParameters;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class EncryptionUtils {
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  public static final SecretKey DEFAULT_AES_SECRET_KEY = generateAesSecretKey();
  public static final AlgorithmParameterSpec DEFAULT_GCM_PARAMETER_SPEC = createGcmParameterSpec();
  public static final AlgorithmParameters DEFAULT_GCM_PARAMETERS = createGcmParameters();

  public static final int DEFAULT_AES_KEY_SIZE = 256;
  public static final String AES_ALGORITHM = "AES";
  public static final String GCM_ALGORITHM_MODE = "GCM";
  public static final String NO_PADDING = "NoPadding";
  public static final String AES_GCM_ALGORITHM =
      AES_ALGORITHM + "/" + GCM_ALGORITHM_MODE + "/" + NO_PADDING;
  public static final int DEFAULT_GCM_TAG_LENGTH = 128;
  public static final int DEFAULT_GCM_IV_LENGTH = 12;

  public static final int DEFAULT_RSA_KEY_SIZE = 1024;
  public static final String RSA_ALGORITHM = "RSA";
  public static final KeyPair DEFAULT_RSA_KEY_PAIR = generateRsaKeyPair();
  public static final PrivateKey DEFAULT_RSA_PRIVATE_KEY = DEFAULT_RSA_KEY_PAIR.getPrivate();
  public static final PublicKey DEFAULT_RSA_PUBLIC_KEY = DEFAULT_RSA_KEY_PAIR.getPublic();

  private EncryptionUtils() {}

  public static KeyPair generateRsaKeyPair() {
    return generateRsaKeyPair(DEFAULT_RSA_KEY_SIZE);
  }

  public static KeyPair generateRsaKeyPair(int keySize) {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
      keyPairGenerator.initialize(keySize);
      return keyPairGenerator.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalArgumentException("Exception occurred while generating RSA key pair.", e);
    }
  }

  public static SecretKey generateAesSecretKey() {
    return generateAesSecretKey(DEFAULT_AES_KEY_SIZE);
  }

  public static SecretKey generateAesSecretKey(int aesKeySize) {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
      keyGenerator.init(DEFAULT_AES_KEY_SIZE);
      return keyGenerator.generateKey();
    } catch (Exception e) {
      throw new IllegalArgumentException("Exception occurred while generating AES secret key.", e);
    }
  }

  public static AlgorithmParameterSpec createGcmParameterSpec() {
    return createGcmParameterSpec(DEFAULT_GCM_TAG_LENGTH, DEFAULT_GCM_IV_LENGTH);
  }

  public static AlgorithmParameterSpec createGcmParameterSpec(int gcmTagLength, int gcmIvLength) {
    return new GCMParameterSpec(gcmTagLength, secureRandomBytes(gcmIvLength));
  }

  public static AlgorithmParameters createGcmParameters() {
    try {
      AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(GCM_ALGORITHM_MODE);
      algorithmParameters.init(createGcmParameterSpec());
      return algorithmParameters;
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Exception occurred while getting algorithm parameters.", e);
    }
  }

  public static AlgorithmParameters createAlgorithmParameters(
      String algorithm, AlgorithmParameterSpec algorithmParameterSpec) {
    try {
      AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(algorithm);
      algorithmParameters.init(algorithmParameterSpec);
      return algorithmParameters;
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Exception occurred while getting algorithm parameters.", e);
    }
  }

  public static String encryptRsaBase64(String value) {
    return Base64.getEncoder().encodeToString(encryptRsa(value, DEFAULT_RSA_PUBLIC_KEY));
  }

  public static String encryptRsaBase64(String value, PublicKey publicKey) {
    return Base64.getEncoder().encodeToString(encryptRsa(value, publicKey));
  }

  public static byte[] encryptRsa(String value, PublicKey publicKey) {
    try {
      Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      return cipher.doFinal(value.getBytes());
    } catch (Exception e) {
      throw new IllegalArgumentException("Exception occurred while encrypting.", e);
    }
  }

  public static String encryptAesBase64(String value) {
    return Base64.getEncoder().encodeToString(encryptAes(value));
  }

  public static String encryptAesBase64(String value, String aesAlgorithm, SecretKey secretKey) {
    return Base64.getEncoder().encodeToString(encryptAes(value, aesAlgorithm, secretKey));
  }

  public static String encryptAesBase64(
      String value,
      String aesAlgorithm,
      SecretKey secretKey,
      AlgorithmParameterSpec algorithmParameterSpec) {
    return Base64.getEncoder()
        .encodeToString(encryptAes(value, aesAlgorithm, secretKey, algorithmParameterSpec));
  }

  public static byte[] encryptAes(String value) {
    return encryptAes(value, AES_GCM_ALGORITHM, DEFAULT_AES_SECRET_KEY, DEFAULT_GCM_PARAMETER_SPEC);
  }

  public static byte[] encryptAes(String value, String aesAlgorithm, SecretKey secretKey) {
    try {
      Cipher cipher = Cipher.getInstance(aesAlgorithm);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      return cipher.doFinal(value.getBytes());
    } catch (Exception e) {
      throw new IllegalArgumentException("Exception occurred while encrypting.", e);
    }
  }

  public static byte[] encryptAes(
      String value,
      String aesAlgorithm,
      SecretKey secretKey,
      AlgorithmParameterSpec algorithmParameterSpec) {
    try {
      Cipher cipher = Cipher.getInstance(aesAlgorithm);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, algorithmParameterSpec);
      return cipher.doFinal(value.getBytes());
    } catch (Exception e) {
      throw new IllegalArgumentException("Exception occurred while encrypting.", e);
    }
  }

  public static byte[] secureRandomBytes(int length) {
    byte[] bytes = new byte[length];
    SECURE_RANDOM.nextBytes(bytes);
    return bytes;
  }
}
