package io.neow3j.protocol.core.response;

import io.neow3j.crypto.Base64;
import io.neow3j.transaction.TransactionAttributeType;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransactionAttributeTest {

    @Test
    public void testAsHighPriority_wrongType() {
        TransactionAttribute oracleResponseAttribute = new OracleResponseAttribute(new OracleResponse(BigInteger.TEN,
                OracleResponseCode.SUCCESS, Base64.encode(new byte[]{0x00, 0x01})));
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                oracleResponseAttribute::asHighPriority);
        assertThat(thrown.getMessage(),
                containsString("attribute is not of type " + TransactionAttributeType.HIGH_PRIORITY.jsonValue()));
    }

    @Test
    public void testAsOracleResponse_wrongType() {
        TransactionAttribute notValidBeforeAttribute = new HighPriorityAttribute();
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                notValidBeforeAttribute::asOracleResponse);
        assertThat(thrown.getMessage(),
                containsString("attribute is not of type " + TransactionAttributeType.ORACLE_RESPONSE.jsonValue()));
    }

    @Test
    public void testHighPriority_transformFromSerializable() {
        TransactionAttribute actual = TransactionAttribute.fromSerializable(new io.neow3j.transaction.HighPriorityAttribute());
        assertEquals(actual, new HighPriorityAttribute());
    }

    @Test
    public void testOracleResponse_transformFromSerializable() {
        TransactionAttribute actual = TransactionAttribute.fromSerializable(
                new io.neow3j.transaction.OracleResponseAttribute(
                        BigInteger.TEN, OracleResponseCode.TIMEOUT, "hello".getBytes()
                )
        );

        OracleResponseAttribute expected = new OracleResponseAttribute(
                new OracleResponse(BigInteger.TEN, OracleResponseCode.TIMEOUT, "hello")
        );
        assertEquals(actual, expected);
    }

}
