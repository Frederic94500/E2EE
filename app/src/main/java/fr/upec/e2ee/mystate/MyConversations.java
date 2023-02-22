package fr.upec.e2ee.mystate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import fr.upec.e2ee.Tools;
import fr.upec.e2ee.protocol.Conversation;

/**
 * MyConversations contains SecretBuild for each conversation
 * <pre>MUST BE HIDDEN!!! CONTAINS SENSITIVE INFORMATION!!!</pre>
 */
public class MyConversations {
    /**
     * Filename
     */
    public static final String FILENAME = ".MyConversations";
    private final List<Conversation> myConversations;

    public MyConversations() {
        this.myConversations = new ArrayList<>();
    }

    /**
     * Constructor for MyConversations
     *
     * @throws FileNotFoundException Throws FileNotFoundException if the file was not found
     */
    public MyConversations(SecretKey secretKey) throws IOException, GeneralSecurityException {
        this.myConversations = load(secretKey);
    }

    /**
     * Load .MyConversations and create an ArrayList
     *
     * @return Return an ArrayList of SecretBuild
     * @throws FileNotFoundException Throws FileNotFoundException if the file was not found
     */
    public List<Conversation> load(SecretKey secretKey) throws IOException, GeneralSecurityException {
        ArrayList<Conversation> myConversations = new ArrayList<>();
        if (Tools.isFileExists(FILENAME)) {
            byte[] cipheredData = Tools.readFile(FILENAME);
            if (cipheredData.length != 0) {
                //byte[] rawData = Cipher.decipher(secretKey, cipheredData);

                String[] rawConversations = new String(cipheredData).split(",");
                for (String rawConversation : rawConversations) {
                    String[] splitConversation = rawConversation.split(":");
                    myConversations.add(new Conversation(new String(Tools.toBytes(splitConversation[0])),
                            Tools.bytesToLong(Tools.toBytes(splitConversation[1])),
                            Tools.toBytes(splitConversation[2])));
                }
            }
        }
        return myConversations;
    }

    /**
     * Save MyConversations to .MyConversations
     *
     * @throws IOException Throws IOException if there is an I/O exception
     */
    public void save(SecretKey secretKey) throws IOException, GeneralSecurityException {
        String rawConversations = myConversations.stream()
                .map(conversation -> Tools.toBase64(conversation.getName().getBytes(StandardCharsets.UTF_8)) + ":" +
                        Tools.toBase64(Tools.longToByteArray(conversation.getDate())) + ":" +
                        Tools.toBase64(conversation.getSecretKey()))
                .collect(Collectors.joining(","));

        if (myConversations.size() > 0) {
            /*byte[] cipheredOutput = Cipher.cipher(secretKey, rawConversations.getBytes(StandardCharsets.UTF_8));
            Tools.writeToFile(FILENAME, cipheredOutput);*/
            Tools.writeToFile(FILENAME, rawConversations.getBytes(StandardCharsets.UTF_8));
        } else {
            Tools.deleteFile(FILENAME);
            Tools.createFile(FILENAME);
        }
    }

    /**
     * Get size of the list of conversations
     *
     * @return Return the size of the list of conversations
     */
    public int getSize() {
        return myConversations.size();
    }

    /**
     * Add a new conversation to the list of conversations
     *
     * @param conversation Conversation to be added
     */
    public void addConversation(Conversation conversation) {
        myConversations.add(conversation);
    }

    /**
     * Get a conversation
     *
     * @param index Index of the conversation
     * @return Return a conversation (as SecretBuild)
     */
    public Conversation getConversation(int index) {
        return myConversations.get(index);
    }

    /**
     * Delete a conversation
     *
     * @param conversation Conversation (as SecretBuild) to be deleted
     */
    public void deleteConversation(Conversation conversation) {
        myConversations.remove(conversation);
    }
}
