package io.neow3j.transaction;

import io.neow3j.protocol.core.response.OracleResponseCode;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash256;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionAttributesTest {

    @Test
    public void testSerialize_highPriorityAttribute() throws DeserializationException {
        // 01 - attribute type -> high priority

        byte[] bytes = hexStringToByteArray("01");
        TransactionAttribute attrActual = NeoSerializableInterface.from(bytes, HighPriorityAttribute.class);

        HighPriorityAttribute attrExpected = new HighPriorityAttribute();
        assertEquals(attrActual, attrExpected);
    }

    @Test
    public void testSerialize_oracleResponseAttribute() throws DeserializationException {
        // 11 - attribute type
        // 0000000000000000 - response id
        // 00 - code
        // 06 - result size
        // 6e656f77336a - result

        byte[] bytes = hexStringToByteArray("11000000000000000000066e656f77336a");
        TransactionAttribute attrActual = NeoSerializableInterface.from(bytes, OracleResponseAttribute.class);

        OracleResponseAttribute attrExpected = new OracleResponseAttribute(BigInteger.ZERO,
                OracleResponseCode.SUCCESS, "neow3j".getBytes());
        assertEquals(attrActual, attrExpected);

        // 11 - attribute type
        // 0100000000000000 - response id
        // 18 - code
        // 00 - result size

        bytes = hexStringToByteArray("1101000000000000001800");
        attrActual = NeoSerializableInterface.from(bytes, OracleResponseAttribute.class);

        attrExpected = new OracleResponseAttribute(BigInteger.ONE, OracleResponseCode.FORBIDDEN, "".getBytes());
        assertEquals(attrActual, attrExpected);
    }

    @Test
    public void testSerialize_notValidBeforeAttribute() throws DeserializationException {
        // 20 - attribute type
        // 10000000 - height 4 bytes

        byte[] bytes = hexStringToByteArray("2010000000");
        TransactionAttribute attrActual = NeoSerializableInterface.from(bytes, NotValidBeforeAttribute.class);

        NotValidBeforeAttribute attrExpected = new NotValidBeforeAttribute(BigInteger.valueOf(16));
        assertEquals(attrActual, attrExpected);
    }

    @Test
    public void testSerialize_conflictsAttribute() throws DeserializationException {
        // 21 - attribute type
        // 277d342421fb5373a4d2ee7254ee7a968da66b2179b27c855e0462434c6386fd - conflict hash256 little endian

        byte[] bytes = hexStringToByteArray("21277d342421fb5373a4d2ee7254ee7a968da66b2179b27c855e0462434c6386fd");
        TransactionAttribute attrActual = NeoSerializableInterface.from(bytes, ConflictsAttribute.class);

        ConflictsAttribute attrExpected = new ConflictsAttribute(
                new Hash256(reverseHexString("277d342421fb5373a4d2ee7254ee7a968da66b2179b27c855e0462434c6386fd")));
        assertEquals(attrActual, attrExpected);
    }

}
