package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.script.InvocationScript;
import io.neow3j.script.OpCode;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.List;

import static io.neow3j.script.OpCode.PUSHDATA1;
import static io.neow3j.script.OpCode.PUSHDATA2;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InvocationScriptTest {

    @Test
    public void testFromMessageAndKeyPair() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        InvocationScript invScript = InvocationScript.fromMessageAndKeyPair(message, keyPair);
        byte[] expectedSignature = Sign.signMessage(message, keyPair).getConcatenated();
        String expected = ""
                + OpCode.PUSHDATA1.toString() + "40" // 64 bytes of signature
                + Numeric.toHexStringNoPrefix(expectedSignature); // signature

        assertArrayEquals(Numeric.hexStringToByteArray(expected), invScript.getScript());
    }

    @Test
    public void testSerializeSignatureInvocationScript() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 10);
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        InvocationScript invScript = InvocationScript.fromMessageAndKeyPair(message, keyPair);

        byte[] expectedSignature = Sign.signMessage(message, keyPair).getConcatenated();
        String expected = ""
                + "42" // VarInt 66 bytes for invocation script
                + PUSHDATA1.toString() + "40" // 64 bytes of signature
                + Numeric.toHexStringNoPrefix(expectedSignature); // signature

        assertArrayEquals(Numeric.hexStringToByteArray(expected), invScript.toArray());
    }

    @Test
    public void testSerializeRandomInvocationScript() {
        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 1);
        InvocationScript invScript = new InvocationScript(message);
        byte[] expectedScript = ArrayUtils.concatenate((byte) 10, message);
        assertArrayEquals(expectedScript, invScript.toArray());
    }

    @Test
    public void deserializeCustomInvocationScript() throws DeserializationException {
        // Create a invocation script that does not contain a simple signature but some other
        // data that is longer. We chose 256 bytes here because this demands two bytes to encode
        // the messages size. I.e. PUSHDATA2 instead of PUSHDATA1.
        byte[] message = new byte[256]; // length 256 = 0x0100 (big-endian) = 0x0001 (little-endian)
        Arrays.fill(message, (byte) 1);

        String script = ""
                + PUSHDATA2.toString() + "0001" // 256 bytes of data
                + Numeric.toHexStringNoPrefix(message);

        String serializedScript = ""
                + "FD" + "0301" // VarInt 259 bytes. 1 + 2 + 256 = 259 = 0x0301 (little-endian)
                + script;

        InvocationScript deserializedScript = NeoSerializableInterface
                .from(Numeric.hexStringToByteArray(serializedScript), InvocationScript.class);

        assertArrayEquals(Numeric.hexStringToByteArray(script), deserializedScript.getScript());
    }

    @Test
    public void deserializeSignatureInvocationScript()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, DeserializationException {

        byte[] message = new byte[10];
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        byte[] signature = Sign.signMessage(message, keyPair).getConcatenated();

        String script = ""
                + PUSHDATA1.toString() + "40" // 64 bytes of signature
                + Numeric.toHexStringNoPrefix(signature); // signature

        String serializedScript = ""
                + "42" // VarInt 66 bytes for invocation script
                + script;

        InvocationScript deserializedScript = NeoSerializableInterface
                .from(Numeric.hexStringToByteArray(serializedScript), InvocationScript.class);
        assertArrayEquals(Numeric.hexStringToByteArray(script), deserializedScript.getScript());
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
    public void getSignatures() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {
        byte[] message = new byte[10];
        ECKeyPair keyPair = ECKeyPair.createEcKeyPair();
        Sign.SignatureData signature = Sign.signMessage(message, keyPair);
        InvocationScript inv = InvocationScript.fromSignatures(asList(signature, signature, signature));
        List<Sign.SignatureData> sigs = inv.getSignatures();
        assertTrue(sigs.stream().map(Sign.SignatureData::getConcatenated)
                .allMatch(s -> Arrays.equals(s, signature.getConcatenated())));
    }

}
