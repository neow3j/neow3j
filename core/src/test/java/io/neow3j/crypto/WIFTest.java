package io.neow3j.crypto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.neow3j.utils.Numeric;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WIFTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testWIF() {
        final String privateKey = "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3";
        byte[] privateKeyFromWIF = WIF.getPrivateKeyFromWIF("L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13A");
        assertThat(Numeric.toHexStringNoPrefix(privateKeyFromWIF), is(privateKey));
    }

    @Test
    public void testWIFLargerThan38() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Incorrect WIF format.");
        WIF.getPrivateKeyFromWIF("L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13Ahc7S");
    }

    @Test
    public void testWIFFirstByteDifferentThan0x80() {
        byte[] wifBytes = Base58.decode("L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13A");
        wifBytes[0] = (byte) 0x81;
        String wifString = Base58.encode(wifBytes);
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Incorrect WIF format.");
        WIF.getPrivateKeyFromWIF(wifString);
    }

    @Test
    public void testWIF33ByteDifferentThan0x01() {
        byte[] wifBytes = Base58.decode("L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13A");
        wifBytes[33] = (byte) 0x00;
        String wifString = Base58.encode(wifBytes);
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Incorrect WIF format.");
        WIF.getPrivateKeyFromWIF(wifString);
    }

    @Test
    public void testWIFNull() {
        exceptionRule.expect(NullPointerException.class);
        WIF.getPrivateKeyFromWIF(null);
    }

    @Test
    public void privateKeyToWif() {
        final String privateKey = "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3";
        String result = WIF.getWIFFromPrivateKey(Numeric.hexStringToByteArray(privateKey));
        String expected = "L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13A";
        assertThat(result, is(expected));
    }

    @Test
    public void failUsingWrongSizePrivateKey() {
        final String privateKey = "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3";
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Given key is not of expected length (32 bytes).");
        WIF.getWIFFromPrivateKey(Numeric.hexStringToByteArray(privateKey));
    }

}
