package io.neow3j.protocol.core.response;

import io.neow3j.transaction.ConflictsAttribute;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.types.Hash256;
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
        TransactionAttribute actual = TransactionAttribute.fromSerializable(
                new io.neow3j.transaction.HighPriorityAttribute()
        );
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

    @Test
    public void testNotValidBefore_transformFromSerializable() {
        BigInteger notValidBefore = new BigInteger("438034626");
        TransactionAttribute actual = TransactionAttribute.fromSerializable(
                new io.neow3j.transaction.NotValidBeforeAttribute(notValidBefore)
        );

        NotValidBeforeAttribute expected = new NotValidBeforeAttribute(notValidBefore);
        assertEquals(actual, expected);
    }

    @Test
    public void testConflicts_transformFromSerializable() {
        Hash256 conflictsHash = new Hash256("0xf4609b99e171190c22adcf70c88a7a14b5b530914d2398287bd8bb7ad95a661c");
        TransactionAttribute actual = TransactionAttribute.fromSerializable(
                new io.neow3j.transaction.ConflictsAttribute(conflictsHash)
        );

        ConflictsAttribute expected = new ConflictsAttribute(conflictsHash);
        assertEquals(actual, expected);
    }

}
