package io.neow3j.transaction;

import static io.neow3j.constants.OpCode.PUSHBYTES33;
import static io.neow3j.constants.OpCode.PUSHBYTES64;
import static io.neow3j.constants.OpCode.SYSCALL;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Hash;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.Numeric;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class WitnessTest {

    @Test
    public void testCreateWitness() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        Witness witness = Witness.createWitness(message, keyPair);

        SignatureData expectedSignature = Sign.signMessage(message, keyPair);
        byte[] expectedInvScript = concatenate(PUSHBYTES64.getValue(), expectedSignature.getConcatenated());
        assertArrayEquals(expectedInvScript, witness.getInvocationScript().getScript());

        byte[] expectedVerScript = concatenate(concatenate(concatenate(
                        PUSHBYTES33.getValue(),
                        keyPair.getPublicKey2().getEncoded(true)),
                        OpCode.SYSCALL.getValue()),
                        InteropServiceCode.NEO_CRYPTO_CHECKSIG.getCodeBytes());
        assertArrayEquals(expectedVerScript, witness.getVerificationScript().getScript());
    }

    @Test
    public void testSerializeWithWitness()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        Witness witness = Witness.createWitness(message, keyPair);

        byte[] invScript = InvocationScript.fromMessageAndKeyPair(message, keyPair).getScript();
        byte[] invScriptLen = BigInteger.valueOf(invScript.length).toByteArray();
        byte[] veriScript = new VerificationScript(keyPair.getPublicKey2()).getScript();
        byte[] veriScriptLen = BigInteger.valueOf(veriScript.length).toByteArray();

        byte[] expectedWitness = concatenate(invScriptLen, invScript, veriScriptLen, veriScript);

        assertArrayEquals(expectedWitness, witness.toArray());
    }

    @Test
    public void testSerializeMultiSigScript() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        int signingThreshold = 2;

        List<SignatureData> signatures = new ArrayList<>();
        List<ECPublicKey> publicKeys = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
            signatures.add(Sign.signMessage(message, keyPair));
            publicKeys.add(keyPair.getPublicKey2());
        }

        byte[] expectedScript = Numeric.hexStringToByteArray(
                // 130 (0x82) bytes follow
                "82" +
                        // PUSHBYTES64
                        "40" +
                        // Signature with key 1
                        Numeric.toHexStringNoPrefix(signatures.get(0).getConcatenated()) +
                        // PUSHBYTES64
                        "40" +
                        // Signature with key 2
                        Numeric.toHexStringNoPrefix(signatures.get(1).getConcatenated()) +
                        // 105 (0x69) bytes follow
                        "69" +
                        // PUSH2 (the signing threshold)
                        "52" +
                        // PUSHBYTES33
                        "21" +
                        // public key 1
                        Numeric.toHexStringNoPrefix(publicKeys.get(0).getEncoded(true)) +
                        // PUSHBYTES33
                        "21" +
                        // public key 2
                        Numeric.toHexStringNoPrefix(publicKeys.get(1).getEncoded(true)) +
                        // PUSHBYTES33
                        "21" +
                        // public key 3
                        Numeric.toHexStringNoPrefix(publicKeys.get(2).getEncoded(true)) +
                        // PUSH3 (the total number of involved addresses)
                        "53" +
                        // CHECKMULTISIG
                        "ae"
        );

        // Test create from BigIntegers
        Witness script = Witness.createMultiSigWitness(signingThreshold, signatures, publicKeys);
        assertArrayEquals(expectedScript, script.toArray());

        // Test create from byte arrays.
        script = Witness.createMultiSigWitness(signingThreshold, signatures, publicKeys);
        assertArrayEquals(expectedScript, script.toArray());
    }

    @Test
    public void testSerializeWithRandomScripts() {
        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        Witness script = new Witness(message, message);
        byte[] halfOfScript = concatenate((byte) message.length, message);
        byte[] expectedScript = concatenate(halfOfScript, halfOfScript);
        assertArrayEquals(expectedScript, script.toArray());
    }


    @Test
    public void testDeserializeWithWitness() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, DeserializationException {

        int messageSize = 10;
        byte[] message = new byte[messageSize];
        Arrays.fill(message, (byte) 1);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();

        ByteBuffer buf = ByteBuffer.allocate(1 + 1 + 64 + 1 + 1 + 33 + 1);
        buf.put((byte) 65);
        buf.put(PUSHBYTES64.getValue());
        buf.put(Sign.signMessage(message, keyPair).getConcatenated());
        buf.put((byte) 35);
        buf.put(PUSHBYTES33.getValue());
        buf.put(keyPair.getPublicKey().toByteArray());
        buf.put(SYSCALL.getValue());
        buf.put(InteropServiceCode.NEO_CRYPTO_CHECKSIG.getCodeBytes());

        Witness script = NeoSerializableInterface.from(buf.array(), Witness.class);

        buf = ByteBuffer.allocate(1 + 64);
        buf.put(PUSHBYTES64.getValue());
        buf.put(Sign.signMessage(message, keyPair).getConcatenated());
        assertArrayEquals(buf.array(), script.getInvocationScript().getScript());

        buf = ByteBuffer.allocate(1 + 33 + 1);
        buf.put(PUSHBYTES33.getValue());
        buf.put(keyPair.getPublicKey().toByteArray());
        buf.put(SYSCALL.getValue());
        buf.put(InteropServiceCode.NEO_CRYPTO_CHECKSIG.getCodeBytes());
        assertArrayEquals(buf.array(), script.getVerificationScript().getScript());
    }

    @Test
    public void testGetScriptHash() {
        String sk = "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3";
        String pk = "0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6";
        ECKeyPair keyPair = ECKeyPair.create(Numeric.hexStringToByteArray(sk));
        int messageSize = 10;
        byte[] message = new byte[messageSize];
        Arrays.fill(message, (byte) 1);
        Witness script = Witness.createWitness(message, keyPair);

        byte[] expectedVerificationScript = Numeric.hexStringToByteArray(
                // PUSHBYTES33 + Public key + CHECKSIG
                "21" + pk + "ac"
        );
        byte[] expectedHash = Hash.sha256AndThenRipemd160(expectedVerificationScript);
        assertArrayEquals(expectedHash, script.getScriptHash().toArray());

        // Test with script hash generated by neon-js
        byte[] invocationScript = Numeric.hexStringToByteArray(
                "4051c2e6e2993c6feb43383131ed2091f4953747d3e16ecad752cdd90203a992dea0273e98c8cd09e9bfcf2dab22ce843429cdf0fcb9ba4ac93ef1aeef40b20783");
        byte[] verificationScript = Numeric.hexStringToByteArray(
                "21031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9ac");
        script = new Witness(invocationScript, verificationScript);
        assertArrayEquals(
                Numeric.hexStringToByteArray("35b20010db73bf86371075ddfba4e6596f1ff35d"),
                script.getScriptHash().toArray()
        );
    }

    @Test
    public void createWithoutVerificationScript() {
        byte[] invocationScript = Numeric.hexStringToByteArray("0000");
        ScriptHash sh = new ScriptHash("1a70eac53f5882e40dd90f55463cce31a9f72cd4");
        Witness s = new Witness(invocationScript, sh);
        // 02: two bytes of invocation script;
        // 0000: invocation script;
        // 00: zero bytes of verification script
        assertEquals("02000000", Numeric.toHexStringNoPrefix(s.toArray()));
    }

}