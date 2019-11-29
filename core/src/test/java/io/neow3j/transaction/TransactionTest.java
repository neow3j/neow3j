package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.Keys;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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

    @Test
    public void buildTransactionWithCorrectNonce() {
        Long nonce = ThreadLocalRandom.current().nextLong((long) Math.pow(2, 32));
        Transaction.Builder b = new Transaction.Builder()
                .validUntilBlock(1)
                .sender(account);
        Transaction t = b.nonce(nonce).build();
        assertThat(t.getNonce(), is(nonce));

        nonce = 0L;
        t = b.nonce(0L).build();
        assertThat(t.getNonce(), is(nonce));

        nonce = (long) Math.pow(2, 32) - 1;
        t = b.nonce(nonce).build();
        assertThat(t.getNonce(), is(nonce));

        nonce = Integer.toUnsignedLong(-1);
        t = b.nonce(nonce).build();
        assertThat(t.getNonce(), is(nonce));
    }

    @Test
    public void failBuildingTransactionWithIncorrectNonce() {
        Transaction.Builder b = new Transaction.Builder()
                .validUntilBlock(1)
                .sender(account);
        try {
            Long nonce = Integer.toUnsignedLong(-1) + 1;
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {}

        try {
            Long nonce = (long) Math.pow(2, 32);
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {}

        try {
            Long nonce = -1L;
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {}
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failBuildingTransactionWithIncorrectValidUntilBlockNumber() {
        Transaction.Builder b = new Transaction.Builder().validUntilBlock(-1);
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

    @Test
    public void buildTxWithUpToMaxCosigners() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Transaction.Builder b = new Transaction.Builder()
                .sender(account)
                .validUntilBlock(1);

        // Add two cosigners via varargs method.
        ScriptHash account1 = ScriptHash.fromPublicKey(
                Keys.publicKeyIntegerToByteArray(ECKeyPair.createEcKeyPair().getPublicKey()));
        ScriptHash account2 = ScriptHash.fromPublicKey(
                Keys.publicKeyIntegerToByteArray(ECKeyPair.createEcKeyPair().getPublicKey()));
        b.cosigners(Cosigner.calledByEntry(account1), Cosigner.global(account2));

        // Add the rest of cosigners via method taking a set argument.
        Set<Cosigner> cosigners = new HashSet<>();
        for (int i = 3; i <= NeoConstants.MAX_COSIGNERS; i++) {
            ScriptHash account = ScriptHash.fromPublicKey(
                    Keys.publicKeyIntegerToByteArray(ECKeyPair.createEcKeyPair().getPublicKey()));
            cosigners.add(Cosigner.global(account));
        }
        Transaction tx = b.cosigners(cosigners).build();
        assertThat(tx.getCosigners(), hasSize(NeoConstants.MAX_COSIGNERS));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMultipleCosignersConcerningTheSameAccount1() {
        Transaction.Builder b = new Transaction.Builder();
        b.cosigners(Cosigner.global(account), Cosigner.calledByEntry(account));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMultipleCosignersConcerningTheSameAccount2() {
        Transaction.Builder b = new Transaction.Builder();
        b.cosigners(Cosigner.global(account));
        b.cosigners(Cosigner.calledByEntry(account));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMoreThanMaxCosignersToTxBuilder() throws
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        Set<Cosigner> cosigners = new HashSet<>();
        // Create one too many cosigners
        for (int i = 0; i <= NeoConstants.MAX_COSIGNERS; i++) {
            ScriptHash account = ScriptHash.fromPublicKey(
                    Keys.publicKeyIntegerToByteArray(ECKeyPair.createEcKeyPair().getPublicKey()));
            cosigners.add(Cosigner.global(account));
        }
        Transaction.Builder tx = new Transaction.Builder()
                .sender(account)
                .validUntilBlock(1)
                .cosigners(cosigners);
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMoreThanMaxAttributesToTxBuilder() {
        Set<TransactionAttribute> attrs = new HashSet<>();
        // Create one too many attributes.
        for (int i = 0; i <= NeoConstants.MAX_TRANSACTION_ATTRIBUTES; i++) {
            attrs.add(new TransactionAttribute(TransactionAttributeUsageType.DESCRIPTION, "" + i));
        }
        new Transaction.Builder().attributes(attrs);
    }

}