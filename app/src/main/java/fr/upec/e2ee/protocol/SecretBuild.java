package fr.upec.e2ee.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

import fr.upec.e2ee.Tools;

/**
 * Object for SecretBuild
 * <pre>MUST BE HIDDEN!!! CONTAINS SENSITIVE INFORMATION!!!
 * long myDate = 8 bytes
 * long otherDate = 8 bytes
 * byte[64] myNonce = 64 bytes
 * byte[64] otherNonce = 64 bytes
 * byte[] myPubKey = 120 bytes
 * byte[] otherPubKey = 120 bytes
 * byte[] symKey = 32 bytes
 * SecretBuild total size = 416 bytes</pre>
 */
public class SecretBuild {
    private final long myDate;
    private final long otherDate;
    private final byte[] myNonce;
    private final byte[] otherNonce;
    private final byte[] myPubKey;
    private final byte[] otherPubKey;
    private final byte[] symKey;
    private String name;

    /**
     * SecretBuild Constructor
     *
     * @param myDate      My Date as UNIX Timestamp
     * @param otherDate   Other Date as UNIX Timestamp
     * @param myNonce     My Nonce (salt)
     * @param otherNonce  Other Nonce (salt)
     * @param myPubKey    My Public Key as Base64
     * @param otherPubKey My Public Key as Base64
     * @param symKey      Symmetric Key as Base64
     */
    public SecretBuild(long myDate, long otherDate, byte[] myNonce, byte[] otherNonce, byte[] myPubKey, byte[] otherPubKey, byte[] symKey) {
        this.myDate = myDate;
        this.otherDate = otherDate;
        this.myNonce = myNonce;
        this.otherNonce = otherNonce;
        this.myPubKey = myPubKey;
        this.otherPubKey = otherPubKey;
        this.symKey = symKey;
    }

    /**
     * Constructor to get the same SecretBuild of other
     *
     * @param mySecretBuild My SecretBuild
     */
    SecretBuild(SecretBuild mySecretBuild) {
        this.myDate = mySecretBuild.otherDate;
        this.otherDate = mySecretBuild.myDate;
        this.myNonce = mySecretBuild.otherNonce;
        this.otherNonce = mySecretBuild.myNonce;
        this.myPubKey = mySecretBuild.otherPubKey;
        this.otherPubKey = mySecretBuild.myPubKey;
        this.symKey = null;
    }

    /**
     * Constructor to load known information
     *
     * @param conversation Known information
     */
    public SecretBuild(String name, byte[] conversation) {
        this.myDate = Tools.toLong(conversation, 0, 8);
        this.otherDate = Tools.toLong(conversation, 8, 16);
        this.myNonce = Arrays.copyOfRange(conversation, 16, 80);
        this.otherNonce = Arrays.copyOfRange(conversation, 80, 144);
        this.myPubKey = Arrays.copyOfRange(conversation, 144, 264);
        this.otherPubKey = Arrays.copyOfRange(conversation, 264, 384);
        this.symKey = Arrays.copyOfRange(conversation, 384, 416);
        this.name = name;
    }

    /**
     * Compare between SecretBuild
     *
     * @param other Other SecretBuild
     * @return Return a boolean if is the same
     */
    public Boolean equals(SecretBuild other) {
        return this.myDate == other.otherDate &&
                this.otherDate == other.myDate && //Nonce can't be compared
                Arrays.equals(this.myPubKey, other.otherPubKey) &&
                Arrays.equals(this.otherPubKey, other.myPubKey) &&
                Arrays.equals(this.symKey, other.symKey);
    }

    /**
     * Encode SecretBuild without symmetric key
     *
     * @return Return SecretBuild as byte[]
     */
    public byte[] toBytesWithoutSymKey() {
        ByteBuffer buffer = ByteBuffer.allocate(384);
        buffer.putLong(myDate);
        buffer.putLong(otherDate);
        buffer.put(myNonce);
        buffer.put(otherNonce);
        buffer.put(myPubKey);
        buffer.put(otherPubKey);
        return buffer.array();
    }

    /**
     * Encode SecretBuild with symmetric key
     *
     * @return Return SecretBuild as byte[]
     */
    public byte[] toBytesWithSymKey() {
        ByteBuffer buffer = ByteBuffer.allocate(416);
        buffer.putLong(myDate);
        buffer.putLong(otherDate);
        buffer.put(myNonce);
        buffer.put(otherNonce);
        buffer.put(myPubKey);
        buffer.put(otherPubKey);
        assert symKey != null;
        buffer.put(symKey);
        return buffer.array();
    }

    /**
     * Get my UNIX timestamp
     *
     * @return Return my UNIX timestamp
     */
    public long getMyDate() {
        return myDate;
    }

    /**
     * Get other UNIX timestamp
     *
     * @return Return other UNIX timestamp
     */
    public long getOtherDate() {
        return otherDate;
    }

    /**
     * Get my nonce
     *
     * @return Return my nonce
     */
    public byte[] getMyNonce() {
        return myNonce;
    }

    /**
     * Get other nonce
     *
     * @return Return other nonce
     */
    public byte[] getOtherNonce() {
        return otherNonce;
    }

    /**
     * Get other Public Key
     *
     * @return Return other Public Key
     */
    public byte[] getOtherPubKey() {
        return otherPubKey;
    }

    /**
     * Get Symmetric Key
     *
     * @return Return Symmetric Key
     */
    public byte[] getSymKey() {
        return symKey;
    }

    /**
     * Get name of the user for the message
     *
     * @return Return name user
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the user for the message
     *
     * @param name Name of the user for the message
     */
    public void setName(String name) {
        this.name = name;
    }
}
