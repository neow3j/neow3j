package io.neow3j.transaction;

import static io.neow3j.constants.OpCode.PUSH2;
import static io.neow3j.constants.OpCode.PUSH3;
import static io.neow3j.constants.OpCode.PUSHBYTES33;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;

public class VerificationScriptTest {

    @Test
    public void testFromPublicKey() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        ECPublicKey key = ECKeyPair.createEcKeyPair().getPublicKey2();
        VerificationScript veriScript = new VerificationScript(key);

        byte[] expectedScript = concatenate(concatenate(concatenate(
                PUSHBYTES33.getValue(),
                key.getEncoded(true)),
                OpCode.SYSCALL.getValue()),
                InteropServiceCode.NEO_CRYPTO_CHECKSIG.getCodeBytes());

        assertArrayEquals(expectedScript, veriScript.getScript());
    }

    @Test
    public void testFromPublicKeys() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        List<ECPublicKey> publicKeys = new ArrayList<>();
        publicKeys.add(ECKeyPair.createEcKeyPair().getPublicKey2());
        publicKeys.add(ECKeyPair.createEcKeyPair().getPublicKey2());
        publicKeys.add(ECKeyPair.createEcKeyPair().getPublicKey2());

        ByteBuffer buf = ByteBuffer.allocate(1 + 3*(1 + 33) + 1 + 1);
        buf.put(PUSH2.getValue());
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(0).getEncoded(true));
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(1).getEncoded(true));
        buf.put(PUSHBYTES33.getValue());
        buf.put(publicKeys.get(2).getEncoded(true));
        buf.put(PUSH3.getValue());
        buf.put(OpCode.SYSCALL.getValue());
        buf.put(InteropServiceCode.NEO_CRYPTO_CHECKMULTISIG.getCodeBytes());
        VerificationScript script = new VerificationScript(publicKeys, 2);

        assertArrayEquals(buf.array(), script.getScript());
    }

    @Test
    public void testSerialize() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {

        ECPublicKey key = ECKeyPair.createEcKeyPair().getPublicKey2();
        VerificationScript veriScript = new VerificationScript(key);

        byte[] expectedScript = ByteBuffer.allocate(1+1+33+1)
                .put((byte)35)
                .put(PUSHBYTES33.getValue())
                .put(key.getEncoded(true))
                .put(OpCode.SYSCALL.getValue())
                .put(InteropServiceCode.NEO_CRYPTO_CHECKSIG.getCodeBytes())
                .array();

        assertArrayEquals(expectedScript, veriScript.toArray());
    }

    @Test
    public void testDeserialize() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, DeserializationException {

        int messageSize = 32;
        byte[] message = new byte[messageSize];
        Arrays.fill(message, (byte) 1);
        byte[] serializedScript = ArrayUtils.concatenate((byte)messageSize, message);
        VerificationScript script = NeoSerializableInterface.from(serializedScript, VerificationScript.class);
        assertArrayEquals(message, script.getScript());

        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        byte[] pub = ArrayUtils.concatenate(PUSHBYTES33.getValue(), keyPair.getPublicKey().toByteArray());
        byte[] expectedScript = concatenate(concatenate(concatenate(
                PUSHBYTES33.getValue(),
                keyPair.getPublicKey2().getEncoded(true)),
                OpCode.SYSCALL.getValue()),
                InteropServiceCode.NEO_CRYPTO_CHECKSIG.getCodeBytes());

        serializedScript = ArrayUtils.concatenate((byte)35, expectedScript);
        script = NeoSerializableInterface.from(serializedScript, VerificationScript.class);
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
        script = NeoSerializableInterface.from(buf.array(), VerificationScript.class);
        assertArrayEquals(message, script.getScript());
    }
    
    @Test
    public void testGetSigningThreshold() {
        String key = "21" + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef";

        // Signing threshold: 2
        StringBuilder script = new StringBuilder("52"); // signing threshold
        IntStream.range(0,3).forEach(i -> script.append(key));
        script.append("53"); // number of public keys
        script.append("68c7c34cba"); // sys call
        byte[] scriptBytes = Numeric.hexStringToByteArray(script.toString());
        int th = new VerificationScript(scriptBytes).getSigningThreshold();
        assertEquals(2, th);

        // Signing threshold: 255
        StringBuilder script2 = new StringBuilder("02ff00"); // signing threshold
        IntStream.range(0,16).forEach(i -> script2.append(key));
        script2.append("02ff00"); // number of public keys
        script2.append("68c7c34cba"); // sys call
        scriptBytes = Numeric.hexStringToByteArray(script2.toString());
        th = new VerificationScript(scriptBytes).getSigningThreshold();
        assertEquals(255, th);

        // Signing threshold: 1024
        StringBuilder script3 = new StringBuilder("020004ae"); // signing threshold
        IntStream.range(0,16).forEach(i -> script3.append(key));
        script3.append("020004ae"); // number of public keys
        script3.append("68c7c34cba"); // sys call
        scriptBytes = Numeric.hexStringToByteArray(script3.toString());
        th = new VerificationScript(scriptBytes).getSigningThreshold();
        assertEquals(1024, th);
    }

    @Test
    public void getSize() {
        byte[] script = Numeric.hexStringToByteArray(""
            + "147e5f3c929dd830d961626551dbea6b70e4b2837ed2fe9089eed2072ab3a655"
            + "523ae0fa8711eee4769f1913b180b9b3410bbb2cf770f529c85f6886f22cbaaf");
        InvocationScript s = new InvocationScript(script);
        assertThat(s.getSize(), is(1 + 64)); // byte for script length and actual length.
    }

    @Test
    public void isMultiSigScript() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + "52" // signing threshold
                + "21" + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef"
                + "21" + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9"
                + "21" + "03f0f9b358dfed564e74ffe242713f8bc866414226649f59859b140a130818898b"
                + "53" // number of public keys
                + "68c7c34cba"); // sys call
        VerificationScript s = new VerificationScript(scriptBytes);
        assertTrue(s.isMultiSigScript());

        // Also return true if the script is not valid but contains the check multi-sig interop
        // code.
        scriptBytes = Numeric.hexStringToByteArray("c7c34cba");
        s = new VerificationScript(scriptBytes);
        assertTrue(s.isMultiSigScript());

        // Return false if the script does not end in the above interop code.
        scriptBytes = Numeric.hexStringToByteArray("c8c34cba");
        s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());

        scriptBytes = Numeric.hexStringToByteArray("a720be29988");
        s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }
}