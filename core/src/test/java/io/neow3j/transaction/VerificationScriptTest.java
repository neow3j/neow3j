package io.neow3j.transaction;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.ScriptFormatException;
import io.neow3j.utils.Numeric;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Test;

public class VerificationScriptTest {

    @Test
    public void testFromPublicKey() {
        ECPublicKey key = new ECPublicKey(Numeric.hexStringToByteArray(TestKeys.pubKey1));
        VerificationScript script = new VerificationScript(key);

        assertArrayEquals(
                Numeric.hexStringToByteArray(TestKeys.verificationScript1),
                script.getScript());
    }

    @Test
    public void testFromPublicKeys() {
        List<ECPublicKey> publicKeys = Arrays.asList(
                new ECPublicKey(Numeric.hexStringToByteArray(TestKeys.pubKey2_1)),
                new ECPublicKey(Numeric.hexStringToByteArray(TestKeys.pubKey2_2)));
        VerificationScript script = new VerificationScript(publicKeys, 2);

        assertArrayEquals(
                Numeric.hexStringToByteArray(TestKeys.verificationScript2),
                script.getScript());
    }

    @Test
    public void testSerialize() {
        final String expected = "27" + TestKeys.verificationScript1;
        ECPublicKey key = new ECPublicKey(Numeric.hexStringToByteArray(TestKeys.pubKey1));
        VerificationScript veriScript = new VerificationScript(key);
        assertArrayEquals(Numeric.hexStringToByteArray(expected), veriScript.toArray());


    }

    @Test
    public void testDeserialize() throws DeserializationException {
        final String serialized = "27" + TestKeys.verificationScript1;
        VerificationScript script = NeoSerializableInterface.from(
                Numeric.hexStringToByteArray(serialized), VerificationScript.class);

        assertThat(script.getScript(),
                is(Numeric.hexStringToByteArray(TestKeys.verificationScript1)));
    }

    @Test
    public void testGetSigningThreshold() {
        String key = "21" + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef";

        // Signing threshold: 2
        StringBuilder script = new StringBuilder("52"); // signing threshold
        IntStream.range(0, 3).forEach(i -> script.append(key));
        script.append("53"); // number of public keys
        script.append("68c7c34cba"); // sys call
        byte[] scriptBytes = Numeric.hexStringToByteArray(script.toString());
        int th = new VerificationScript(scriptBytes).getSigningThreshold();
        assertEquals(2, th);

        // Signing threshold: 255
        StringBuilder script2 = new StringBuilder("02ff00"); // signing threshold
        IntStream.range(0, 16).forEach(i -> script2.append(key));
        script2.append("02ff00"); // number of public keys
        script2.append("68c7c34cba"); // sys call
        scriptBytes = Numeric.hexStringToByteArray(script2.toString());
        th = new VerificationScript(scriptBytes).getSigningThreshold();
        assertEquals(255, th);

        // Signing threshold: 1024
        StringBuilder script3 = new StringBuilder("020004ae"); // signing threshold
        IntStream.range(0, 16).forEach(i -> script3.append(key));
        script3.append("020004ae"); // number of public keys
        script3.append("68c7c34cba"); // sys call
        scriptBytes = Numeric.hexStringToByteArray(script3.toString());
        th = new VerificationScript(scriptBytes).getSigningThreshold();
        assertEquals(1024, th);
    }

    @Test(expected = ScriptFormatException.class)
    public void throwOnInvalidScriptFormat1() {
        VerificationScript script = new VerificationScript(
                Numeric.hexStringToByteArray("0123456789abcdef"));
       script.getPublicKeys();
    }

    @Test(expected = ScriptFormatException.class)
    public void throwOnInvalidScriptFormat2() {
        VerificationScript script = new VerificationScript(
                Numeric.hexStringToByteArray("0123456789abcdef"));
        script.getSigningThreshold();
    }

    @Test(expected = ScriptFormatException.class)
    public void throwOnInvalidScriptFormat3() {
        VerificationScript script = new VerificationScript(
                Numeric.hexStringToByteArray("0123456789abcdef"));
        script.getNrOfAccounts();
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

    @Test
    public void getPublicKeys() {
       fail();
    }
}