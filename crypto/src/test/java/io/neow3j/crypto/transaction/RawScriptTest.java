package io.neow3j.crypto.transaction;

import io.neow3j.constants.OpCode;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Hash;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.SampleKeys;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import org.junit.Test;
import org.testcontainers.shaded.io.netty.buffer.ByteBufUtil;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.neow3j.constants.OpCode.CHECKMULTISIG;
import static io.neow3j.constants.OpCode.CHECKSIG;
import static io.neow3j.constants.OpCode.PUSH2;
import static io.neow3j.constants.OpCode.PUSH3;
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
        ECKeyPair keyPair = Keys.createEcKeyPair();
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
        ECKeyPair keyPair = Keys.createEcKeyPair();
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

        ECKeyPair keyPair1 = Keys.createEcKeyPair();
        signatures.add(Sign.signMessage(message, keyPair1));
        publicKeys.add(keyPair1.getPublicKey());

        ECKeyPair keyPair2 = Keys.createEcKeyPair();
        signatures.add(Sign.signMessage(message, keyPair2));
        publicKeys.add(keyPair2.getPublicKey());

        ECKeyPair keyPair3 = Keys.createEcKeyPair();
        publicKeys.add(keyPair3.getPublicKey());

        RawScript script = RawScript.createMultiSigWitness(signingThreshold, signatures, publicKeys);

        int invScriptSize = 2 * (1 + PUSHBYTES64.getValue());
        int verScriptSize = 1 + 3 * (1 + PUSHBYTES33.getValue()) + 2;
        ByteBuffer buf = ByteBuffer.allocate(2 + invScriptSize + verScriptSize);

        buf.put((byte)130);
        // invocation script
        buf.put(PUSHBYTES64.getValue());
        buf.put(Sign.signMessage(message, keyPair1).getConcatenated());
        buf.put(PUSHBYTES64.getValue());
        buf.put(Sign.signMessage(message, keyPair2).getConcatenated());

        buf.put((byte)105);
        // verification script
        buf.put(PUSH2.getValue());
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(0).toByteArray());
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(1).toByteArray());
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(2).toByteArray());
        buf.put(PUSH3.getValue());
        buf.put(CHECKMULTISIG.getValue());

        assertArrayEquals(buf.array(), script.toArray());
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
        ECKeyPair keyPair = Keys.createEcKeyPair();

        ByteBuffer buf = ByteBuffer.allocate(1 + 1 + 64 + 1 + 1 + 33 + 1);
        buf.put((byte)65);
        buf.put(PUSHBYTES64.getValue());
        buf.put(Sign.signMessage(message, keyPair).getConcatenated());
        buf.put((byte)35);
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

        ByteBuffer buf = ByteBuffer.allocate(1 + 33 + 1);
        buf.put(PUSHBYTES33.getValue());
        buf.put(keyPair.getPublicKey().toByteArray());
        buf.put(CHECKSIG.getValue());
        String expectedHash = Numeric.toHexStringNoPrefix(Hash.sha256AndThenRipemd160(buf.array()));

        assertEquals(expectedHash, script.getScriptHash());
    }
}