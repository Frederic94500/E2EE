package fr.upec.e2ee.mystate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import fr.upec.e2ee.Tools;
import fr.upec.e2ee.protocol.SecretBuild;

/**
 * Contain user state
 * <pre>MUST BE HIDDEN!!! CONTAINS SENSITIVE INFORMATION!!!</pre>
 */
public class MyState {
    /**
     * Filename
     */
    public static final String FILENAME = ".MyState";
    private final MyDirectory myDirectory;
    private final MyConversations myConversations;
    private final String hashedPassword;
    private MyKeyPair myKeyPair;
    private int myNonce;

    /**
     * Create MyState for unit test
     *
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public MyState(String alias, boolean unitTest) throws GeneralSecurityException {
        this.myKeyPair = new MyKeyPair(alias);
        this.myDirectory = new MyDirectory();
        this.myConversations = new MyConversations();
        this.myNonce = 0;
        this.hashedPassword = Tools.hashPassword("1234");
    }

    /**
     * Create MyState if it is the first start
     *
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public MyState(String hashedPassword) throws GeneralSecurityException {
        this.myKeyPair = new MyKeyPair();
        this.myDirectory = new MyDirectory();
        this.myConversations = new MyConversations();
        this.myNonce = 0;
        this.hashedPassword = hashedPassword;
    }

    /**
     * Create MyState from known information
     *
     * @param myKeyPair       MyKeyPair
     * @param myDirectory     MyDirectory
     * @param myConversations MyConversations
     * @param myNonce         MyNonce
     */
    public MyState(MyKeyPair myKeyPair, MyDirectory myDirectory, MyConversations myConversations, int myNonce, String hashedPassword) {
        this.myKeyPair = myKeyPair;
        this.myDirectory = myDirectory;
        this.myConversations = myConversations;
        this.myNonce = myNonce;
        this.hashedPassword = hashedPassword;
    }

    /**
     * Load or create a MyState
     *
     * @return Return MyState
     * @throws IOException              Throws IOException if there is an I/O exception
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static MyState load(String hashedPassword, SecretKey secretKey) throws IOException, GeneralSecurityException {
        if (Tools.isFileExists(FILENAME)) {
            String data = new String(Tools.readFile(MyState.FILENAME));
            String[] rawData = data.split(",");
            if (isEqualsDigest(rawData)) {
                return new MyState(MyKeyPair.load("fr.upec.e2ee.keypair"),
                        new MyDirectory(secretKey),
                        new MyConversations(secretKey),
                        ByteBuffer.wrap(Tools.toBytes(rawData[2])).getInt(),
                        hashedPassword);
            } else {
                throw new IllegalStateException("""
                        WARNING!!! YOUR FILES HAS BEEN COMPROMISED!
                        PLEASE ERASE .MyState, .MyKeyPair, .MyDirectory AND .MyConversations!!!""");
            }
        } else {
            MyState myState = new MyState(hashedPassword);
            myState.save();
            return myState;
        }
    }

    /**
     * Verify if the digest of .MyKeyPair, .MyDirectory and .MyConversations is the same
     *
     * @param rawData Data from .MyState
     * @return Return a Boolean
     * @throws IOException              Throws IOException if there is an I/O exception
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    private static boolean isEqualsDigest(String[] rawData) throws IOException, NoSuchAlgorithmException {
        return rawData[0].equals(Tools.digest(MyDirectory.FILENAME))
                && rawData[1].equals(Tools.digest(MyConversations.FILENAME));
    }


    /**
     * Get my Public Key from MyKeyPair
     *
     * @return Return my Public Key
     */
    public PublicKey getMyPublicKey() {
        return myKeyPair.getMyPublicKey();
    }

    /**
     * Get my Private Key from MyKeyPair
     *
     * @return Return my Private Key
     */
    public PrivateKey getMyPrivateKey() {
        return myKeyPair.getMyPrivateKey();
    }

    /**
     * Get all conversations
     *
     * @return Return ArrayList MyConversations
     */
    public MyConversations getMyConversations() {
        return myConversations;
    }

    /**
     * Get MyDirectory
     *
     * @return Return MyDirectory
     */
    public MyDirectory getMyDirectory() {
        return myDirectory;
    }

    /**
     * Get my nonce
     *
     * @return int MyNonce
     */
    public int getMyNonce() {
        return myNonce;
    }

    /**
     * Increment myNonce
     *
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    public void incrementMyNonce() throws NoSuchAlgorithmException {
        int temp;
        do {
            temp = Tools.generateSecureRandom().nextInt(100);
        } while (temp == 0);
        this.myNonce += temp;
    }

    /**
     * Save MyState in a file
     * <pre>Contain digest .MyKeyPair, digest .MyDirectory, digest .MyConversations, Base64 myNonce</pre>
     *
     * @throws IOException              Throws IOException if there is an I/O exception
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public void save() throws IOException, GeneralSecurityException {
        byte[] salt = Tools.generateRandomBytes(32);
        SecretKey secretKey = Tools.getSecretKeyPBKDF2(hashedPassword.toCharArray(), salt);

        myDirectory.saveFile(secretKey);
        myConversations.save(secretKey);

        String checksumMyDirectory = Tools.digest(MyDirectory.FILENAME);
        String checksumMyConversations = Tools.digest(MyConversations.FILENAME);
        String myNonceBase64 = Tools.toBase64(ByteBuffer.allocate(4).putInt(myNonce).array());
        String saltBase64 = Tools.toBase64(salt);

        Tools.writeToFile(MyState.FILENAME, (checksumMyDirectory + "," + checksumMyConversations + "," + myNonceBase64 + "," + saltBase64).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Add a new conversation to the list of conversations
     *
     * @param secretBuild SecretBuild from the new conversation
     */
    public void addAConversation(SecretBuild secretBuild) {
        myConversations.addConversation(secretBuild);
    }

    /**
     * Get the size of conversations
     *
     * @return Return the size of conversations
     */
    public int getConversationSize() {
        return myConversations.getSize();
    }

    /**
     * Replace MyKeyPair by a new one and save the new one
     *
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    public void replaceMyKeyPair() throws GeneralSecurityException, IOException {
        this.myKeyPair = new MyKeyPair();
        this.save();
    }
}
