package fr.upec.e2ee.protocol;

import android.os.Build;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import fr.upec.e2ee.protocol.Der.DerInputStream;
import fr.upec.e2ee.protocol.Der.DerOutputStream;
import fr.upec.e2ee.protocol.Der.DerValue;

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
        Signature signature = Signature.getInstance("SHA512withECDSA");
        //Need to have an ID verification in Android
        signature.initSign(privateKey);

        signature.update(input);

        return decodeSignature(signature.sign());
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
        byte[] toVerify = encodeSignature(signedMessage);

        Signature signature = Signature.getInstance("SHA512withECDSA");
        signature.initVerify(publicKey);

        signature.update(expectedMessage);

        return signature.verify(toVerify);
    }

    /**
     * Trim leading (most significant) zeroes from the result
     * <pre>This part come from <a href="https://github.com/AdoptOpenJDK/openjdk-jdk11/blob/master/src/jdk.crypto.ec/share/classes/sun/security/ec/ECDSASignature.java#L510">AdoptOpenJDK</a></pre>
     *
     * @param b Input bytes
     * @return Return trimmed bytes
     */
    private static byte[] trimZeroes(byte[] b) {
        int i = 0;
        while ((i < b.length - 1) && (b[i] == 0)) {
            i++;
        }
        if (i == 0) {
            return b;
        }
        byte[] t = new byte[b.length - i];
        System.arraycopy(b, i, t, 0, t.length);
        return t;
    }

    /**
     * Convert the concatenation of R and S into their DER encoding
     * <pre>This part come from <a href="https://github.com/AdoptOpenJDK/openjdk-jdk11/blob/master/src/jdk.crypto.ec/share/classes/sun/security/ec/ECDSASignature.java#L451">AdoptOpenJDK</a></pre>
     *
     * @param signature Signature as R and S
     * @return Return signature as DER
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    private static byte[] encodeSignature(byte[] signature) throws GeneralSecurityException {
        try {
            int n = signature.length >> 1;
            byte[] bytes = new byte[n];
            System.arraycopy(signature, 0, bytes, 0, n);
            BigInteger r = new BigInteger(1, bytes);
            System.arraycopy(signature, n, bytes, 0, n);
            BigInteger s = new BigInteger(1, bytes);

            DerOutputStream out = new DerOutputStream(signature.length + 10);
            out.putInteger(r);
            out.putInteger(s);
            DerValue result =
                    new DerValue(DerValue.tag_Sequence, out.toByteArray());

            return result.toByteArray();

        } catch (Exception e) {
            throw new SignatureException("Could not encode signature", e);
        }
    }

    /**
     * Convert the DER encoding of R and S into a concatenation of R and S
     * <pre>This part come from <a href="https://github.com/AdoptOpenJDK/openjdk-jdk11/blob/master/src/jdk.crypto.ec/share/classes/sun/security/ec/ECDSASignature.java#L476">AdoptOpenJDK</a></pre>
     *
     * @param sig Signature as DER
     * @return Return signature as R and S
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    private static byte[] decodeSignature(byte[] sig) throws GeneralSecurityException {
        try {
            // Enforce strict DER checking for signatures
            DerInputStream in = new DerInputStream(sig, 0, sig.length, false);
            DerValue[] values = in.getSequence(2);

            // check number of components in the read sequence
            // and trailing data
            if ((values.length != 2) || (in.available() != 0)) {
                throw new IOException("Invalid encoding for signature");
            }

            byte[] rBytes;
            byte[] sBytes;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                BigInteger r = values[0].getPositiveBigInteger();
                BigInteger s = values[1].getPositiveBigInteger();
                // trim leading zeroes
                rBytes = trimZeroes(r.toByteArray());
                sBytes = trimZeroes(s.toByteArray());
            } else {
                fr.upec.e2ee.protocol.math.BigInteger r = values[0].getPositiveBigInteger2();
                fr.upec.e2ee.protocol.math.BigInteger s = values[0].getPositiveBigInteger2();
                // trim leading zeroes
                rBytes = trimZeroes(r.toByteArray());
                sBytes = trimZeroes(s.toByteArray());
            }


            int k = Math.max(rBytes.length, sBytes.length);
            // r and s each occupy half the array
            byte[] result = new byte[k << 1];
            System.arraycopy(rBytes, 0, result, k - rBytes.length,
                    rBytes.length);
            System.arraycopy(sBytes, 0, result, result.length - sBytes.length,
                    sBytes.length);
            return result;

        } catch (Exception e) {
            throw new SignatureException("Invalid encoding for signature", e);
        }
    }
}
