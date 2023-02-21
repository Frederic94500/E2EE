package fr.upec.e2ee.protocol;

import static java.util.Arrays.copyOfRange;
import static fr.upec.e2ee.Tools.toBase64;
import static fr.upec.e2ee.Tools.toBytes;
import static fr.upec.e2ee.Tools.toLong;
import static fr.upec.e2ee.Tools.toPublicKey;
import static fr.upec.e2ee.Tools.toSecretKey;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import fr.upec.e2ee.Tools;
import fr.upec.e2ee.mystate.MyDirectory;

/**
 * Create and Handle Messages
 */
public class Communication {
    /**
     * Create Message1 for the key negotiation/agreement
     *
     * @param message1 Message1 object
     * @return Return Message1 as Base64
     */
    public static String createMessage1(Message1 message1) {
        return toBase64(message1.toBytes());
    }

    /**
     * Handle the message 1 received from other
     *
     * @param myMessage1    My Message1
     * @param otherMessage1 Message 1 received from other
     * @return Return a SecretBuild
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static SecretBuild handleMessage1(Message1 myMessage1, String otherMessage1) throws GeneralSecurityException {
        byte[] otherMessage1Bytes = toBytes(otherMessage1);

        if (otherMessage1Bytes.length != 192) {
            throw new IllegalArgumentException("The other Message 1 is not the expected size!");
        }

        long otherTimestamp = toLong(otherMessage1Bytes, 0, 8);
        byte[] otherNonce = copyOfRange(otherMessage1Bytes, 8, 72);
        byte[] otherPubKeyByte = copyOfRange(otherMessage1Bytes, 72, 192);

        byte[] xor = new byte[64];
        byte[] myDigestNonce = Tools.digest(myMessage1.getNonce());
        byte[] otherDigestNonce = Tools.digest(otherNonce);
        for (int i = 0; i < 64; i++) {
            xor[i] = (byte) (myDigestNonce[i] ^ otherDigestNonce[i]);
        }

        PublicKey otherPubKey = toPublicKey(otherPubKeyByte);
        byte[] symKey = KeyExchange.createSharedKey(myMessage1.getPrivateKey(), otherPubKey, xor, "brenski").getEncoded();

        return new SecretBuild(myMessage1.getTimestamp(),
                otherTimestamp,
                myMessage1.getNonce(),
                otherNonce,
                myMessage1.getPublicKey().getEncoded(),
                otherPubKeyByte,
                symKey);
    }

    /**
     * Create message 2 by signing then ciphering
     *
     * @param myPrivateKey  Your Private Key
     * @param mySecretBuild Your SecretBuild
     * @return Return the signed and ciphered message 2 as Base64
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static String createMessage2(PrivateKey myPrivateKey, SecretBuild mySecretBuild) throws GeneralSecurityException {
        byte[] message2Base64 = mySecretBuild.toBytesWithoutSymKey();

        //Need to have an ID verification in Android
        byte[] signedMessage = Sign.sign(myPrivateKey, message2Base64);
        byte[] cipheredSignedMessage = Cipher.cipher(toSecretKey(mySecretBuild.getSymKey()), signedMessage);

        return toBase64(cipheredSignedMessage);
    }

    /**
     * Handle the message 2 received from other
     *
     * @param myDirectory   My Directory
     * @param mySecretBuild My SecretBuild
     * @param otherMessage2 Message 2 received by other
     * @return Return the name of the sender if the message 2 is authentic and come from the other user
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static Conversation handleMessage2(MyDirectory myDirectory, SecretBuild mySecretBuild, String otherMessage2) throws GeneralSecurityException {
        SecretBuild otherSecretBuild = new SecretBuild(mySecretBuild); //Swap information without symKey
        byte[] expectedMessage2 = otherSecretBuild.toBytesWithoutSymKey();

        byte[] cipheredSignedOtherMessage2 = toBytes(otherMessage2);
        byte[] signedMessage = Cipher.decipher(toSecretKey(mySecretBuild.getSymKey()), cipheredSignedOtherMessage2);

        String otherPersonName = myDirectory.getSigner(signedMessage, expectedMessage2); //null == invalid

        return new Conversation(otherPersonName, mySecretBuild.getMyDate(), mySecretBuild.getSymKey());
    }
}
