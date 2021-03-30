package io.neow3j.transaction;

import io.neow3j.constants.InteropService;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.Hash160;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Hash;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.neow3j.constants.OpCode.PUSH2;
import static io.neow3j.constants.OpCode.PUSHDATA1;
import static io.neow3j.constants.OpCode.SYSCALL;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class WitnessTest {

    @Test
    public void createWitness() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        Witness witness = Witness.create(message, keyPair);

        // Test invocation script
        SignatureData expectedSig = Sign.signMessage(message, keyPair);
        String expected = "" +
                PUSHDATA1.toString() + "40" + // 64 bytes of signature data
                Numeric.toHexStringNoPrefix(expectedSig.getConcatenated()); // signature

        assertArrayEquals(
                Numeric.hexStringToByteArray(expected),
                witness.getInvocationScript().getScript());

        // Test verification script
        expected = "" +
                PUSHDATA1.toString() + "21" + // 33 bytes of public key
                Numeric.toHexStringNoPrefix(keyPair.getPublicKey().getEncoded(true)) + // pubKey
                SYSCALL.toString() + // syscall to...
                InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash(); // ...signature verification

        assertArrayEquals(
                Numeric.hexStringToByteArray(expected),
                witness.getVerificationScript().getScript());
    }

    @Test
    public void serializeWithWitness()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        Witness witness = Witness.create(message, keyPair);

        byte[] invScript = InvocationScript.fromMessageAndKeyPair(message, keyPair).getScript();
        byte[] invScriptLen = BigInteger.valueOf(invScript.length).toByteArray();
        byte[] veriScript = new VerificationScript(keyPair.getPublicKey()).getScript();
        byte[] veriScriptLen = BigInteger.valueOf(veriScript.length).toByteArray();

        byte[] expectedWitness = concatenate(invScriptLen, invScript, veriScriptLen, veriScript);

        assertArrayEquals(expectedWitness, witness.toArray());
    }

    @Test
    public void serializeMultiSigWitness() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        int signingThreshold = 2;

        List<SignatureData> signatures = new ArrayList<>();
        List<ECPublicKey> publicKeys = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
            signatures.add(Sign.signMessage(message, keyPair));
            publicKeys.add(keyPair.getPublicKey());
        }

        String expected = ""
                + "84" // 132 bytes follow as invocation script
                + PUSHDATA1.toString() + "40" // PUSHDATA 64 bytes
                + Numeric.toHexStringNoPrefix(signatures.get(0).getConcatenated()) // key 1 sig
                + PUSHDATA1.toString() + "40" // PUSHDATA 64 bytes
                + Numeric.toHexStringNoPrefix(signatures.get(1).getConcatenated()) // key 2 sig
                + "70" // 113 bytes follow as verification script
                + PUSH2.toString() // signing threshold
                + PUSHDATA1 + "21" // PUSHDATA 33 bytes
                + Numeric.toHexStringNoPrefix(publicKeys.get(0).getEncoded(true)) // public key 1
                + PUSHDATA1 + "21" // PUSHDATA 33 bytes
                + Numeric.toHexStringNoPrefix(publicKeys.get(1).getEncoded(true)) // public key 2
                + PUSHDATA1 + "21" // PUSHDATA 33 bytes
                + Numeric.toHexStringNoPrefix(publicKeys.get(2).getEncoded(true)) // public key 3
                + OpCode.PUSH3.toString() // m = 3, number of keys
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKMULTISIG.getHash();

        // Test create from BigIntegers
        Witness script = Witness.createMultiSigWitness(signingThreshold, signatures, publicKeys);
        assertArrayEquals(Numeric.hexStringToByteArray(expected), script.toArray());

        // Test create from byte arrays.
        script = Witness.createMultiSigWitness(signingThreshold, signatures, publicKeys);
        assertArrayEquals(Numeric.hexStringToByteArray(expected), script.toArray());
    }

    @Test
    public void serializeWitnessWithCustomScripts() {
        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        Witness script = new Witness(message, message);
        byte[] halfOfScript = concatenate((byte) message.length, message);
        byte[] expectedScript = concatenate(halfOfScript, halfOfScript);
        assertArrayEquals(expectedScript, script.toArray());
    }

    @Test
    public void deserializeWitness() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, DeserializationException {

        int messageSize = 10;
        byte[] message = new byte[messageSize];
        Arrays.fill(message, (byte) 1);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();

        byte[] sig = Sign.signMessage(message, keyPair).getConcatenated();

        String invocationScript = ""
                + PUSHDATA1.toString() + "40" // 64 bytes of signature
                + Numeric.toHexStringNoPrefix(sig); // signature

        String verificationScript = ""
                + PUSHDATA1.toString() + "21" // 33 bytes of public key
                + Numeric.toHexStringNoPrefix(keyPair.getPublicKey().getEncoded(true)) // pubKey
                + SYSCALL.toString() // syscall to...
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash(); // ...signature verification

        String serializedWitness = ""
                + "42" // VarInt 66 bytes for invocation script
                + invocationScript
                + "28" // VarInt 1 + 1 + 33 + 1 + 4 = 40 bytes for verification script.
                + verificationScript;

        Witness witness = NeoSerializableInterface
                .from(Numeric.hexStringToByteArray(serializedWitness), Witness.class);

        assertArrayEquals(
                Numeric.hexStringToByteArray(invocationScript),
                witness.getInvocationScript().getScript());
        assertArrayEquals(
                Numeric.hexStringToByteArray(verificationScript),
                witness.getVerificationScript().getScript());
    }

    @Test
    public void getScriptHashFromWitness1() {
        String sk = "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3";
        String pk = "0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6";
        ECKeyPair keyPair = ECKeyPair.create(Numeric.hexStringToByteArray(sk));
        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 1);
        Witness script = Witness.create(message, keyPair);

        String expectedVerificationScript = ""
                + PUSHDATA1.toString() + "21" // 33 bytes of public key
                + pk // public key
                + SYSCALL.toString() // syscall to...
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash(); // ...signature verification

        byte[] expectedHash = Hash.sha256AndThenRipemd160(
                Numeric.hexStringToByteArray(expectedVerificationScript));

        assertArrayEquals(expectedHash, script.getScriptHash().toArray());
    }

    @Test
    public void getScriptHashFromWitness2() {
        // Test with script hash generated by neon-js.
        byte[] invocationScript = Numeric.hexStringToByteArray(
                "4051c2e6e2993c6feb43383131ed2091f4953747d3e16ecad752cdd90203a992dea0273e98c8cd09e9bfcf2dab22ce843429cdf0fcb9ba4ac93ef1aeef40b20783");
        byte[] verificationScript = Numeric.hexStringToByteArray(
                "21031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9ac");
        Witness witness = new Witness(invocationScript, verificationScript);
        assertArrayEquals(
                Numeric.hexStringToByteArray("35b20010db73bf86371075ddfba4e6596f1ff35d"),
                witness.getScriptHash().toArray()
        );
    }

    @Test
    public void createWithoutVerificationScript() {
        byte[] invocationScript = Numeric.hexStringToByteArray("0000");
        Hash160 hash = new Hash160("1a70eac53f5882e40dd90f55463cce31a9f72cd4");
        Witness witness = new Witness(invocationScript, hash);
        // 02: two bytes of invocation script;
        // 0000: invocation script;
        // 00: zero bytes of verification script
        assertEquals("02000000", Numeric.toHexStringNoPrefix(witness.toArray()));
    }

}