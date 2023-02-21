package fr.upec.e2ee.protocol;

import fr.upec.e2ee.Tools;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Cipher and Decipher text
 */
public class Cipher {
    /**
     * Galois Counter Mode IV
     */
    public static final int GCM_IV_LENGTH = 12;
    /**
     * Galois Counter Mode Tag
     */
    public static final int GCM_TAG_LENGTH = 16;

    /**
     * Cipher an input
     *
     * @param secretKey Symmetric Key
     * @param input     Text in Bytes
     * @return Return a ciphered input in Bytes
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static byte[] cipher(SecretKey secretKey, byte[] input) throws GeneralSecurityException {
        // Get Cipher Instance
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES_256/GCM/NoPadding");

        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = Tools.generateSecureRandom();
        random.nextBytes(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] cipherText = cipher.doFinal(input);

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        return byteBuffer.array();
    }

    /**
     * Decipher a ciphered input
     *
     * @param secretKey     Symmetric Key
     * @param cipherMessage Ciphered input in Bytes
     * @return Return an input in Bytes
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static byte[] decipher(SecretKey secretKey, byte[] cipherMessage) throws GeneralSecurityException {
        // Get Cipher Instance
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES_256/GCM/NoPadding");

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, cipherMessage, 0, GCM_IV_LENGTH);

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

        // Perform Decryption and Return
        return cipher.doFinal(cipherMessage, GCM_IV_LENGTH, cipherMessage.length - GCM_IV_LENGTH);
    }
}
