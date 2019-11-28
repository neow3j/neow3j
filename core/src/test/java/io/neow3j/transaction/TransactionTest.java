package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.ScriptHash;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.Numeric;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TransactionTest {

    private ScriptHash account;

    @Before
    public void setUp() throws Exception {
        account = ScriptHash.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
    }

    @Test
    public void buildMinimalTransaction() {
        int validUntilBlock = 100;
        Transaction t = new Transaction.Builder()
                .validUntilBlock(validUntilBlock)
                .sender(account)
                .build();

        assertThat(t.getVersion(), is(NeoConstants.CURRENT_TX_VERSION));
        assertThat(t.getNetworkFee(), is(0L));
        assertThat(t.getSystemFee(), is(0L));
        assertThat(t.getAttributes(), empty());
        assertThat(t.getCosigners(), empty());
        assertThat(t.getScript().length, is(0));
        assertThat(t.getNonce(), notNullValue());
        assertThat(t.getWitnesses(), empty());
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failBuildingTxWithoutValidUntilBlockProperty() {
        Transaction t = new Transaction.Builder()
                .sender(account)
                .build();
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failBuildingTxWithoutSenderAccount() {
        Transaction t = new Transaction.Builder()
                .validUntilBlock(100)
                .build();
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMoreThanMaxCosignersToTxBuilder() {
        List<Cosigner> cosigners = new ArrayList<>();
        IntStream.range(0, NeoConstants.MAX_COSIGNERS + 1).forEach(
                i -> cosigners.add(Cosigner.calledByEntry(account)));
        // Use m
        new Transaction.Builder().cosigners(cosigners);
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMoreThanMaxAttributesToTxBuilder() {
        List<TransactionAttribute> attrs = new ArrayList<>();
        // Create one too many attributes.
        IntStream.range(0, NeoConstants.MAX_TRANSACTION_ATTRIBUTES + 1).forEach(
                i -> attrs.add(new TransactionAttribute()));
        new Transaction.Builder().attributes(attrs);
    }

    @Test
    public void addWitnessesInBuilderAndCheckOrdering() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as
        // integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as
        // integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        // first message has script hash 226912894221247444770625744046962264064050576762 (as
        // integer)
        byte[] m3 = Numeric.hexStringToByteArray("a7b3a191");
        Witness s1 = new Witness(m1, ScriptHash.fromScript(m1));
        Witness s2 = new Witness(m2, ScriptHash.fromScript(m2));
        Witness s3 = new Witness(m3, ScriptHash.fromScript(m3));

        Transaction tx = new Transaction.Builder()
                .sender(account)
                .validUntilBlock(1)
                .witness(s1)
                .witness(s2)
                .witness(s3)
                .build();
        assertEquals(tx.getWitnesses().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getWitnesses().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getWitnesses().get(2).getScriptHash(), s2.getScriptHash());

        // Add in reverse order
        tx = new Transaction.Builder()
                .sender(account)
                .validUntilBlock(1)
                .witness(s3)
                .witness(s2)
                .witness(s1)
                .build();
        assertEquals(tx.getWitnesses().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getWitnesses().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getWitnesses().get(2).getScriptHash(), s2.getScriptHash());

        // Add all together
        tx = new Transaction.Builder()
                .sender(account)
                .validUntilBlock(1)
                .witnesses(Arrays.asList(s3, s1, s2))
                .build();
        assertEquals(tx.getWitnesses().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getWitnesses().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getWitnesses().get(2).getScriptHash(), s2.getScriptHash());
    }

    @Test
    public void addWitnessesInTxAndCheckOrdering() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as
        // integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as
        // integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        // first message has script hash 226912894221247444770625744046962264064050576762 (as
        // integer)
        byte[] m3 = Numeric.hexStringToByteArray("a7b3a191");
        Witness s1 = new Witness(m1, ScriptHash.fromScript(m1));
        Witness s2 = new Witness(m2, ScriptHash.fromScript(m2));
        Witness s3 = new Witness(m3, ScriptHash.fromScript(m3));

        Transaction tx = new Transaction.Builder()
                .sender(account)
                .validUntilBlock(1)
                .witness(s1).build();
        tx.addWitness(s2);
        tx.addWitness(s3);
        assertEquals(tx.getWitnesses().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getWitnesses().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getWitnesses().get(2).getScriptHash(), s2.getScriptHash());

        // Add in different order
        tx = new Transaction.Builder()
                .sender(account)
                .validUntilBlock(1)
                .build();
        tx.addWitness(s2);
        tx.addWitness(s1);
        tx.addWitness(s3);
        assertEquals(tx.getWitnesses().get(0).getScriptHash(), s1.getScriptHash());
        assertEquals(tx.getWitnesses().get(1).getScriptHash(), s3.getScriptHash());
        assertEquals(tx.getWitnesses().get(2).getScriptHash(), s2.getScriptHash());
    }

}