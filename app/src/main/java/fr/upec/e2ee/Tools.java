package fr.upec.e2ee;

import static java.util.Arrays.copyOfRange;

import android.content.Context;

import androidx.security.crypto.EncryptedFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import fr.upec.e2ee.protocol.Keys;

/**
 * Frequently used functions
 */
public class Tools {
    /**
     * Get the current time as UNIX Timestamp
     *
     * @return Return UNIX Timestamp
     */
    public static Long getCurrentTime() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Encode bytes to String Base64
     *
     * @param in Bytes
     * @return Return String as Base64
     */
    public static String toBase64(byte[] in) {
        return Base64.getEncoder().encodeToString(in);
    }

    /**
     * Decode String Base64 to bytes
     *
     * @param in String Base64
     * @return Return Bytes
     */
    public static byte[] toBytes(String in) {
        return Base64.getDecoder().decode(in);
    }

    /**
     * Decode Bytes to PublicKey
     *
     * @param bytesPubKey Bytes Public Key
     * @return Return PublicKey
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static PublicKey toPublicKey(byte[] bytesPubKey) throws GeneralSecurityException {
        return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(bytesPubKey));
    }

    /**
     * Decode Bytes to SecretKey
     *
     * @param secretKeyBytes SecretKey in byte[]
     * @return Return a SecretKey
     */
    public static SecretKey toSecretKey(byte[] secretKeyBytes) {
        return new SecretKeySpec(secretKeyBytes, "AES");
    }

    /**
     * Generate a SecureRandom using AES(256)
     *
     * @return Return a SecureRandom
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    public static SecureRandom generateSecureRandom() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return new SecureRandom(keyGenerator.generateKey().getEncoded());
    }

    /**
     * Generate Random Bytes
     *
     * @param size Size of the list
     * @return Return Random Bytes in list
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    public static byte[] generateRandomBytes(int size) throws NoSuchAlgorithmException {
        return generateSecureRandom().generateSeed(size);
    }

    /**
     * Verify if the Public Key is an EC key
     *
     * @param pubKey EC Public Key
     * @return Return if is EC Key
     */
    public static boolean isECPubKey(byte[] pubKey) {
        try {
            toPublicKey(pubKey);
        } catch (GeneralSecurityException e) {
            if (new String(pubKey).equals("0")) {
                return false;
            }
            System.out.println("Not a Public Key!");
            return false;
        }
        return true;
    }

    /**
     * Transform Bytes to Long from byte[]
     *
     * @param tab  byte[] source
     * @param from Start index
     * @param to   End index
     * @return Return a Long
     */
    public static Long toLong(byte[] tab, int from, int to) {
        ByteBuffer bb = ByteBuffer.wrap(copyOfRange(tab, from, to));
        return bb.getLong();
    }

    /**
     * Transform Bytes to Long
     *
     * @param input byte[] input
     * @return Return a Long
     */
    public static Long bytesToLong(byte[] input) {
        return toLong(input, 0, 8);
    }

    /**
     * Convert Long to Bytes
     *
     * @param input Long input
     * @return Return byte[]
     */
    public static byte[] longToByteArray(long input) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        return byteBuffer.putLong(input).array();
    }

    /**
     * Check if a file exists
     *
     * @param filename File to check
     * @return Return a boolean if the file exists
     */
    public static boolean isFileExists(String filename) {
        Context context = E2EE.getContext();
        return new File(context.getFilesDir(), filename).exists();
    }

    /**
     * Create a file
     *
     * @param filename File to create
     */
    public static void createFile(String filename) throws IOException {
        Context context = E2EE.getContext();
        new File(context.getFilesDir(), filename).createNewFile();
    }

    /**
     * Delete a file
     *
     * @param filename File to be deleted
     */
    public static void deleteFile(String filename) {
        Context context = E2EE.getContext();
        new File(context.getFilesDir(), filename).delete();
    }

    /**
     * Compute the checksum of a file
     *
     * @param filename File to get the digest
     * @return Return an SHA-512 Checksum
     * @throws IOException              Throws IOException if there is an I/O exception
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    public static String digest(String filename) throws IOException, NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-512").digest(Tools.readFile(filename));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Hash input
     *
     * @param input Input
     * @return Return hashed input
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    public static byte[] digest(byte[] input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-512").digest(input);
    }


    /**
     * Get user input using nextLine() method
     *
     * @param sentence Sentence for the input
     * @return Return user input
     */
    public static String getInput(String sentence) {
        System.out.print(sentence);
        Scanner scanner = new Scanner(System.in);

        String input = scanner.nextLine();
        if (input.equals("0")) {
            return "0";
        }
        return input;
    }

    /**
     * Parser for the Public Key from PEM format
     *
     * @param keyPem Public Key
     * @return Return parsed Public Key
     */
    public static String keyParser(String keyPem) {
        if (keyPem.contains("----BEGIN PUBLIC KEY-----")) {
            String[] tokens = keyPem.split("-----");
            for (String token : tokens) {
                try {
                    if (Tools.toBytes(token).length == 91) {
                        return token;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        } else if (Tools.toBytes(keyPem).length == 91) {
            return keyPem;
        }
        throw new IllegalArgumentException("Can't find public key!");
    }

    /**
     * Read a file
     *
     * @param filename Filename
     * @return Return Bytes from file
     * @throws IOException Throws IOException if there is an I/O exception
     */
    public static byte[] readFile(String filename) throws IOException {
        Context context = E2EE.getContext();
        return Files.readAllBytes(new File(context.getFilesDir(), filename).toPath());
    }

    /**
     * Get EncryptFile for read and write encrypted file
     *
     * @param filename Filename
     * @return Return EncryptFile object
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    private static EncryptedFile getEncryptedFile(String filename) throws GeneralSecurityException, IOException {
        Context context = E2EE.getContext();
        File file = new File(context.getFilesDir(), filename);
        String mainKeyAlias = Keys.getMainKeyAlias();
        return new EncryptedFile.Builder(
                file,
                context,
                mainKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();
    }

    /**
     * Write an encrypted file
     *
     * @param filename Filename
     * @param data     Data to encrypt
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    public static void writeEncryptFile(String filename, byte[] data) throws GeneralSecurityException, IOException {
        deleteFile(filename);
        OutputStream outputStream = getEncryptedFile(filename).openFileOutput();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Read an encrypted file
     *
     * @param filename Filename
     * @return Return data
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     * @throws IOException              Throws IOException if there is an I/O exception
     */
    public static byte[] readEncryptedFile(String filename) throws GeneralSecurityException, IOException {
        InputStream inputStream = getEncryptedFile(filename).openFileInput();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int nextByte = inputStream.read();
        while (nextByte != -1) {
            byteArrayOutputStream.write(nextByte);
            nextByte = inputStream.read();
        }

        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}
