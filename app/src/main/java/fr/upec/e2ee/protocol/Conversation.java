package fr.upec.e2ee.protocol;

/**
 * A Conversation
 */
public class Conversation {
    private final String name;
    private final long date;
    private final byte[] secretKey;

    /**
     * Constructor of Conversation
     *
     * @param name      Name of the other person
     * @param date      Date of the conversation
     * @param secretKey SecretKey for the conversation
     */
    public Conversation(String name, long date, byte[] secretKey) {
        this.name = name;
        this.date = date;
        this.secretKey = secretKey;
    }

    /**
     * Get the name of the person
     *
     * @return Return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the SecretKey for the conversation
     *
     * @return Return the SecretKey
     */
    public byte[] getSecretKey() {
        return secretKey;
    }

    /**
     * Get the date of the conversation
     *
     * @return Return the date
     */
    public long getDate() {
        return date;
    }
}
