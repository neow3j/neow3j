package io.neow3j.transaction;

import static io.neow3j.model.types.TransactionAttributeUsageType.SCRIPT;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.KeyUtils;
import io.neow3j.utils.Numeric;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Before;
import org.junit.Test;

public class TransactionTest {

    private ScriptHash account1;
    private ScriptHash account2;

    @Before
    public void setUp() throws Exception {
        account1 = ScriptHash.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
        account2 = ScriptHash.fromAddress("APLJBPhtRg2XLhtpxEHd6aRNL7YSLGH2ZL");
    }

    @Test
    public void buildMinimalTransaction() {
        long validUntilBlock = 100L;
        Transaction t = new Transaction.Builder()
            .validUntilBlock(validUntilBlock)
            .sender(account1)
            .build();

        assertThat(t.getVersion(), is(NeoConstants.CURRENT_TX_VERSION));
        assertThat(t.getNetworkFee(), is(0L));
        assertThat(t.getSystemFee(), is(0L));
        assertThat(t.getAttributes(), empty());
        assertThat(t.getCosigners(), containsInAnyOrder(Cosigner.calledByEntry(account1)));
        assertThat(t.getScript().length, is(0));
        assertThat(t.getNonce(), notNullValue());
        assertThat(t.getWitnesses(), empty());
    }

