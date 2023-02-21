package fr.upec.e2ee.protocol;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * Sign for signing message
 */
public class Sign {
    /**
     * Sign a message using SHA512-ECDSA
     *
     * @param privateKey Your Private Key
     * @param input      Your Message
     * @return Return a signed message
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static byte[] sign(PrivateKey privateKey, byte[] input) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA512withECDSA"); //SHOULD BE SHA512withECDSAinP1363Format
        //Need to have an ID verification in Android
        signature.initSign(privateKey);

        signature.update(input);

        return signature.sign();
    }

    /**
     * Verify a signed message using SHA512-ECDSA
     *
     * @param publicKey       Other Public Key
     * @param signedMessage   The signed message
     * @param expectedMessage The expected message
     * @return Return a boolean if the message come from the other
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static Boolean verify(PublicKey publicKey, byte[] signedMessage, byte[] expectedMessage) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA512withECDSA"); //SHOULD BE SHA512withECDSAinP1363Format
        signature.initVerify(publicKey);

        signature.update(expectedMessage);

        return signature.verify(signedMessage);
    }
}
