package io.neow3j.crypto.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.Sign;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.utils.ArrayUtils;
import org.junit.Test;

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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RawVerificationScriptTest {

    @Test
    public void testFromPublicKey() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        BigInteger key = Keys.createEcKeyPair().getPublicKey();
        RawVerificationScript veriScript = RawVerificationScript.fromPublicKey(key);

        byte[] expectedScript = ArrayUtils.concatenate(ArrayUtils.concatenate(
                PUSHBYTES33.getValue(), key.toByteArray()), CHECKSIG.getValue());

        assertArrayEquals(expectedScript, veriScript.getScript());
    }

    @Test
    public void testFromPublicKeys() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        List<BigInteger> publicKeys = new ArrayList<>();
        publicKeys.add(Keys.createEcKeyPair().getPublicKey());
        publicKeys.add(Keys.createEcKeyPair().getPublicKey());
        publicKeys.add(Keys.createEcKeyPair().getPublicKey());

        ByteBuffer buf = ByteBuffer.allocate(1 + 3*(1 + 33) + 1 + 1);
        buf.put(PUSH2.getValue());
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(0).toByteArray());
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(1).toByteArray());
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(2).toByteArray());
        buf.put(PUSH3.getValue());
        buf.put(CHECKMULTISIG.getValue());
        RawVerificationScript script = RawVerificationScript.fromPublicKeys(2, publicKeys);

        assertArrayEquals(buf.array(), script.getScript());
    }

    @Test
    public void testSerialize() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {

        BigInteger key = Keys.createEcKeyPair().getPublicKey();
        RawVerificationScript veriScript = RawVerificationScript.fromPublicKey(key);

        byte[] expectedScript = ByteBuffer.allocate(1+1+33+1)
                .put((byte)35)
                .put(PUSHBYTES33.getValue())
                .put(key.toByteArray())
                .put(CHECKSIG.getValue())
                .array();

        assertArrayEquals(expectedScript, veriScript.toArray());
    }

    @Test
    public void testDeserialize() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, IllegalAccessException, InstantiationException {

        int messageSize = 32;
        byte[] message = new byte[messageSize];
        Arrays.fill(message, (byte) 1);
        byte[] serializedScript = ArrayUtils.concatenate((byte)messageSize, message);
        RawVerificationScript script = NeoSerializableInterface.from(serializedScript, RawVerificationScript.class);
        assertArrayEquals(message, script.getScript());

        ECKeyPair keyPair = Keys.createEcKeyPair();
        byte[] pub = ArrayUtils.concatenate(PUSHBYTES33.getValue(), keyPair.getPublicKey().toByteArray());
        byte[] expectedScript = ArrayUtils.concatenate(pub, CHECKSIG.getValue());
        serializedScript = ArrayUtils.concatenate((byte)35, expectedScript);
        script = NeoSerializableInterface.from(serializedScript, RawVerificationScript.class);
        assertArrayEquals(expectedScript, script.getScript());

        messageSize = 256;
        message = new byte[messageSize];
        Arrays.fill(message, (byte)1);
        ByteBuffer buf = ByteBuffer.allocate(3 + messageSize);
        // Message size is bigger than one byte and needs encoding with byte 0xFD, which signifies
        // that a uint16 follows in little endian format, i.e. least significant byte first.
        buf.put((byte)0xFD);
        buf.put((byte)0x00);
        buf.put((byte)0x01);
        buf.put(message);
        script = NeoSerializableInterface.from(buf.array(), RawVerificationScript.class);
        assertArrayEquals(message, script.getScript());
    }
}