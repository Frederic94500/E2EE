package fr.upec.e2ee.mystate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import fr.upec.e2ee.Tools;
import fr.upec.e2ee.protocol.Conversation;

/**
 * Contain user state
 * <pre>MUST BE HIDDEN!!! CONTAINS SENSITIVE INFORMATION!!!</pre>
 */
public class MyState {
    /**
     * Filename
     */
    public static final String FILENAME = ".MyState";
    /**
     * Default alias for KetStore
     */
    public static final String DEFAULT_ALIAS = "fr.upec.e2ee.keypair";
    private final MyDirectory myDirectory;
    private final MyConversations myConversations;
    private MyKeyPair myKeyPair;
    private int myNonce;

    /**
     * Create MyState for unit test
     *
     * @param alias Alias for KeyStore
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public MyState(String alias) throws GeneralSecurityException, IOException {
        this.myKeyPair = new MyKeyPair(alias);
        this.myDirectory = new MyDirectory();
        this.myConversations = new MyConversations();
        this.myNonce = 0;
    }

    /**
     * Create MyState from known information
     *
     * @param myKeyPair       MyKeyPair
     * @param myDirectory     MyDirectory
     * @param myConversations MyConversations
     * @param myNonce         MyNonce
     */
    public MyState(MyKeyPair myKeyPair, MyDirectory myDirectory, MyConversations myConversations, int myNonce) {
        this.myKeyPair = myKeyPair;
        this.myDirectory = myDirectory;
        this.myConversations = myConversations;
        this.myNonce = myNonce;
    }

    /**
     * Load or create a MyState
     *
     * @param alias Alias for KeyStore
     * @return Return MyState
     * @throws IOException              Throws IOException if there is an I/O exception
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static MyState load(String alias) throws IOException, GeneralSecurityException {
        if (Tools.isFileExists(FILENAME)) {
            String data = new String(Tools.readEncryptedFile(MyState.FILENAME));
            String[] rawData = data.split(",");
            if (isEqualsDigest(rawData)) {
                return new MyState(MyKeyPair.load(alias),
                        new MyDirectory(),
                        new MyConversations(),
                        ByteBuffer.wrap(Tools.toBytes(rawData[2])).getInt());
            } else {
                throw new IllegalStateException("""
                        WARNING!!! YOUR FILES HAS BEEN COMPROMISED!
                        PLEASE ERASE .MyState, .MyKeyPair, .MyDirectory AND .MyConversations!!!""");
            }
        } else {
            MyState myState = new MyState(alias);
            myState.save();
            return myState;
        }
    }

    /**
     * Load or create a MyState with the default alias
     *
     * @return Return MyState
     * @throws IOException              Throws IOException if there is an I/O exception
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static MyState load() throws GeneralSecurityException, IOException {
        return load(DEFAULT_ALIAS);
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
     * <pre>Contain digest .MyDirectory, digest .MyConversations, Base64 myNonce</pre>
     *
     * @throws IOException              Throws IOException if there is an I/O exception
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public void save() throws IOException, GeneralSecurityException {
        myDirectory.saveFile();
        myConversations.save();

        String checksumMyDirectory = Tools.digest(MyDirectory.FILENAME);
        String checksumMyConversations = Tools.digest(MyConversations.FILENAME);
        String myNonceBase64 = Tools.toBase64(ByteBuffer.allocate(4).putInt(myNonce).array());

        Tools.writeEncryptFile(MyState.FILENAME, (checksumMyDirectory + "," + checksumMyConversations + "," + myNonceBase64).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Add a new conversation to the list of conversations
     *
     * @param conversation SecretBuild from the new conversation
     */
    public void addAConversation(Conversation conversation) {
        myConversations.addConversation(conversation);
    }

    /**
     * Replace MyKeyPair by a new one and save the new one. Alias by default: fr.upec.e2ee.keypair
     *
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    public void replaceMyKeyPair() throws GeneralSecurityException, IOException {
        this.myKeyPair = new MyKeyPair(DEFAULT_ALIAS);
        this.save();
    }

    /**
     * Replace MyKeyPair by a new one and save the new one.
     *
     * @param alias Alias for KeyStore
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    public void replaceMyKeyPair(String alias) throws GeneralSecurityException, IOException {
        this.myKeyPair = new MyKeyPair(alias);
        this.save();
    }
}