    @Test
    public void buildTransactionWithCorrectNonce() {
        Long nonce = ThreadLocalRandom.current().nextLong((long) Math.pow(2, 32));
        Transaction.Builder b = new Transaction.Builder()
            .validUntilBlock(1L)
            .sender(account1);
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
            .validUntilBlock(1L)
            .sender(account1);
        try {
            Long nonce = Integer.toUnsignedLong(-1) + 1;
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {
        }

        try {
            Long nonce = (long) Math.pow(2, 32);
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {
        }

        try {
            Long nonce = -1L;
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {
        }
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failBuildingTransactionWithNegativeValidUntilBlockNumber() {
        new Transaction.Builder().validUntilBlock(-1L);
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failBuildingTransactionWithTooHighValidUntilBlockNumber() {
        new Transaction.Builder().validUntilBlock((long) Math.pow(2, 32));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failBuildingTxWithoutValidUntilBlockProperty() {
        new Transaction.Builder()
            .sender(account1)
            .build();
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failBuildingTxWithoutSenderAccount() {
        new Transaction.Builder()
            .validUntilBlock(100L)
            .build();
    }

    @Test
    public void buildTxWithUpToMaxCosigners() throws InvalidAlgorithmParameterException,
        NoSuchAlgorithmException, NoSuchProviderException {

        Transaction.Builder b = new Transaction.Builder()
            .sender(account1)
            .validUntilBlock(1L);

        // Add two cosigners via varargs method.
        ScriptHash account1 = ScriptHash.fromPublicKey(
            new ECPublicKey(ECKeyPair.createEcKeyPair().getPublicKey()).getEncoded(true));
        ScriptHash account2 = ScriptHash.fromPublicKey(
            KeyUtils.publicKeyIntegerToByteArray(ECKeyPair.createEcKeyPair().getPublicKey()));
        b.cosigners(Cosigner.calledByEntry(account1), Cosigner.global(account2));

        // Add the rest of cosigners via method taking a set argument.
        List<Cosigner> cosigners = new ArrayList<>();
        for (int i = 3; i <= NeoConstants.MAX_COSIGNERS; i++) {
            ScriptHash account = ScriptHash.fromPublicKey(
                KeyUtils.publicKeyIntegerToByteArray(ECKeyPair.createEcKeyPair().getPublicKey()));
            cosigners.add(Cosigner.global(account));
        }
        Transaction tx = b.cosigners(cosigners).build();
        assertThat(tx.getCosigners(), hasSize(NeoConstants.MAX_COSIGNERS));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMultipleCosignersConcerningTheSameAccount1() {
        Transaction.Builder b = new Transaction.Builder();
        b.cosigners(Cosigner.global(account1), Cosigner.calledByEntry(account1));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMultipleCosignersConcerningTheSameAccount2() {
        Transaction.Builder b = new Transaction.Builder();
        b.cosigners(Cosigner.global(account1));
        b.cosigners(Cosigner.calledByEntry(account1));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMoreThanMaxCosignersToTxBuilder() throws
        InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        List<Cosigner> cosigners = new ArrayList<>();
        // Create one too many cosigners
        for (int i = 0; i <= NeoConstants.MAX_COSIGNERS; i++) {
            ScriptHash account = ScriptHash.fromPublicKey(
                KeyUtils.publicKeyIntegerToByteArray(ECKeyPair.createEcKeyPair().getPublicKey()));
            cosigners.add(Cosigner.global(account));
        }
        new Transaction.Builder()
            .sender(account1)
            .validUntilBlock(1L)
            .cosigners(cosigners);
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMoreThanMaxAttributesToTxBuilder() {
        List<TransactionAttribute> attrs = new ArrayList<>();
        // Create one too many attributes.
        for (int i = 0; i <= NeoConstants.MAX_TRANSACTION_ATTRIBUTES; i++) {
            attrs.add(new TransactionAttribute(TransactionAttributeUsageType.DESCRIPTION, "" + i));
        }
        new Transaction.Builder().attributes(attrs);
    }

    @Test
    public void serializeWithoutAttributesWitnessesAndCosigners() {
        Transaction tx = new Transaction.Builder()
            .sender(account1)
            .version((byte) 0)
            .nonce((long) 0x01020304)
            .systemFee(BigInteger.TEN.pow(8).longValue()) // 1 GAS
            .networkFee(1L) // 1 fraction of GAS
            .validUntilBlock(0x01020304L)
            .script(new byte[]{OpCode.PUSH1.getValue()})
            .build();

        byte[] actual = tx.toArray();
        byte[] expected = Numeric.hexStringToByteArray(""
            + "00" // version
            + "04030201"  // nonce
            + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash
            + "00e1f50500000000"  // system fee (1 GAS)
            + "0100000000000000"  // network fee (1 GAS fraction)
            + "04030201"  // valid until block
            + "00"  // no attributes
            + "01"  // one default cosigners
            + "23ba2703c53263e8d6e522dc32203339dcd8eee901" // calledByEntry cosigner
            + "0151"  // push1 script
            + "00"); // no witnesses

        assertArrayEquals(expected, actual);
    }

    @Test
    public void serializeWithAttributesWitnessesAndCosigners() {
        Transaction tx = new Transaction.Builder()
            .sender(account1)
            .version((byte) 0)
            .nonce((long) 0x01020304)
            .systemFee(BigInteger.TEN.pow(8).longValue()) // 1 GAS
            .networkFee(1L) // 1 fraction of GAS
            .validUntilBlock(0x01020304L)
            .script(new byte[]{OpCode.PUSH1.getValue()})
            .attributes(
                new TransactionAttribute(SCRIPT, account1.toArray()),
                new TransactionAttribute(SCRIPT, account2.toArray()))
            .cosigners(
                Cosigner.global(account1),
                Cosigner.calledByEntry(account2))
            .witnesses(new Witness(new byte[]{0x00}, new byte[]{0x00}))
            .build();

        byte[] actual = tx.toArray();
        byte[] expected = Numeric.hexStringToByteArray(""
            + "00" // version
            + "04030201"  // nonce
            + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash
            + "00e1f50500000000"  // system fee (1 GAS)
            + "0100000000000000"  // network fee (1 GAS fraction)
            + "04030201"  // valid until block
            + "02"  // 2 attributes
            + "2023ba2703c53263e8d6e522dc32203339dcd8eee9" // Script attribute 1
            + "2052eaab8b2aab608902c651912db34de36e7a2b0f" // Script attribute 2
            + "02"  // 2 cosigners
            + "23ba2703c53263e8d6e522dc32203339dcd8eee900" // global cosigner
            + "52eaab8b2aab608902c651912db34de36e7a2b0f01" // calledByEntry cosigner
            + "0151"  // push1 script
            + "01" // 1 witness
            + "01000100" // witness
        );

        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = Numeric.hexStringToByteArray(""
            + "00" // version
            + "04030201"  // nonce
            + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash
            + "00e1f50500000000"  // system fee (1 GAS)
            + "0100000000000000"  // network fee (1 GAS fraction)
            + "04030201"  // valid until block
            + "02"  // 2 attributes
            + "2023ba2703c53263e8d6e522dc32203339dcd8eee9" // Script attribute 1
            + "2052eaab8b2aab608902c651912db34de36e7a2b0f" // Script attribute 2
            + "02"  // 2 cosigners
            + "23ba2703c53263e8d6e522dc32203339dcd8eee900" // global cosigner
            + "52eaab8b2aab608902c651912db34de36e7a2b0f01" // calledByEntry cosigner
            + "0151"  // push1 script
            + "01" // 1 witness
            + "01000100"); /* witness*/

        Transaction tx = NeoSerializableInterface.from(data, Transaction.class);
        assertThat(tx.getVersion(), is((byte) 0));
        assertThat(tx.getNonce(), is(16_909_060L));
        assertThat(tx.getSender(), is(account1));
        assertThat(tx.getSystemFee(), is((long) Math.pow(10, 8)));
        assertThat(tx.getNetworkFee(), is(1L));
        assertThat(tx.getValidUntilBlock(), is(16_909_060L));
        assertThat(tx.getAttributes(), containsInAnyOrder(
            new TransactionAttribute(SCRIPT, account1.toArray()),
            new TransactionAttribute(SCRIPT, account2.toArray())));
        assertThat(tx.getCosigners(), containsInAnyOrder(
            Cosigner.global(account1),
            Cosigner.calledByEntry(account2)));
        assertArrayEquals(new byte[]{OpCode.PUSH1.getValue()}, tx.getScript());
        assertThat(tx.getWitnesses(), is(
            Arrays.asList(new Witness(new byte[]{0x00}, new byte[]{0x00}))));
    }
    
    @Test
    public void getSize() {
        Transaction tx = new Transaction.Builder()
            .sender(account1)
            .version((byte) 0)
            .nonce((long) 0x01020304)
            .systemFee(BigInteger.TEN.pow(8).longValue()) // 1 GAS
            .networkFee(1L) // 1 fraction of GAS
            .validUntilBlock(0x01020304L)
            .script(new byte[]{OpCode.PUSH1.getValue()})
            .attributes(
                new TransactionAttribute(SCRIPT, account1.toArray()),
                new TransactionAttribute(SCRIPT, account2.toArray()))
            .cosigners(
                Cosigner.global(account1),
                Cosigner.calledByEntry(account2))
            .witnesses(new Witness(new byte[]{0x00}, new byte[]{0x00}))
            .build();

        int expectedSize = 1 +  // Version
            4 +  // Nonce
            20 + // Sender script hash
            8 +  // System fee
            8 +  // Network fee
            4 + // Valid until block
            1 + // Byte for attributes list size
            1 + 20 +  // Attribute type byte and length of script hash attribute
            1 + 20 +  // Attribute type byte and length of script hash attribute
            1 + // Byte for cosigners list size
            1 + 20 + // Cosigner scope byte and cosigner script hash
            1 + 20 + // Cosigner scope byte and cosigner script hash
            1 + 1 + // Byte for script length and the actual length
            1 + // Byte for witnesses list size
            1 + 1 + // Byte for invocation script length and the actual length.
            1 + 1; // Byte for verifiaction script length and the actual length.

        assertThat(tx.getSize(), is(expectedSize));
    }

}