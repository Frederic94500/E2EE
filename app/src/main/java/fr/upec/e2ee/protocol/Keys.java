package fr.upec.e2ee.protocol;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.ECGenParameterSpec;

import fr.upec.e2ee.Tools;

/**
 * Generate Key Pair
 */
public class Keys {
    /**
     * Generate a KeyPair with a SecureRandom to be stored in KeyStore
     *
     * @param alias Alias for KeyStore
     * @return Return KeyPair
     * @throws NoSuchAlgorithmException           Throws NoSuchAlgorithmException if there is not the expected algorithm
     * @throws InvalidAlgorithmParameterException InvalidAlgorithmParameterException if there is an invalid or inappropriate algorithm parameter
     */
    public static KeyPair generate(String alias) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
        keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                .setDigests(KeyProperties.DIGEST_SHA512)
                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                //.setUserAuthenticationRequired(true) //Deactivated for test
                //.setUserAuthenticationValidityDurationSeconds(5 * 60) //Deactivated for test
                .build(), Tools.generateSecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Get main key alias
     *
     * @return Return alias
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    public static String getMainKeyAlias() throws GeneralSecurityException, IOException {
        KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;
        return MasterKeys.getOrCreate(keyGenParameterSpec);
    }
}
