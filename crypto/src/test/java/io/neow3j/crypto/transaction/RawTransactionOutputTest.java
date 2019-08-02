package io.neow3j.crypto.transaction;

import io.neow3j.model.types.NEOAsset;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class RawTransactionOutputTest {

    @Test
    public void testEqualsWithDifferentDecimalNotation() {
        RawTransactionOutput o1 = new RawTransactionOutput(NEOAsset.HASH_ID, "15983.0", "address");
        RawTransactionOutput o2 = new RawTransactionOutput(NEOAsset.HASH_ID, "15983", "address");
        assertEquals(o1, o2);
    }

}