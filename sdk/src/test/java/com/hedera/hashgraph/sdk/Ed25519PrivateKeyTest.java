package com.hedera.hashgraph.sdk;

//import com.hedera.hashgraph.sdk.BadKeyException;
//import com.hedera.hashgraph.sdk.Mnemonic;

import java8.util.stream.RefStreams;
import java8.util.stream.Stream;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ed25519PrivateKeyTest {
    private static final String TEST_KEY_STR = "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10";
    private static final String TEST_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n"
        + "MC4CAQAwBQYDK2VwBCIEINtIS4KOZLLY8SzjwKDpOguMznrxu485yXcyOUSCU44Q\n"
        + "-----END PRIVATE KEY-----\n";

    // generated by hedera-sdk-js, not used anywhere
    private static final String MNEMONIC_STRING = "inmate flip alley wear offer often piece magnet surge toddler submit right radio absent pear floor belt raven price stove replace reduce plate home";
    private static final String MNEMONIC_PRIVATE_KEY = "302e020100300506032b657004220420853f15aecd22706b105da1d709b4ac05b4906170c2b9c7495dff9af49e1391da";

    private static final String MNEMONIC_LEGACY_STRING = "jolly kidnap tom lawn drunk chick optic lust mutter mole bride galley dense member sage neural widow decide curb aboard margin manure";
    private static final String MNEMONIC_LEGACY_PRIVATE_KEY = "302e020100300506032b657004220420882a565ad8cb45643892b5366c1ee1c1ef4a730c5ce821a219ff49b6bf173ddf";

    // backup phrase generated by the iOS wallet, not used anywhere
    private static final String IOS_MNEMONIC_STRING = "tiny denial casual grass skull spare awkward indoor ethics dash enough flavor good daughter early hard rug staff capable swallow raise flavor empty angle";

    // private key for "default account", should be index 0
    private static final String IOS_DEFAULT_PRIVATE_KEY = "5f66a51931e8c99089472e0d70516b6272b94dd772b967f8221e1077f966dbda2b60cf7ee8cf10ecd5a076bffad9a7c7b97df370ad758c0f1dd4ef738e04ceb6";

    // backup phrase generated by the Android wallet, also not used anywhere
    private static final String ANDROID_MNEMONIC_STRING = "ramp april job flavor surround pyramid fish sea good know blame gate village viable include mixed term draft among monitor swear swing novel track";
    // private key for "default account", should be index 0
    private static final String ANDROID_DEFAULT_PRIVATE_KEY = "c284c25b3a1458b59423bc289e83703b125c8eefec4d5aa1b393c2beb9f2bae66188a344ba75c43918ab12fa2ea4a92960eca029a2320d8c6a1c3b94e06c9985";

    private static final String PEM_PASSPHRASE = "this is a passphrase";

    /*
        # enter passphrase "this is a passphrase"
        echo '302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10' \
        | xxd -r -p \
        | openssl pkey -inform der -aes-128-cbc
     */
    private static final String ENCRYPTED_PEM = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n"
        + "MIGbMFcGCSqGSIb3DQEFDTBKMCkGCSqGSIb3DQEFDDAcBAi8WY7Gy2tThQICCAAw\n"
        + "DAYIKoZIhvcNAgkFADAdBglghkgBZQMEAQIEEOq46NPss58chbjUn20NoK0EQG1x\n"
        + "R88hIXcWDOECttPTNlMXWJt7Wufm1YwBibrxmCq1QykIyTYhy1TZMyxyPxlYW6aV\n"
        + "9hlo4YEh3uEaCmfJzWM=\n"
        + "-----END ENCRYPTED PRIVATE KEY-----\n";
    private static final String MESSAGE_STR = "This is a message about the world.";
    private static final byte[] MESSAGE_BYTES = MESSAGE_STR.getBytes(StandardCharsets.UTF_8);
    private static final String SIG_STR = "73bea53f31ca9c42a422ecb7516ec08d0bbd1a6bfd630ccf10ec1872454814d29f4a8011129cd007eab544af01a75f508285b591e5bed24b68f927751e49e30e";

    @SuppressWarnings("unused")
    private static Stream<String> privKeyStrings() {
        return RefStreams.of(
            TEST_KEY_STR,
            // raw hex (concatenated private + public key)
            "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10" +
                "e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7",
            // raw hex (just private key)
            "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10"
        );
    }

    @Test
    @DisplayName("private key generates successfully")
    void keyGenerates() {
        PrivateKey key = PrivateKey.generate();

        assertNotNull(key);
        assertNotNull(key.toBytes());

        // we generate the chain code at the same time
        assertTrue(key.isDerivable(), "generated key must support generation");
    }

    @Test
    @DisplayName("private key can be recovered from bytes")
    void keySerialization() {
        PrivateKey key1 = PrivateKey.generate();
        byte[] key1Bytes = key1.toBytes();
        PrivateKey key2 = PrivateKey.fromBytes(key1Bytes);
        byte[] key2Bytes = key2.toBytes();

        assertArrayEquals(key1Bytes, key2Bytes);
    }

    @ParameterizedTest
    @DisplayName("private key can be recovered from external string")
    @ValueSource(strings = {
        TEST_KEY_STR,
        // raw hex (concatenated private + public key)
        "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10" +
            "e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7",
        // raw hex (just private key)
        "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10"
    })
    void externalKeyDeserialize(String keyStr) {
        PrivateKey key = PrivateKey.fromString(keyStr);
        assertNotNull(key);
        // the above are all the same key
        assertEquals(
            TEST_KEY_STR,
            key.toString()
        );
    }

    @Test
    @DisplayName("private key can be encoded to a string")
    void keyToString() {
        PrivateKey key = PrivateKey.fromString(TEST_KEY_STR);

        assertNotNull(key);
        assertEquals(TEST_KEY_STR, key.toString());
    }

    @Test
    @DisplayName("private key can be decoded from a PEM file")
    void keyFromPem() throws IOException {
        StringReader stringReader = new StringReader(TEST_KEY_PEM);
        PrivateKey privateKey = PrivateKey.readPem(stringReader);

        assertEquals(privateKey.toString(), TEST_KEY_STR);
    }

    @Test
    @DisplayName("private key can be recovered from a mnemonic")
    void keyFromMnemonic() {
        Mnemonic mnemonic = assertDoesNotThrow(() -> Mnemonic.fromString(MNEMONIC_STRING));
        PrivateKey key = PrivateKey.fromMnemonic(mnemonic);
        PrivateKey key2 = PrivateKey.fromString(MNEMONIC_PRIVATE_KEY);
        assertArrayEquals(key2.toBytes(), key.toBytes());
    }

    @Test
    @DisplayName("validate 12 word generated mnemonic")
    void validateGenerated12() {
        Mnemonic mnemonic = Mnemonic.generate12();
        assertDoesNotThrow(() -> Mnemonic.fromString(mnemonic.toString()));
    }

    @Test
    @DisplayName("validate legacy mnemonic")
    void validateLegacyMnemonic() {
        Mnemonic mnemonic = assertDoesNotThrow(() -> Mnemonic.fromString(MNEMONIC_LEGACY_STRING));
        PrivateKey key = assertDoesNotThrow(mnemonic::toLegacyPrivateKey);
        assertEquals(key.toString(), MNEMONIC_LEGACY_PRIVATE_KEY);
    }

    @Test
    @DisplayName("validate 24 word generated mnemonic")
    void validateGenerated24() {
        Mnemonic mnemonic = Mnemonic.generate24();
        assertDoesNotThrow(() -> Mnemonic.fromString(mnemonic.toString()));
    }

    @Test
    @DisplayName("derived key matches that of the mobile wallets")
    void deriveKeyIndex0() {
        Mnemonic mnemonic = assertDoesNotThrow(() -> Mnemonic.fromString(MNEMONIC_STRING));
        PrivateKey mnemonicKey = PrivateKey.fromMnemonic(mnemonic);

        PrivateKey mnemonicDerivedKey = mnemonicKey.derive(0);
        assertEquals("302e020100300506032b657004220420f8dcc99a1ced1cc59bc2fee161c26ca6d6af657da9aa654da724441343ecd16f", mnemonicDerivedKey.toString());

        Mnemonic iosMnemonic = assertDoesNotThrow(() -> Mnemonic.fromString(IOS_MNEMONIC_STRING));
        PrivateKey iosKey = PrivateKey.fromMnemonic(iosMnemonic);

        PrivateKey iosDerivedKey = iosKey.derive(0);
        PrivateKey iosExpectedKey = PrivateKey.fromString(IOS_DEFAULT_PRIVATE_KEY);

        assertArrayEquals(iosDerivedKey.toBytes(), iosExpectedKey.toBytes());

        Mnemonic androidMnemonic = assertDoesNotThrow(() -> Mnemonic.fromString(ANDROID_MNEMONIC_STRING));
        PrivateKey androidKey = PrivateKey.fromMnemonic(androidMnemonic);

        PrivateKey androidDerivedKey = androidKey.derive(0);
        PrivateKey androidExpectedKey = PrivateKey.fromString(ANDROID_DEFAULT_PRIVATE_KEY);

        Mnemonic legacy = assertDoesNotThrow(() -> Mnemonic.fromString(MNEMONIC_LEGACY_STRING));
        PrivateKey legacyKey = assertDoesNotThrow(legacy::toLegacyPrivateKey);

        PrivateKey legacyDerivedKey = legacyKey.legacyDerive(0);
        assertEquals(legacyKey.toString(), "302e020100300506032b657004220420882a565ad8cb45643892b5366c1ee1c1ef4a730c5ce821a219ff49b6bf173ddf");
        assertEquals(legacyDerivedKey.toString(), "302e020100300506032b65700422042085c30624eb42423e159dde4571a621b454b8660055fb491b392c487569a9215f");

        assertArrayEquals(androidDerivedKey.toBytes(), androidExpectedKey.toBytes());
    }

    @Test
    @DisplayName("generated mnemonic24 can be turned into a working private key")
    void keyFromGeneratedMnemonic24() {
        Mnemonic mnemonic = Mnemonic.generate24();
        PrivateKey privateKey = PrivateKey.fromMnemonic(mnemonic);

        byte[] messageToSign = "this is a test message".getBytes(StandardCharsets.UTF_8);

        byte[] signature = privateKey.sign(messageToSign);

        assertTrue(Ed25519.verify(signature, 0, privateKey.getPublicKey().toBytes(), 0, messageToSign, 0, messageToSign.length));
    }

    @Test
    @DisplayName("generated mnemonic12 can be turned into a working private key")
    void keyFromGeneratedMnemonic12() {
        Mnemonic mnemonic = Mnemonic.generate12();
        PrivateKey privateKey = PrivateKey.fromMnemonic(mnemonic);

        byte[] messageToSign = "this is a test message".getBytes(StandardCharsets.UTF_8);

        byte[] signature = privateKey.sign(messageToSign);

        assertTrue(Ed25519.verify(signature, 0, privateKey.getPublicKey().toBytes(), 0, messageToSign, 0, messageToSign.length));
    }

    @Test
    @DisplayName("fromPem() with passphrase produces same key")
    void keyFromEncryptedPem() throws IOException {
        PrivateKey privateKey = PrivateKey.fromPem(ENCRYPTED_PEM, PEM_PASSPHRASE);
        assertEquals(privateKey.toString(), TEST_KEY_STR);
    }

    @Test
    @DisplayName("fromPem() with encrypted key without a passphrase throws useful error")
    void errorKeyFromEncryptedPemNoPassphrase() {
        assertEquals(
            "PEM file contained an encrypted private key but no passphrase was given",
            assertThrows(BadKeyException.class, () -> PrivateKey.fromPem(ENCRYPTED_PEM)).getMessage());
    }

    @ParameterizedTest
    @DisplayName("reproducible signature can be computed")
    @ValueSource(strings = {
        TEST_KEY_STR,
        // raw hex (concatenated private + public key)
        "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10" +
            "e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7",
        // raw hex (just private key)
        "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10"
    })
    void reproducibleSignature(String keyStr) {
        PrivateKey key = PrivateKey.fromString(keyStr);
        byte[] signature = key.sign(MESSAGE_BYTES);

        assertEquals(
            SIG_STR,
            Hex.toHexString(signature)
        );
    }
}
