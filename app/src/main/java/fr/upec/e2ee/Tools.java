package fr.upec.e2ee;

import static java.util.Arrays.copyOfRange;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import fr.upec.e2ee.mystate.MyDirectory;
import fr.upec.e2ee.mystate.MyState;
import fr.upec.e2ee.protocol.Cipher;

/**
 * Frequently used functions
 */
public class Tools {
    /**
     * PBKDF2 Number of iteration
     */
    public static final int PBKDF2_ITERATION = 1048576;

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
     * Get Secret Key with PBKDF2
     *
     * @param password   Password
     * @param salt       Salt
     * @param iterations Iterations for PBKDF2
     * @return Return SecretKey AES
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static SecretKey getSecretKeyPBKDF2(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, iterations, 256);
        SecretKey temp = keyFactory.generateSecret(pbeKeySpec);
        return new SecretKeySpec(temp.getEncoded(), "AES");
    }

    /**
     * Get Secret Key with PBKDF2
     *
     * @param password Password
     * @param salt     Salt
     * @return Return SecretKey AES
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static SecretKey getSecretKeyPBKDF2(char[] password, byte[] salt) throws GeneralSecurityException {
        return getSecretKeyPBKDF2(password, salt, PBKDF2_ITERATION);
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
     * Hashing the password
     *
     * @param password Password
     * @return Return a hashed password
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-512").digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Create a new SecretKey PBKDF2 for the first time open
     *
     * @return Return SecretKey PBKDF2
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static SecretKey createSecretKey(String hashedPassword) throws GeneralSecurityException {
        byte[] salt = generateRandomBytes(32);
        return getSecretKeyPBKDF2(hashedPassword.toCharArray(), salt);
    }

    /**
     * Confirm the password
     *
     * @return Return a hashed password
     * @throws NoSuchAlgorithmException Throws NoSuchAlgorithmException if there is not the expected algorithm
     */
    public static String getConfirmPassword() throws NoSuchAlgorithmException {
        String password;
        String confirmPassword;
        do {
            password = getInput("Create the password (0 = quit): ");
            if (password.equals("0")) {
                System.exit(0);
            }
            confirmPassword = getInput("Confirm the password (0 = quit): ");
            if (confirmPassword.equals("0")) {
                System.exit(0);
            }
            if (!password.equals(confirmPassword)) {
                System.out.println("Not the same password!");
            }
        } while (!password.equals(confirmPassword));
        return hashPassword(password);
    }

    /**
     * Load a SecretKey PBKDF2
     *
     * @return Return SecretKey PBKDF2
     * @throws FileNotFoundException    Throws FileNotFoundException if there file is not found
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static SecretKey loadSecretKey(String hashedPassword) throws IOException, GeneralSecurityException {
        String data = new String(readFile(MyState.FILENAME));
        String[] rawData = data.split(",");

        return getSecretKeyPBKDF2(hashedPassword.toCharArray(), Tools.toBytes(rawData[3]));
    }

    /**
     * Get the correct password and SecretKey
     *
     * @return Return a hashed password and SecretKey
     * @throws FileNotFoundException    Throws FileNotFoundException if there file is not found
     * @throws GeneralSecurityException Throws GeneralSecurityException if there is a security-related exception
     */
    public static Map.Entry<String, SecretKey> getPassAndSecret() throws GeneralSecurityException, IOException {
        HashMap<String, SecretKey> output = new HashMap<>();
        Scanner scanner = new Scanner(new File(MyState.FILENAME));
        String data = scanner.nextLine();
        String[] rawData = data.split(",");

        String hashedPassword;
        SecretKey secretKey;
        boolean cli = true;
        do {
            hashedPassword = hashPassword(getInput("Type your password (0 = quit): "));
            if (hashedPassword.equals(hashPassword("0"))) {
                System.exit(0);
            }
            secretKey = getSecretKeyPBKDF2(hashedPassword.toCharArray(), Tools.toBytes(rawData[4]));
            if (testSecretKey(secretKey)) {
                output.put(hashedPassword, secretKey);
                cli = false;
            }
        } while (cli);
        return output.entrySet().iterator().next();
    }

    /**
     * Test the SecretKey
     *
     * @param secretKey SecretKey PBKDF2
     * @return Return a boolean if the SecretKey is correct
     */
    public static Boolean testSecretKey(SecretKey secretKey) throws IOException {
        try {
            Cipher.decipher(secretKey, Tools.readFile(MyDirectory.FILENAME));
        } catch (GeneralSecurityException e) {
            return false;
        }
        return true;
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
     * Write to file
     *
     * @param filename Filename
     * @param input    Input to write
     * @throws IOException Throws IOException if there is an I/O exception
     */
    public static void writeToFile(String filename, byte[] input) throws IOException {
        Context context = E2EE.getContext();
        if (!isFileExists(filename)) {
            createFile(filename);
        }
        try (FileOutputStream fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fileOutputStream.write(input);
        }
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
}
