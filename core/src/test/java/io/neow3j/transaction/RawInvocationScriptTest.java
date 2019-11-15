package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.utils.ArrayUtils;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

import static io.neow3j.constants.OpCode.PUSHBYTES64;
import static org.junit.Assert.assertArrayEquals;

public class RawInvocationScriptTest {

    @Test
    public void testFromMessageAndKeyPair() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        RawInvocationScript invScript = RawInvocationScript.fromMessageAndKeyPair(message, keyPair);
        SignatureData expectedSignature = Sign.signMessage(message, keyPair);
        byte[] expectedScript = ArrayUtils.concatenate(PUSHBYTES64.getValue(), expectedSignature.getConcatenated());
        assertArrayEquals(expectedScript, invScript.getScript());
    }

    @Test
    public void testSerializeSignatureInvocationScript() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        RawInvocationScript invScript = RawInvocationScript.fromMessageAndKeyPair(message, keyPair);
        byte[] signature = Sign.signMessage(message, keyPair).getConcatenated();

        byte[] expectedScript = ByteBuffer.allocate(1+1+64)
                .put((byte)65)
                .put(PUSHBYTES64.getValue())
                .put(signature)
                .array();

        assertArrayEquals(expectedScript, invScript.toArray());
    }

    @Test
    public void testSerializeRandomInvocationScript() {
        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 1);
        RawInvocationScript invScript = new RawInvocationScript(message);
        byte[] expectedScript = ArrayUtils.concatenate((byte)10, message);
        assertArrayEquals(expectedScript, invScript.toArray());
    }

    @Test
    public void testDeserialize() throws IllegalAccessException, InstantiationException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

        int messageSize = 32;
        byte[] message = new byte[messageSize];
        Arrays.fill(message, (byte) 1);
        byte[] serializedScript = ArrayUtils.concatenate((byte)messageSize, message);
        RawInvocationScript script = NeoSerializableInterface.from(serializedScript, RawInvocationScript.class);
        assertArrayEquals(message, script.getScript());

        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        byte[] signature = Sign.signMessage(message, keyPair).getConcatenated();
        byte[] expectedScript = ArrayUtils.concatenate(PUSHBYTES64.getValue(), signature);
        serializedScript = ArrayUtils.concatenate((byte)65, expectedScript);
        script = NeoSerializableInterface.from(serializedScript, RawInvocationScript.class);
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
        script = NeoSerializableInterface.from(buf.array(), RawInvocationScript.class);
        assertArrayEquals(message, script.getScript());
    }

}