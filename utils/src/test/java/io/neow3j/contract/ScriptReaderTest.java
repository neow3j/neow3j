package io.neow3j.contract;

import io.neow3j.utils.Numeric;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ScriptReaderTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testReadInteger() {
        byte[] number = Numeric.hexStringToByteArray("4F");
        assertEquals(BigInteger.ONE.negate(), ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("00");
        assertEquals(BigInteger.ZERO, ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("51");
        assertEquals(BigInteger.ONE, ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("60");
        assertEquals(BigInteger.valueOf(16), ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("0101");
        assertEquals(BigInteger.ONE, ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("01ff");
        assertEquals(BigInteger.valueOf(255), ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("4b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001");
        assertEquals(BigInteger.ONE, ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("4bffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        assertEquals(new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16), ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("4c0101");
        assertEquals(BigInteger.ONE, ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("4c04ffffffff");
        assertEquals(new BigInteger("ffffffff", 16), ScriptReader.readInteger(number));

        number = Numeric.hexStringToByteArray("4d0001ff");
        assertEquals(BigInteger.valueOf(255), ScriptReader.readInteger(number));
    }

    @Test
    public void testFailReadInteger() {
        List<String> hexNumberss = Arrays.asList(
                "01",
                "04ffffff",
                "4c04ffffff",
                "4c01",
                "4c10ffffffffffffffffffffffffffffff",
                "4d01",
                "4d0001",
                "4d000201",
                "4e00000001",
                "4e0000000affffffffffffffffff"
        );

        for (String hexNum : hexNumberss) {
            try {
                ScriptReader.readInteger(Numeric.hexStringToByteArray(hexNum));
                fail();
            } catch (ArrayIndexOutOfBoundsException e) { }
        }
    }
}