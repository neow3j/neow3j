package io.neow3j.transaction;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.neow3j.constants.OpCode;
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
        String key = OpCode.PUSHDATA1.toString() + "21"
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef";

        // Signing threshold: 2
        StringBuilder sb = new StringBuilder();
        sb.append(OpCode.PUSH2.toString()); // signing threshold
        IntStream.range(0, 3).forEach(i -> sb.append(key));
        sb.append(OpCode.PUSH3.toString()); // number of public keys
        sb.append(OpCode.PUSHNULL.toString());
        sb.append(OpCode.SYSCALL.toString());
        sb.append("3073b3bb"); // sys call
        byte[] script = Numeric.hexStringToByteArray(sb.toString());
        assertEquals(2, new VerificationScript(script).getSigningThreshold());

        // Signing threshold: 127
        StringBuilder sb2 = new StringBuilder();
        sb2.append(OpCode.PUSHINT8.toString()); // signing threshold
        sb2.append("7f"); // signing threshold
        IntStream.range(0, 127).forEach(i -> sb2.append(key));
        sb2.append(OpCode.PUSHINT8.toString()); // signing threshold
        sb2.append("7f"); // number of public keys
        sb2.append(OpCode.PUSHNULL.toString());
        sb2.append(OpCode.SYSCALL.toString());
        sb2.append("3073b3bb"); // sys call
        script = Numeric.hexStringToByteArray(sb2.toString());
        assertEquals(127, new VerificationScript(script).getSigningThreshold());
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
    public void isSingleSigScript() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHNULL.toString() // null message
                + OpCode.SYSCALL.toString() + "0a906ad4"); // sys call
        VerificationScript s = new VerificationScript(scriptBytes);
        assertTrue(s.isSingleSigScript());
    }

    @Test
    public void isMultiSigScript() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "03f0f9b358dfed564e74ffe242713f8bc866414226649f59859b140a130818898b" // key
                + OpCode.PUSH3.toString() // number of public keys
                + OpCode.PUSHNULL.toString() // null message
                + OpCode.SYSCALL.toString() + "3073b3bb"); // sys call to
        VerificationScript s = new VerificationScript(scriptBytes);
        assertTrue(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseScrtipIsTooShort() {
        byte[] scriptBytes = Numeric.hexStringToByteArray("a89429c3be9f");
        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseNIsSmallerThanOne() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH0.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSH1.toString() // number of public keys
                + OpCode.PUSHNULL.toString() // null message
                + OpCode.SYSCALL.toString() + "3073b3bb"); // sys call to

        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseScriptAppruptlyEnds() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef"); // key
        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseOfWrongPushData() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHDATA1.toString() + "43"// PUSHDATA1 and wrong number of bytes for key
                + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9" // key
                + OpCode.PUSH2.toString() // number of public keys
                + OpCode.PUSHNULL.toString() // null message
                + OpCode.SYSCALL.toString() + "3073b3bb"); // sys call to
        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseNLargerThanM() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH3.toString() // n > m
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and wrong number of bytes for key
                + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9" // key
                + OpCode.PUSH2.toString() // number of public keys
                + OpCode.PUSHNULL.toString() // null message
                + OpCode.SYSCALL.toString() + "3073b3bb"); // sys call to
        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseMIsIncorrect() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and wrong number of bytes for key
                + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9" // key
                + OpCode.PUSH3.toString() // m not congruent with number of keys
                + OpCode.PUSHNULL.toString() // null message
                + OpCode.SYSCALL.toString() + "3073b3bb"); // sys call to
        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseOfMissingPUSHNULL() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and wrong number of bytes for key
                + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9" // key
                + OpCode.PUSH2.toString() // number of public keys
                // PUSHNULL missing
                + OpCode.SYSCALL.toString() + "3073b3bb"); // sys call to
        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseOfMissingSYSCALL() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and wrong number of bytes for key
                + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9" // key
                + OpCode.PUSH2.toString() // number of public keys
                + OpCode.PUSHNULL.toString() // m not congruent with number of keys
                // SYSCALL missing
                + "3073b3bb");
        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void failIsMultiSigScriptBecauseOfWrongInteropService() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and wrong number of bytes for key
                + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9" // key
                + OpCode.PUSH3.toString() // number of public keys
                + OpCode.PUSHNULL.toString() // m not congruent with number of keys
                + OpCode.SYSCALL.toString() + "103ab300"); // wrong interop service
        VerificationScript s = new VerificationScript(scriptBytes);
        assertFalse(s.isMultiSigScript());
    }

    @Test
    public void getPublicKeys() {
        fail();
    }
}