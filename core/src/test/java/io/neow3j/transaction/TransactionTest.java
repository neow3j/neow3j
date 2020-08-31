package io.neow3j.transaction;

import static org.hamcrest.Matchers.contains;
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
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.NeoConfig;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Before;
import org.junit.Test;

public class TransactionTest {

    private ScriptHash account1;
    private ScriptHash account2;
    private ScriptHash account3;

    @Before
    public void setUp() {
        account1 = ScriptHash.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
        account2 = ScriptHash.fromAddress("APLJBPhtRg2XLhtpxEHd6aRNL7YSLGH2ZL");
        account3 = ScriptHash.fromAddress("AZt9DgwW8PKSEQsa9QLX86SyE1DSNjSbsS");
    }

    @Test
    public void buildMinimalTransaction() {
        long validUntilBlock = 100L;
        Transaction t = new Transaction.Builder()
                .validUntilBlock(validUntilBlock)
                .signers(Signer.feeOnly(account1))
                .build();

        assertThat(t.getVersion(), is(NeoConstants.CURRENT_TX_VERSION));
        assertThat(t.getNetworkFee(), is(0L));
        assertThat(t.getSystemFee(), is(0L));
        assertThat(t.getAttributes(), is(notNullValue()));
        assertThat(t.getAttributes(), hasSize(0));
        assertThat(t.getSigners(), hasSize(1));
        assertThat(t.getSigners().get(0).getScriptHash(), is(account1));
        assertThat(t.getSigners().get(0).getScopes(), contains(WitnessScope.FEE_ONLY));
        assertThat(t.getScript().length, is(0));
        assertThat(t.getNonce(), notNullValue());
        assertThat(t.getWitnesses(), empty());
    }

