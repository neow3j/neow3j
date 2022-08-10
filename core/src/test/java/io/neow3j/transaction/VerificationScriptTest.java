package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.script.InteropService;
import io.neow3j.script.InvocationScript;
import io.neow3j.script.OpCode;
import io.neow3j.script.VerificationScript;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.ScriptFormatException;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static io.neow3j.constants.NeoConstants.VERIFICATION_SCRIPT_SIZE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerificationScriptTest {

    @Test
    public void testFromPublicKey() {
        final String key = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        final ECPublicKey ecPubKey = new ECPublicKey(Numeric.hexStringToByteArray(key));
        VerificationScript script = new VerificationScript(ecPubKey);

        byte[] expected = Numeric.hexStringToByteArray(""
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key // public key
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash()
        );
        assertArrayEquals(expected, script.getScript());
    }

    @Test
    public void testFromPublicKeys() {
        final String key1 = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        final String key2 = "03eda286d19f7ee0b472afd1163d803d620a961e1581a8f2704b52c0285f6e022d";
        final String key3 = "03ac81ec17f2f15fd6d193182f927c5971559c2a32b9408a06fec9e711fb7ca02e";

        List<ECPublicKey> publicKeys = Arrays.asList(
                new ECPublicKey(Numeric.hexStringToByteArray(key1)),
                new ECPublicKey(Numeric.hexStringToByteArray(key2)),
                new ECPublicKey(Numeric.hexStringToByteArray(key3)));
        VerificationScript script = new VerificationScript(publicKeys, 2);

        // Keys should be ordered key1, key3, key2.
        byte[] expected = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // n = 2, signing threshold
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key1 // public key
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key3 // public key
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key2 // public key
                + OpCode.PUSH3.toString() // m = 3, number of keys
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKMULTISIG.getHash()
        );
        assertArrayEquals(expected, script.getScript());
    }

    @Test
    public void testSerialize() {
        final String key = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        final ECPublicKey ecPubKey = new ECPublicKey(Numeric.hexStringToByteArray(key));
        VerificationScript script = new VerificationScript(ecPubKey);

        byte[] expected = Numeric.hexStringToByteArray(""
                + Numeric.toHexStringNoPrefix((byte) VERIFICATION_SCRIPT_SIZE) // Var Int
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key // public key
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash()
        );
        assertArrayEquals(expected, script.toArray());
    }

    @Test
    public void testDeserialize() throws DeserializationException {
        final String key = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        String script = ""
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key // public key
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash();

        byte[] serialized = Numeric.hexStringToByteArray(""
                + Numeric.toHexStringNoPrefix((byte) VERIFICATION_SCRIPT_SIZE) // Var Int
                + script);

        VerificationScript verificationScript =
                NeoSerializableInterface.from(serialized, VerificationScript.class);

        assertThat(verificationScript.getScript(), is(Numeric.hexStringToByteArray(script)));
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
        sb.append(OpCode.SYSCALL.toString());
        sb.append(InteropService.SYSTEM_CRYPTO_CHECKMULTISIG.getHash());
        byte[] script = Numeric.hexStringToByteArray(sb.toString());
        assertEquals(2, new VerificationScript(script).getSigningThreshold());

        // Signing threshold: 127
        StringBuilder sb2 = new StringBuilder();
        sb2.append(OpCode.PUSHINT8.toString()); // signing threshold
        sb2.append("7f"); // signing threshold
        IntStream.range(0, 127).forEach(i -> sb2.append(key));
        sb2.append(OpCode.PUSHINT8.toString()); // signing threshold
        sb2.append("7f"); // number of public keys
        sb2.append(OpCode.SYSCALL.toString());
        sb2.append(InteropService.SYSTEM_CRYPTO_CHECKMULTISIG.getHash());
        script = Numeric.hexStringToByteArray(sb2.toString());
        assertEquals(127, new VerificationScript(script).getSigningThreshold());
    }

    @Test
    public void throwOnInvalidScriptFormat1() {
        VerificationScript script = new VerificationScript(Numeric.hexStringToByteArray("0123456789abcdef"));
        assertThrows(ScriptFormatException.class, script::getPublicKeys);
    }

    @Test
    public void throwOnInvalidScriptFormat2() {
        VerificationScript script = new VerificationScript(Numeric.hexStringToByteArray("0123456789abcdef"));
        assertThrows(ScriptFormatException.class, script::getSigningThreshold);
    }

    @Test
    public void throwOnInvalidScriptFormat3() {
        VerificationScript script = new VerificationScript(Numeric.hexStringToByteArray("0123456789abcdef"));
        assertThrows(ScriptFormatException.class, script::getNrOfAccounts);
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
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash());
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
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKMULTISIG.getHash());
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
    public void getPublicKeysFromSingleSigScript() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash());
        VerificationScript s = new VerificationScript(scriptBytes);
        List<ECPublicKey> pubKeys = s.getPublicKeys();
        assertThat(pubKeys, hasSize(1));
        assertThat(Numeric.toHexStringNoPrefix(pubKeys.get(0).getEncoded(true)),
                is("02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef"));
    }

    @Test
    public void getPublicKeysFromMutliSigScript() {
        byte[] scriptBytes = Numeric.hexStringToByteArray(""
                + OpCode.PUSH2.toString() // signing threshold
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9" // key
                + OpCode.PUSHDATA1.toString() + "21"// PUSHDATA1 and 33 bytes of key
                + "03f0f9b358dfed564e74ffe242713f8bc866414226649f59859b140a130818898b" // key
                + OpCode.PUSH3.toString() // number of public keys
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKMULTISIG.getHash());
        VerificationScript s = new VerificationScript(scriptBytes);
        List<ECPublicKey> pubKeys = s.getPublicKeys();
        assertThat(pubKeys, hasSize(3));
        assertThat(Numeric.toHexStringNoPrefix(pubKeys.get(0).getEncoded(true)),
                is("02028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26e8861699ef"));
        assertThat(Numeric.toHexStringNoPrefix(pubKeys.get(1).getEncoded(true)),
                is("031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c9"));
        assertThat(Numeric.toHexStringNoPrefix(pubKeys.get(2).getEncoded(true)),
                is("03f0f9b358dfed564e74ffe242713f8bc866414226649f59859b140a130818898b"));
    }

}
