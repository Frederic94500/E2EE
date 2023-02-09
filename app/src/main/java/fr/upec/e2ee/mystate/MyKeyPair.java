package fr.upec.e2ee.mystate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

import fr.upec.e2ee.protocol.Keys;

/**
 * Store the PublicKey and the PrivateKey of the user
 * <pre>MUST BE HIDDEN!!! CONTAINS SENSITIVE INFORMATION!!!</pre>
 */
public class MyKeyPair {
    private final PublicKey myPublicKey;
    private final PrivateKey myPrivateKey;

    /**
     * Constructor of MyKeyPair if file does not exist
     *
     * @throws InvalidAlgorithmParameterException InvalidAlgorithmParameterException if there is an invalid or inappropriate algorithm parameter
     * @throws NoSuchAlgorithmException           Throws NoSuchAlgorithmException if there is not the expected algorithm
     * @throws NoSuchProviderException            Throws NoSuchProviderException if a security provider is requested but is not available in the environment
     */
    public MyKeyPair(String alias) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = Keys.generate(alias);
        this.myPublicKey = keyPair.getPublic();
        this.myPrivateKey = keyPair.getPrivate();
    }

    /**
     * Constructor of MyKeyPair from known information
     *
     * @param privateKey Private key
     * @param publicKey  Public Key
     */
    private MyKeyPair(PrivateKey privateKey, PublicKey publicKey) {
        this.myPrivateKey = privateKey;
        this.myPublicKey = publicKey;
    }

    /**
     * Load .MyKeyPair or generate a new one and return a MyKeyPair
     *
     * @return Return a MyKeyPair
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    public static MyKeyPair load(String alias) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (keyStore.containsAlias(alias)) {
            KeyStore.Entry entry = keyStore.getEntry(alias, null);
            return new MyKeyPair(((KeyStore.PrivateKeyEntry) entry).getPrivateKey(), ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey());
        } else {
            return new MyKeyPair(alias);
        }
    }

    /**
     * Get myPublicKey
     *
     * @return Return PublicKey
     */
    public PublicKey getMyPublicKey() {
        return myPublicKey;
    }

    /**
     * Get myPrivateKey
     *
     * @return Return PrivateKey
     */
    public PrivateKey getMyPrivateKey() {
        return myPrivateKey;
    }
}
