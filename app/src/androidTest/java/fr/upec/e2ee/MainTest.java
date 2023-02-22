package fr.upec.e2ee;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.SecretKey;

import fr.upec.e2ee.mystate.MyConversations;
import fr.upec.e2ee.mystate.MyDirectory;
import fr.upec.e2ee.mystate.MyKeyPair;
import fr.upec.e2ee.mystate.MyState;
import fr.upec.e2ee.protocol.Cipher;
import fr.upec.e2ee.protocol.Communication;
import fr.upec.e2ee.protocol.Conversation;
import fr.upec.e2ee.protocol.Message1;
import fr.upec.e2ee.protocol.SecretBuild;
import fr.upec.e2ee.protocol.Sign;

public class MainTest {
    static private MyState user1;
    static private MyState user2;
    static private SecretBuild sbUser1;
    static private SecretBuild sbUser2;

    @BeforeClass
    public static void setupClass() throws GeneralSecurityException {
        user1 = new MyState(Tools.hashPassword("1234"), "fr.upec.e2ee.keypair.unittest1");
        user2 = new MyState(Tools.hashPassword("1234"), "fr.upec.e2ee.keypair.unittest2");
    }

    @AfterClass
    public static void deleteClass() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        keyStore.deleteEntry("fr.upec.e2ee.keypair");
        keyStore.deleteEntry("fr.upec.e2ee.keypair.unittest1");
        keyStore.deleteEntry("fr.upec.e2ee.keypair.unittest2");
        keyStore.deleteEntry("fr.upec.e2ee.keypair.unittest3");
    }

    @Before
    public void deleteFilesBefore() {
        Tools.deleteFile(MyState.FILENAME);
        Tools.deleteFile(MyDirectory.FILENAME);
        Tools.deleteFile(MyConversations.FILENAME);
    }

    @After
    public void deleteFilesAfter() {
        Tools.deleteFile(MyState.FILENAME);
        Tools.deleteFile(MyDirectory.FILENAME);
        Tools.deleteFile(MyConversations.FILENAME);
    }

    @Test
    public void testMessage1() throws GeneralSecurityException {
        Message1 message1User1 = new Message1(System.currentTimeMillis() / 1000L, user1.getMyNonce());
        Message1 message1User2 = new Message1(System.currentTimeMillis() / 1000L, user2.getMyNonce());

        user2.getMyDirectory().addPerson("user1", user1.getMyPublicKey().getEncoded());
        String message1User1For2 = Communication.createMessage1(message1User1);
        SecretBuild secretBuildUser2 = Communication.handleMessage1(message1User2, message1User1For2);

        user1.getMyDirectory().addPerson("user2", user2.getMyPublicKey().getEncoded());
        String message1User2For1 = Communication.createMessage1(message1User2);
        SecretBuild secretBuildUser1 = Communication.handleMessage1(message1User1, message1User2For1);

        assertEquals(message1User1.getTimestamp(), secretBuildUser2.getOtherDate());
        assertArrayEquals(message1User1.getNonce(), secretBuildUser2.getOtherNonce());
        assertArrayEquals(message1User1.getPublicKey().getEncoded(), secretBuildUser2.getOtherPubKey());

        assertEquals(message1User2.getTimestamp(), secretBuildUser1.getOtherDate());
        assertArrayEquals(message1User2.getNonce(), secretBuildUser1.getOtherNonce());
        assertArrayEquals(message1User2.getPublicKey().getEncoded(), secretBuildUser1.getOtherPubKey());
    }

    @Test
    public void testMessage2() throws Exception {
        Message1 message1User1 = new Message1(System.currentTimeMillis() / 1000L, user1.getMyNonce());
        Message1 message1User2 = new Message1(System.currentTimeMillis() / 1000L, user2.getMyNonce());

        user2.getMyDirectory().addPerson("user1", user1.getMyPublicKey().getEncoded());
        String message1User1For2 = Communication.createMessage1(message1User1);
        SecretBuild secretBuildUser2 = Communication.handleMessage1(message1User2, message1User1For2);

        user1.getMyDirectory().addPerson("user2", user2.getMyPublicKey().getEncoded());
        String message1User2For1 = Communication.createMessage1(message1User2);
        SecretBuild secretBuildUser1 = Communication.handleMessage1(message1User1, message1User2For1);

        assertTrue(secretBuildUser1.equals(secretBuildUser2));
        sbUser1 = secretBuildUser1;
        sbUser2 = secretBuildUser2;

        String message2User1 = Communication.createMessage2(user1.getMyPrivateKey(), secretBuildUser1);
        String message2User2 = Communication.createMessage2(user2.getMyPrivateKey(), secretBuildUser2);

        Conversation conversationUser1 = Communication.handleMessage2(user1.getMyDirectory(), secretBuildUser1, message2User2);
        Conversation conversationUser2 = Communication.handleMessage2(user2.getMyDirectory(), secretBuildUser2, message2User1);

        assertEquals("user1", conversationUser2.getName());
        assertEquals("user2", conversationUser1.getName());
    }

    @Test
    public void testSigningVerifying() throws GeneralSecurityException {
        String textString = "Around the World, Around the World";
        byte[] signatureUser1 = Sign.sign(user1.getMyPrivateKey(), textString.getBytes(StandardCharsets.UTF_8));
        String signatureBase64User1 = Tools.toBase64(signatureUser1);

        byte[] signatureFromUser1 = Tools.toBytes(signatureBase64User1);
        assertTrue(Sign.verify(user1.getMyPublicKey(), signatureFromUser1, textString.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testCipherDecipher() throws GeneralSecurityException {
        String textString = "Moeagare Moeagare GANDAMU!";
        byte[] cipheredTextUser1 = Cipher.cipher(Tools.toSecretKey(sbUser1.getSymKey()), textString.getBytes(StandardCharsets.UTF_8));
        String cipheredTextBase64User1 = Tools.toBase64(cipheredTextUser1);

        byte[] cipheredTextFromUser1 = Tools.toBytes(cipheredTextBase64User1);
        assertEquals(textString, new String(Cipher.decipher(Tools.toSecretKey(sbUser2.getSymKey()), cipheredTextFromUser1)));
    }

    @Test
    public void testSaveAndLoadMyKeyPair() throws GeneralSecurityException, IOException {
        MyKeyPair myKeyPair = new MyKeyPair("fr.upec.e2ee.keypair.unittest3"); //Without File

        MyKeyPair myKeyPairViaFile = MyKeyPair.load("fr.upec.e2ee.keypair.unittest3"); //With File

        assertArrayEquals(myKeyPair.getMyPublicKey().getEncoded(), myKeyPairViaFile.getMyPublicKey().getEncoded());
        assertArrayEquals(myKeyPair.getMyPrivateKey().getEncoded(), myKeyPairViaFile.getMyPrivateKey().getEncoded());
    }

    @Test
    public void testSaveAndLoadMyState() throws GeneralSecurityException, IOException {
        String hashedPassword = Tools.hashPassword("1234");
        MyState myState = user1;
        myState.save();

        SecretKey newSecretKey = Tools.loadSecretKey(hashedPassword);
        MyState myStateFile = MyState.load(hashedPassword, newSecretKey, "fr.upec.e2ee.keypair.unittest1");

        assertArrayEquals(myState.getMyPublicKey().getEncoded(), myStateFile.getMyPublicKey().getEncoded());
        assertArrayEquals(myState.getMyPrivateKey().getEncoded(), myStateFile.getMyPrivateKey().getEncoded());
        assertEquals(myState.getMyNonce(), myStateFile.getMyNonce());
    }

    @Test
    public void testMyDirectory() throws IOException, GeneralSecurityException {
        String hashedPassword = Tools.hashPassword("1234");
        //SecretKey secretKey = Tools.getSecretKeyPBKDF2(hashedPassword.toCharArray(), Tools.generateRandomBytes(32), 1024);
        SecretKey secretKey = null;

        MyDirectory myDirectory = new MyDirectory();
        assertEquals(0, myDirectory.sizeOfDirectory());

        myDirectory.addPerson("user1", user1.getMyPublicKey().getEncoded());
        assertEquals(1, myDirectory.sizeOfDirectory());

        myDirectory.addPerson("user2", user2.getMyPublicKey().getEncoded());
        assertEquals(2, myDirectory.sizeOfDirectory());

        myDirectory.saveFile(secretKey);
        MyDirectory myDirectoryFile = new MyDirectory(secretKey);

        assertTrue(myDirectory.isInDirectory(user1.getMyPublicKey().getEncoded()));
        assertTrue(myDirectoryFile.isInDirectory(myDirectory.getPerson("user2")));

        myDirectory.deletePerson("user2");
        assertEquals(1, myDirectory.sizeOfDirectory());
        myDirectory.saveFile(secretKey);

        MyDirectory myDirectoryFile2 = new MyDirectory(secretKey);
        assertEquals(1, myDirectoryFile2.sizeOfDirectory());
    }

    @Test
    public void testReplaceMyKeyPair() throws GeneralSecurityException, IOException {
        String hashedPassword = Tools.hashPassword("1234");

        user1.save();

        SecretKey secretKey = Tools.loadSecretKey(hashedPassword);
        MyState myStateFile = MyState.load(hashedPassword, secretKey);

        user1.replaceMyKeyPair("fr.upec.e2ee.keypair.unittest1");

        assertFalse(Arrays.equals(myStateFile.getMyPublicKey().getEncoded(), user1.getMyPublicKey().getEncoded()));
    }

    @Test
    public void testParserPubKey() {
        String pemKey = "-----BEGIN PUBLIC KEY-----MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAECdQQzt/cpVAylBBPo4qw6dwVU17vNy5ZQG9QJqUwZnnC4yMjdrFC0MIvPgGxA/p1yOLPbSXnQZKEak27u9OEZg==-----END PUBLIC KEY-----";
        String pubKey = Tools.keyParser(pemKey);
        String reTestPubKey = Tools.keyParser(pubKey);
        String wrongPubKey = "I'm sorry, Dave. I'm afraid I can't do that.";

        assertEquals("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAECdQQzt/cpVAylBBPo4qw6dwVU17vNy5ZQG9QJqUwZnnC4yMjdrFC0MIvPgGxA/p1yOLPbSXnQZKEak27u9OEZg==", pubKey);
        assertEquals("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAECdQQzt/cpVAylBBPo4qw6dwVU17vNy5ZQG9QJqUwZnnC4yMjdrFC0MIvPgGxA/p1yOLPbSXnQZKEak27u9OEZg==", reTestPubKey);
        assertThrows(IllegalArgumentException.class, () -> Tools.keyParser(wrongPubKey));
    }

    /*@Test
    public void testPBKDF2() throws GeneralSecurityException {
        String test = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAECdQQzt/cpVAylBBPo4qw6dwVU17vNy5ZQG9QJqUwZnnC4yMjdrFC0MIvPgGxA/p1yOLPbSXnQZKEak27u9OEZg==";

        byte[] salt = Tools.toBytes("cKpdYGz6EXp5QctsIOsmVSn7ewGwwx5fW8TdA7N+5Ho=");
        SecretKey totoSecretKeyPBKDF2 = Tools.getSecretKeyPBKDF2("toto".toCharArray(), salt, 1000);
        byte[] output = Cipher.cipher(totoSecretKeyPBKDF2, test.getBytes(StandardCharsets.UTF_8));

        assertEquals("ViK7x2IDuqGu13hNAdjMRXaDmu0nSj93KPt1UvylebE=", Tools.toBase64(totoSecretKeyPBKDF2.getEncoded()));
        assertEquals(test, new String(Cipher.decipher(totoSecretKeyPBKDF2, output)));


        byte[] salt2 = Tools.generateRandomBytes(32);
        SecretKey secretKeyPBKDF2 = Tools.getSecretKeyPBKDF2("1234".toCharArray(), salt2, Tools.PBKDF2_ITERATION);
        byte[] output2 = Cipher.cipher(secretKeyPBKDF2, test.getBytes(StandardCharsets.UTF_8));

        assertEquals(test, new String(Cipher.decipher(secretKeyPBKDF2, output2)));
        assertThrows(AEADBadTagException.class, () -> Cipher.decipher(totoSecretKeyPBKDF2, output2));
    }*/

    @Test
    public void testAll() throws GeneralSecurityException, IOException {
        //Create MyState
        String hashedPassword = Tools.hashPassword("1234");
        //SecretKey secretKey = Tools.getSecretKeyPBKDF2(hashedPassword.toCharArray(), Tools.generateRandomBytes(32), 1024);
        //MyState myStatePhone = MyState.load(hashedPassword, secretKey);
        MyState myStatePhone = MyState.load();

        myStatePhone.incrementMyNonce();
        myStatePhone.save();

        //SecretKey newSecretKey = Tools.loadSecretKey(hashedPassword);
        //MyState myStatePhoneFile = MyState.load(hashedPassword, newSecretKey);
        MyState myStatePhoneFile = MyState.load();

        assertArrayEquals(myStatePhone.getMyPublicKey().getEncoded(), myStatePhoneFile.getMyPublicKey().getEncoded());
        assertArrayEquals(myStatePhone.getMyPrivateKey().getEncoded(), myStatePhoneFile.getMyPrivateKey().getEncoded());
        assertEquals(myStatePhone.getMyNonce(), myStatePhoneFile.getMyNonce());

        //Create Conversation
        Message1 message1User1 = new Message1(System.currentTimeMillis() / 1000L, myStatePhone.getMyNonce());
        Message1 message1User2 = new Message1(System.currentTimeMillis() / 1000L, user2.getMyNonce());

        user2.getMyDirectory().addPerson("user1", myStatePhone.getMyPublicKey().getEncoded());
        String message1User1For2 = Communication.createMessage1(message1User1);
        SecretBuild secretBuildUser2 = Communication.handleMessage1(message1User2, message1User1For2);

        myStatePhone.getMyDirectory().addPerson("user2", user2.getMyPublicKey().getEncoded());
        String message1User2For1 = Communication.createMessage1(message1User2);
        SecretBuild secretBuildUser1 = Communication.handleMessage1(message1User1, message1User2For1);

        assertTrue(secretBuildUser1.equals(secretBuildUser2));

        String message2User1 = Communication.createMessage2(myStatePhone.getMyPrivateKey(), secretBuildUser1);
        String message2User2 = Communication.createMessage2(user2.getMyPrivateKey(), secretBuildUser2);

        Conversation conversationUser1 = Communication.handleMessage2(myStatePhone.getMyDirectory(), secretBuildUser1, message2User2);
        Conversation conversationUser2 = Communication.handleMessage2(user2.getMyDirectory(), secretBuildUser2, message2User1);

        assertEquals("user2", conversationUser1.getName());
        assertEquals("user1", conversationUser2.getName());
        //End create Conversation

        //Add Conversation to MyState
        myStatePhone.addAConversation(conversationUser1);
        myStatePhone.incrementMyNonce();
        myStatePhone.save();

        assertEquals(1, myStatePhone.getMyConversations().getSize());

        MyConversations myConversationsUser1 = myStatePhone.getMyConversations();

        //Test cipher/decipher message
        String textString = "Another bites the dust";
        byte[] cipheredTextUser1 = Cipher.cipher(Tools.toSecretKey(myConversationsUser1.getConversation(0).getSecretKey()), textString.getBytes(StandardCharsets.UTF_8));
        String cipheredTextBase64User1 = Tools.toBase64(cipheredTextUser1);

        byte[] cipheredTextFromUser1 = Tools.toBytes(cipheredTextBase64User1);
        assertEquals(textString, new String(Cipher.decipher(Tools.toSecretKey(secretBuildUser2.getSymKey()), cipheredTextFromUser1)));

        //Delete a Conversation
        myConversationsUser1.deleteConversation(myConversationsUser1.getConversation(0));
        myStatePhone.save();

        assertEquals(0, myConversationsUser1.getSize());
    }
}
