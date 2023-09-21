package io.neow3j.protocol.core.response;

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
        TransactionAttribute notValidBeforeAttribute = new NotValidBeforeAttribute(BigInteger.TEN);
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                notValidBeforeAttribute::asHighPriority);
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
    public void testAsConflicts_wrongType() {
        TransactionAttribute notValidBeforeAttribute = new NotValidBeforeAttribute(BigInteger.TEN);
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                notValidBeforeAttribute::asConflicts);
        assertThat(thrown.getMessage(),
                containsString("attribute is not of type " + TransactionAttributeType.CONFLICTS.jsonValue()));
    }

    @Test
    public void testAsNotValidBefore_wrongType() {
        TransactionAttribute highPriorityAttribute = new HighPriorityAttribute();
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                highPriorityAttribute::asNotValidBefore);
        assertThat(thrown.getMessage(),
                containsString("attribute is not of type " + TransactionAttributeType.NOT_VALID_BEFORE.jsonValue()));
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
