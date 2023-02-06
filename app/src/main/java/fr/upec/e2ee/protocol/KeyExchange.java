package fr.upec.e2ee.protocol;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * KeyExchange for the key negotiation/agreement
 */
public class KeyExchange {
    /**
     * HMAC Key Derivation Function (HKDF) Extractor
     * <pre>See <a href="https://www.rfc-editor.org/rfc/rfc5869">RFC5869</a></pre>
     *
     * @param salt Salt value (a non-secret random value)
     * @param ikm  Input Keying Material
     * @return Return a PseudoRandom Key
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     * @throws InvalidKeyException      Throws InvalidKeyException if there is an invalid key
     */
    public static byte[] hkdfExtract(byte[] salt, byte[] ikm) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey secretKey = new SecretKeySpec(salt, "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(secretKey);
        return mac.doFinal(ikm);
    }


    /**
     * HMAC Key Derivation Function (HKDF) Expand
     * <pre>Based on the library HKDF by Patrick Favre-Bulle in <a href="https://github.com/patrickfav/hkdf">GitHub</a>
     * See <a href="https://www.rfc-editor.org/rfc/rfc5869">RFC5869</a></pre>
     *
     * @param prk  PseudoRandom Key
     * @param info An information
     * @param olb  Out Length Bytes
     * @return Return a HKDF
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     * @throws InvalidKeyException      Throws InvalidKeyException if there is an invalid key
     */
    public static byte[] hkdfExpand(byte[] prk, String info, int olb) throws NoSuchAlgorithmException, InvalidKeyException {
        if (olb <= 0 || prk == null) {
            throw new IllegalArgumentException();
        }

        SecretKey secretKey = new SecretKeySpec(prk, "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(secretKey);

        byte[] blockN = new byte[0];

        int iterations = (int) Math.ceil(((double) olb) / ((double) mac.getMacLength()));
        if (iterations > 255) {
            throw new IllegalArgumentException();
        }

        byte[] infoBytes = info.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(olb);
        int remainingBytes = olb;
        int stepSize;

        for (int i = 0; i < iterations; i++) {
            mac.update(blockN);
            mac.update(infoBytes);
            mac.update((byte) (i + 1));

            blockN = mac.doFinal();

            stepSize = Math.min(remainingBytes, blockN.length);

            buffer.put(blockN, 0, stepSize);
            remainingBytes -= stepSize;
        }

        return buffer.array();
    }

    /**
     * Create a shared key for the key negotiation/agreement using ECDH+HKDF(SHA512)
     *
     * @param privateKey     Your Private Key
     * @param publicKeyOther Public Key of the other person
     * @param salt           Salt (xor)
     * @param info           Info
     * @return Return the shared key
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     * @throws InvalidKeyException      Throws InvalidKeyException if there is an invalid key
     */
    public static SecretKey createSharedKey(PrivateKey privateKey, PublicKey publicKeyOther, byte[] salt, String info) throws NoSuchAlgorithmException, InvalidKeyException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKeyOther, true);
        byte[] keyAgreed = keyAgreement.generateSecret();
        SecretKey symKey = new SecretKeySpec(keyAgreed, "ECDH");

        byte[] hkdfExtract = hkdfExtract(salt, symKey.getEncoded());
        byte[] hkdfExpand = hkdfExpand(hkdfExtract, info, 32);

        return new SecretKeySpec(hkdfExpand, "AES");
    }
}
