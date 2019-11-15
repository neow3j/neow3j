package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Hash;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.neow3j.constants.OpCode.CHECKSIG;
import static io.neow3j.constants.OpCode.PUSHBYTES33;
import static io.neow3j.constants.OpCode.PUSHBYTES64;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RawScriptTest {

    @Test
    public void testCreateWitness() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        RawScript witness = RawScript.createWitness(message, keyPair);

        SignatureData expectedSignature = Sign.signMessage(message, keyPair);
        byte[] expectedInvScript = ArrayUtils.concatenate(PUSHBYTES64.getValue(), expectedSignature.getConcatenated());
        assertArrayEquals(expectedInvScript, witness.getInvocationScript().getScript());

        byte[] expectedVerScript = ArrayUtils.concatenate(ArrayUtils.concatenate(
                PUSHBYTES33.getValue(), keyPair.getPublicKey().toByteArray()), CHECKSIG.getValue());
        assertArrayEquals(expectedVerScript, witness.getVerificationScript().getScript());
    }

    @Test
    public void testSerializeWithWitness() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        RawScript witness = RawScript.createWitness(message, keyPair);

        byte[] invScript = RawInvocationScript.fromMessageAndKeyPair(message, keyPair).getScript();
        byte[] invScriptLen = BigInteger.valueOf(invScript.length).toByteArray();
        byte[] veriScript = RawVerificationScript.fromPublicKey(keyPair.getPublicKey()).getScript();
        byte[] veriScriptLen = BigInteger.valueOf(veriScript.length).toByteArray();

        byte[] expectedWitness = ArrayUtils.concatenate(invScriptLen, invScript, veriScriptLen, veriScript);

        assertArrayEquals(expectedWitness, witness.toArray());
    }

    @Test
    public void testSerializeMultiSigScript() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        int signingThreshold = 2;

        List<SignatureData> signatures = new ArrayList<>();
        List<BigInteger> publicKeys = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
            signatures.add(Sign.signMessage(message, keyPair));
            publicKeys.add(keyPair.getPublicKey());
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
                        Numeric.toHexStringNoPrefix(publicKeys.get(0).toByteArray()) +
                        // PUSHBYTES33
                        "21" +
                        // public key 2
                        Numeric.toHexStringNoPrefix(publicKeys.get(1).toByteArray()) +
                        // PUSHBYTES33
                        "21" +
                        // public key 3
                        Numeric.toHexStringNoPrefix(publicKeys.get(2).toByteArray()) +
                        // PUSH3 (the total number of involved addresses)
                        "53" +
                        // CHECKMULTISIG
                        "ae"
        );


        // Test create from BigIntegers
        RawScript script = RawScript.createMultiSigWitness(signingThreshold, signatures, publicKeys);
        assertArrayEquals(expectedScript, script.toArray());

        // Test create from byte arrays.
        byte[][] keys = publicKeys.stream().map(BigInteger::toByteArray).toArray(byte[][]::new);
        script = RawScript.createMultiSigWitness(signingThreshold, signatures, keys);
        assertArrayEquals(expectedScript, script.toArray());
    }

    @Test
    public void testSerializeWithRandomScripts() {
        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        RawScript script = new RawScript(message, message);
        byte[] halfOfScript = ArrayUtils.concatenate((byte) message.length, message);
        byte[] expectedScript = ArrayUtils.concatenate(halfOfScript, halfOfScript);
        assertArrayEquals(expectedScript, script.toArray());
    }


    @Test
    public void testDeserializeWithWitness() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, IllegalAccessException, InstantiationException {

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
        buf.put(CHECKSIG.getValue());

        RawScript script = NeoSerializableInterface.from(buf.array(), RawScript.class);

        buf = ByteBuffer.allocate(1 + 64);
        buf.put(PUSHBYTES64.getValue());
        buf.put(Sign.signMessage(message, keyPair).getConcatenated());
        assertArrayEquals(buf.array(), script.getInvocationScript().getScript());

        buf = ByteBuffer.allocate(1 + 33 + 1);
        buf.put(PUSHBYTES33.getValue());
        buf.put(keyPair.getPublicKey().toByteArray());
        buf.put(CHECKSIG.getValue());
        assertArrayEquals(buf.array(), script.getVerificationScript().getScript());
    }

    @Test
    public void testGetScriptHash() {
        ECKeyPair keyPair = SampleKeys.CREDENTIALS_1.getEcKeyPair();
        int messageSize = 10;
        byte[] message = new byte[messageSize];
        Arrays.fill(message, (byte) 1);
        RawScript script = RawScript.createWitness(message, keyPair);

        byte[] expectedVerificationScript = Numeric.hexStringToByteArray(
                // PUSHBYTES33 + Public key + CHECKSIG
                "21" + SampleKeys.PUBLIC_KEY_STRING_1 + "ac"
        );
        byte[] expectedHash = Hash.sha256AndThenRipemd160(expectedVerificationScript);
        assertArrayEquals(expectedHash, script.getScriptHash().toArray());

        // Test with script hash generated by neon-js
        byte[] invocationScript = Numeric.hexStringToByteArray("4051c2e6e2993c6feb43383131ed2091f4953747d3e16ecad752cdd90203a992dea0273e98c8cd09e9bfcf2dab22ce843429cdf0fcb9ba4ac93ef1aeef40b20783");
        byte[] verificationScript = Numeric.hexStringToByteArray("21031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9ac");
        script = new RawScript(invocationScript, verificationScript);
        assertArrayEquals(
                Numeric.hexStringToByteArray("35b20010db73bf86371075ddfba4e6596f1ff35d"),
                script.getScriptHash().toArray()
        );
    }

    @Test
    public void createWithoutVerificationScript() {
        byte[] invocationScript = Numeric.hexStringToByteArray("0000");
        ScriptHash sh = new ScriptHash("1a70eac53f5882e40dd90f55463cce31a9f72cd4");
        RawScript s = new RawScript(invocationScript, sh);
        // 02: two bytes of invocation script;
        // 0000: invocation script;
        // 00: zero bytes of verification script
        assertEquals("02000000", Numeric.toHexStringNoPrefix(s.toArray()));
    }

}