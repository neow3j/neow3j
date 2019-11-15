package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class RawTransactionTest {

    @Test
    public void addWitnessesInBuilderAndCheckOrdering() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        // first message has script hash 226912894221247444770625744046962264064050576762 (as integer)
        byte[] m3 = Numeric.hexStringToByteArray("a7b3a191");
        RawScript s1 = new RawScript(m1, ScriptHash.fromScript(m1));
        RawScript s2 = new RawScript(m2, ScriptHash.fromScript(m2));
        RawScript s3 = new RawScript(m3, ScriptHash.fromScript(m3));

        RawTransaction tx = new ContractTransaction.Builder()
                .script(s1)
                .script(s2)
                .script(s3)
                .build();
        assertEquals(tx.getScripts().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s2.getScriptHash());

        // Add in reverse order
        tx = new ContractTransaction.Builder()
                .script(s3)
                .script(s2)
                .script(s1)
                .build();
        assertEquals(tx.getScripts().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s2.getScriptHash());

        // Add all together
        tx = new ContractTransaction.Builder()
                .scripts(Arrays.asList(s3, s1, s2))
                .build();
        assertEquals(tx.getScripts().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s2.getScriptHash());
    }

    @Test
    public void addWitnessesInTxAndCheckOrdering() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        // first message has script hash 226912894221247444770625744046962264064050576762 (as integer)
        byte[] m3 = Numeric.hexStringToByteArray("a7b3a191");
        RawScript s1 = new RawScript(m1, ScriptHash.fromScript(m1));
        RawScript s2 = new RawScript(m2, ScriptHash.fromScript(m2));
        RawScript s3 = new RawScript(m3, ScriptHash.fromScript(m3));

        RawTransaction tx = new ContractTransaction.Builder()
                .script(s1).build();
        tx.addScript(s2);
        tx.addScript(s3);
        assertEquals(tx.getScripts().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s2.getScriptHash());

        // Add in different order
        tx = new ContractTransaction.Builder().build();
        tx.addScript(s2);
        tx.addScript(s1);
        tx.addScript(s3);
        assertEquals(tx.getScripts().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getScripts().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getScripts().get(2).getScriptHash(), s2.getScriptHash());
    }

}