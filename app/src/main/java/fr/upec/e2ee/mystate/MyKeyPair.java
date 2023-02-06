package fr.upec.e2ee.mystate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import fr.upec.e2ee.protocol.Cipher;
import fr.upec.e2ee.protocol.Keys;
import fr.upec.e2ee.Tools;

/**
 * Store the PublicKey and the PrivateKey of the user
 * <pre>MUST BE HIDDEN!!! CONTAINS SENSITIVE INFORMATION!!!</pre>
 */
public class MyKeyPair {
    /**
     * Filename
     */
    public static final String FILENAME = ".MyKeyPair";
    private final PublicKey myPublicKey;
    private final PrivateKey myPrivateKey;

    /**
     * Constructor of MyKeyPair if file does not exist
     *
     * @throws InvalidAlgorithmParameterException InvalidAlgorithmParameterException if there is an invalid or inappropriate algorithm parameter
     * @throws NoSuchAlgorithmException           Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    public MyKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPair keyPair = Keys.generate();
        this.myPublicKey = keyPair.getPublic();
        this.myPrivateKey = keyPair.getPrivate();
    }

    /**
     * Constructor of MyKeyPair if file exist
     *
     * @param myPublicKeyBytes  Public Key in byte[]
     * @param myPrivateKeyBytes Private Key in byte[]
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    private MyKeyPair(byte[] myPublicKeyBytes, byte[] myPrivateKeyBytes) throws GeneralSecurityException {
        this.myPublicKey = Tools.toPublicKey(myPublicKeyBytes);
        this.myPrivateKey = Tools.toPrivateKey(myPrivateKeyBytes);
    }

    /**
     * Load .MyKeyPair or generate a new one and return a MyKeyPair
     *
     * @return Return a MyKeyPair
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    public static MyKeyPair load(SecretKey secretKey) throws GeneralSecurityException, IOException {
        if (Tools.isFileExists(FILENAME)) {
            String output = new String(Cipher.decipher(secretKey, Tools.readFile(FILENAME)));
            String[] dataBase64 = output.split(",");
            return new MyKeyPair(Tools.toBytes(dataBase64[0]), Tools.toBytes(dataBase64[1]));
        } else {
            return new MyKeyPair();
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

    /**
     * Save MyKeyPair
     *
     * @throws IOException Throws IOException if there is an I/O exception
     */
    public void save(SecretKey secretKey) throws IOException, GeneralSecurityException {
        String myPublicKeyBase64 = Tools.toBase64(myPublicKey.getEncoded());
        String myPrivateKeyBase64 = Tools.toBase64(myPrivateKey.getEncoded());
        String input = myPublicKeyBase64 + "," + myPrivateKeyBase64;

        byte[] cipheredOutput = Cipher.cipher(secretKey, input.getBytes(StandardCharsets.UTF_8));

        Tools.writeToFile(FILENAME, cipheredOutput);
    }
}
