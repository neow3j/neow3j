package io.neow3j.transaction;

import io.neow3j.crypto.Hash;
import io.neow3j.crypto.transaction.RawScript;
import io.neow3j.crypto.transaction.RawTransaction;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class RawTransactionTest {

    @Test
    public void addWitnessesInBuilderAndCheckOrdering() {
        // first message has script hash 1437071892243240092295917421623735140333232716571 (as integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 410686057768940504778433281623606822736539746952 (as integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        // first message has script hash 700730126708401329226902600088401122400206372647 (as integer)
        byte[] m3 = Numeric.hexStringToByteArray("a7b3a191");
        RawScript s1 = new RawScript(m1, Numeric.toHexStringNoPrefix(Hash.calculateScriptHash(m1)));
        RawScript s2 = new RawScript(m2, Numeric.toHexStringNoPrefix(Hash.calculateScriptHash(m2)));
        RawScript s3 = new RawScript(m3, Numeric.toHexStringNoPrefix(Hash.calculateScriptHash(m3)));

        RawTransaction tx = new ContractTransaction.Builder()
                .script(s1)
                .script(s2)
                .script(s3)
                .build();
        assertEquals(tx.getScripts().get(0).getScriptHash(), s2.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s1.getScriptHash());

        // Add in reverse order
        tx = new ContractTransaction.Builder()
                .script(s3)
                .script(s2)
                .script(s1)
                .build();
        assertEquals(tx.getScripts().get(0).getScriptHash(), s2.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s1.getScriptHash());

        // Add all together
        tx = new ContractTransaction.Builder()
                .scripts(Arrays.asList(s3, s1, s2))
                .build();
        assertEquals(tx.getScripts().get(0).getScriptHash(), s2.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s1.getScriptHash());
    }

    @Test
    public void addWitnessesInTxAndCheckOrdering() {
        // first message has script hash 1437071892243240092295917421623735140333232716571 (as integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 410686057768940504778433281623606822736539746952 (as integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        // first message has script hash 700730126708401329226902600088401122400206372647 (as integer)
        byte[] m3 = Numeric.hexStringToByteArray("a7b3a191");
        RawScript s1 = new RawScript(m1, Numeric.toHexStringNoPrefix(Hash.calculateScriptHash(m1)));
        RawScript s2 = new RawScript(m2, Numeric.toHexStringNoPrefix(Hash.calculateScriptHash(m2)));
        RawScript s3 = new RawScript(m3, Numeric.toHexStringNoPrefix(Hash.calculateScriptHash(m3)));

        RawTransaction tx = new ContractTransaction.Builder()
                .script(s1).build();
        tx.addScript(s2);
        tx.addScript(s3);
        assertEquals(tx.getScripts().get(0).getScriptHash(), s2.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s1.getScriptHash());

        // Add in different order
        tx = new ContractTransaction.Builder().build();
        tx.addScript(s2);
        tx.addScript(s1);
        tx.addScript(s3);
        assertEquals(tx.getScripts().get(0).getScriptHash(), s2.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s1.getScriptHash());
    }

}