    @Test
    public void buildTransactionWithCorrectNonce() {
        Long nonce = ThreadLocalRandom.current().nextLong((long) Math.pow(2, 32));
        Transaction.Builder b = new Transaction.Builder()
                .validUntilBlock(1L)
                .signers(Signer.calledByEntry(account1));
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
                .signers(Signer.calledByEntry(account1));
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
                .signers(Signer.calledByEntry(account1))
                .build();
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failBuildingTxWithoutSenderAccount() {
        new Transaction.Builder()
                .validUntilBlock(100L)
                .build();
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMultipleSignersConcerningTheSameAccount1() {
        Transaction.Builder b = new Transaction.Builder();
        b.signers(Signer.global(account1), Signer.calledByEntry(account1));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMultipleSignersConcerningTheSameAccount2() {
        Transaction.Builder b = new Transaction.Builder();
        b.signers(Signer.global(account1));
        b.signers(Signer.calledByEntry(account1));
    }

    @Test(expected = TransactionConfigurationException.class)
    public void failAddingMoreThanMaxAttributesToTxBuilder() {
        // Add one too many attributes.
        Signer[] singers =
                new Signer[NeoConstants.MAX_SIGNER_SUBITEMS + 1];
        for (int i = 0; i <= NeoConstants.MAX_SIGNER_SUBITEMS; i++) {
            singers[i] = new Signer();
        }
        new Transaction.Builder().signers(singers);
    }

    @Test
    public void serializeWithoutAttributesAndWitnesses() {
        Transaction tx = new Transaction.Builder()
                .signers(Signer.calledByEntry(account1))
                .version((byte) 0)
                .nonce((long) 0x01020304)
                .systemFee(BigInteger.TEN.pow(8).longValue()) // 1 GAS
                .networkFee(1L) // 1 fraction of GAS
                .validUntilBlock(0x01020304L)
                .script(new byte[]{(byte) OpCode.PUSH1.getCode()})
                .build();

        byte[] actual = tx.toArray();
        byte[] expected = Numeric.hexStringToByteArray(""
                + "00" // version
                + "04030201"  // nonce
                + "00e1f50500000000"  // system fee (1 GAS)
                + "0100000000000000"  // network fee (1 GAS fraction)
                + "04030201"  // valid until block
                + "01" + "23ba2703c53263e8d6e522dc32203339dcd8eee9" + "01"
                // one calledByEntry signer with scope
                + "00"
                + "01" + OpCode.PUSH1.toString() // 1-byte script with PUSH1 OpCode
                + "00"); // no witnesses

        assertArrayEquals(expected, actual);
    }

    @Test
    public void serializeWithAttributesAndWitnesses() {
        Transaction tx = new Transaction.Builder()
                .version((byte) 0)
                .nonce((long) 0x01020304)
                .systemFee(BigInteger.TEN.pow(8).longValue()) // 1 GAS
                .networkFee(1L) // 1 fraction of GAS
                .validUntilBlock(0x01020304L)
                .signers(Signer.global(account1), Signer.calledByEntry(account2))
                .script(new byte[]{(byte) OpCode.PUSH1.getCode()})
                .witnesses(new Witness(new byte[]{0x00}, new byte[]{0x00}))
                .build();

        byte[] actual = tx.toArray();
        byte[] expected = Numeric.hexStringToByteArray(""
                + "00" // version
                + "04030201"  // nonce
                + "00e1f50500000000"  // system fee (1 GAS)
                + "0100000000000000"  // network fee (1 GAS fraction)
                + "04030201"  // valid until block
                + "02"  // 2 signers
                + "23ba2703c53263e8d6e522dc32203339dcd8eee9" + "80" // global signer
                + "52eaab8b2aab608902c651912db34de36e7a2b0f" + "01" // calledByEntry signer
                + "00"
                + "01" + OpCode.PUSH1.toString() // 1-byte script with PUSH1 OpCode
                + "01" // 1 witness
                + "01000100" // witness
        );

        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = Numeric.hexStringToByteArray(""
                + "00" // version
                + "62bdaa0e"  // nonce
                + "c272890000000000"  // system fee
                + "a65a130000000000"  // network fee
                + "99232000"  // valid until block
                + "01" + "941343239213fa0e765f1027ce742f48db779a96" + "01"
                // one called by entry signer
                + "00"
                + "01" + OpCode.PUSH1.toString()  // 1-byte script with PUSH1 OpCode
                + "01" // 1 witness
                + "01000100"); /* witness*/

        Transaction tx = NeoSerializableInterface.from(data, Transaction.class);
        assertThat(tx.getVersion(), is((byte) 0));
        assertThat(tx.getNonce(), is(246070626L));
        assertThat(tx.getSender(), is(new ScriptHash("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSystemFee(), is(9007810L));
        assertThat(tx.getNetworkFee(), is(1268390L));
        assertThat(tx.getValidUntilBlock(), is(2106265L));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(new ScriptHash("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertArrayEquals(new byte[]{(byte) OpCode.PUSH1.getCode()}, tx.getScript());
        assertThat(tx.getWitnesses(), is(
                Arrays.asList(new Witness(new byte[]{0x00}, new byte[]{0x00}))));
    }

    @Test
    public void getSize() {
        Transaction tx = new Transaction.Builder()
                .version((byte) 0)
                .nonce((long) 0x01020304)
                .systemFee(BigInteger.TEN.pow(8).longValue()) // 1 GAS
                .networkFee(1L) // 1 fraction of GAS
                .validUntilBlock(0x01020304L)
                .signers(Signer.global(account1), Signer.calledByEntry(account2))
                .script(new byte[]{(byte) OpCode.PUSH1.getCode()})
                .witnesses(new Witness(new byte[]{0x00}, new byte[]{0x00}))
                .build();

        int expectedSize = 1 +  // Version
                4 +  // Nonce
                8 +  // System fee
                8 +  // Network fee
                4 + // Valid until block
                1 + // Signer list size
                1 + 20 + // Signer script hash, scope, allowed groups and allowed contracts
                1 + 20 + // Signer script hash and scope, allowed groups and allowed contracts
                1 + // Byte for attributes list size
                1 + 1 + // Byte for script length and the actual length
                1 + // Byte for witnesses list size
                1 + 1 + // Byte for invocation script length and the actual length.
                1 + 1; // Byte for verification script length and the actual length.

        assertThat(tx.getSize(), is(expectedSize));
    }

    @Test(expected = DeserializationException.class)
    public void failDeserializingWithTooManyTransactionAttributes()
            throws DeserializationException {
        StringBuilder txString = new StringBuilder(""
                + "00" // version
                + "62bdaa0e"  // nonce
                + "941343239213fa0e765f1027ce742f48db779a96"// account script hash
                + "c272890000000000"  // system fee
                + "a65a130000000000"  // network fee
                + "99232000"  // valid until block
                + "17"); // one attribute
        for (int i = 0; i <= 16; i++) {
            txString.append("01941343239213fa0e765f1027ce742f48db779a9601"); // signer
        }
        txString.append(""
                + "01" + OpCode.PUSH1.toString()  // 1-byte script with PUSH1 OpCode
                + "01" // 1 witness
                + "01000100"); /* witness*/
        byte[] txBytes = Numeric.hexStringToByteArray(txString.toString());
        NeoSerializableInterface.from(txBytes, Transaction.class);
    }

    @Test
    public void getTxId() throws DeserializationException {
        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769
        Transaction tx = new Transaction.Builder()
                .nonce(226292130L)
                .version((byte) 0)
                .validUntilBlock(2103398)
                .networkFee(1244390L)
                .systemFee(9007990L)
                .signers(Signer.calledByEntry(account3))
                .script(Numeric.hexStringToByteArray(
                        "110c146cd3d4f4f7e35c5ee7d0e725c11dc880cef1e8b10c14c6a1c24a5b87fb8ccd7ac5f7948ffe526d4e01f713c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5238"))
                .attributes()
                .witnesses(new Witness(
                        Numeric.hexStringToByteArray(
                                "0c407ffa520060cc7c6d89c073963ed80d94af1dd27fdbd8a1a7c56104b394e1719e2469dfb5534460c15a40216f3b74f6e384cfd2a49905698fa5861d0d491cc917"),
                        Numeric.hexStringToByteArray(
                                "0c21030ba3f5cb0676ef4eadc89f4da74a6eade644b87aed9a123a117f144ff247052c0b4195440d78")))
                .build();

        assertThat(tx.getTxId(),
                is("066c44b4540ee7b5a3a57fcfcc272353560af792acc3c95da3e18efc962556a2"));
    }

    @Test
    public void toArrayWithoutWitness() throws DeserializationException {
        Transaction tx = new Transaction.Builder()
                .nonce(226292130L)
                .version((byte) 0)
                .validUntilBlock(2103398)
                .networkFee(1244390L)
                .systemFee(9007990L)
                .signers(Signer.calledByEntry(account3))
                .script(Numeric.hexStringToByteArray(
                        "110c146cd3d4f4f7e35c5ee7d0e725c11dc880cef1e8b10c14c6a1c24a5b87fb8ccd7ac5f7948ffe526d4e01f713c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5238"))
                .attributes()
                .witnesses()
                .build();
        byte[] expectedUnsignedBytes = Numeric.hexStringToByteArray(
                "00a2f17c0d7673890000000000e6fc1200000000006618200001c6a1c24a5b87fb8ccd7ac5f7948ffe526d4e01f7010055110c146cd3d4f4f7e35c5ee7d0e725c11dc880cef1e8b10c14c6a1c24a5b87fb8ccd7ac5f7948ffe526d4e01f713c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5238");
        assertThat(tx.toArrayWithoutWitnesses(), is(expectedUnsignedBytes));
    }

    @Test
    public void getHashData() throws DeserializationException {
        byte[] magicNumber = new byte[]{0x4e, 0x45, 0x4F, 0x00};
        NeoConfig.setMagicNumber(magicNumber);
        Transaction tx = new Transaction.Builder()
                .nonce(0L)
                .version((byte) 0)
                .validUntilBlock(0)
                .networkFee(0L)
                .systemFee(0L)
                .signers(Signer.feeOnly(account1))
                .script(new byte[]{1, 2, 3})
                .attributes()
                .witnesses()
                .build();

        byte[] txHexWithoutWitness = Numeric.hexStringToByteArray(
                "000000000000000000000000000000000000000000000000000123ba2703c53263e8d6e522dc32203339dcd8eee9000003010203");
        byte[] expectedData = ArrayUtils.concatenate(magicNumber, txHexWithoutWitness);
        assertThat(tx.getHashData(), is(expectedData));
    }

}