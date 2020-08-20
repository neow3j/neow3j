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
import io.neow3j.utils.Numeric;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Before;
import org.junit.Test;

public class TransactionTest {

    private ScriptHash account1;
    private ScriptHash account2;

    @Before
    public void setUp() {
        account1 = ScriptHash.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
        account2 = ScriptHash.fromAddress("APLJBPhtRg2XLhtpxEHd6aRNL7YSLGH2ZL");
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
        assertThat(tx.getSender().getScriptHash(),
                is(new ScriptHash("969a77db482f74ce27105f760efa139223431394")));
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
        byte[] txBytes = Numeric.hexStringToByteArray(
                "0081bda92e941343239213fa0e765f1027ce742f48db779a96c272890000000000064b130000000000132620000101941343239213fa0e765f1027ce742f48db779a960155150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c4086c0799939fae59efd4fc8d0b4d0be8fecf8d0c4d1715d84193f0c173ba42b5655b454ca58c866f65608e3744643cef8fbbab2855ce806f3e0ccb18872e05398290c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        Transaction tx = NeoSerializableInterface.from(txBytes, Transaction.class);
        assertThat(tx.getTxId(),
                is("6876017ef8e845a5c659e556bf612e6d37ddd80f64eb8797fc8697909ba6a197"));
    }

    @Test
    public void toArrayWithoutWitness() throws DeserializationException {
        byte[] txBytes = Numeric.hexStringToByteArray(
                "0081bda92e941343239213fa0e765f1027ce742f48db779a96c272890000000000064b130000000000132620000101941343239213fa0e765f1027ce742f48db779a960155150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c4086c0799939fae59efd4fc8d0b4d0be8fecf8d0c4d1715d84193f0c173ba42b5655b454ca58c866f65608e3744643cef8fbbab2855ce806f3e0ccb18872e05398290c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        byte[] txBytesUnsigned = Numeric.hexStringToByteArray(
                "0081bda92e941343239213fa0e765f1027ce742f48db779a96c272890000000000064b130000000000132620000101941343239213fa0e765f1027ce742f48db779a960155150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238");
        Transaction tx = NeoSerializableInterface.from(txBytes, Transaction.class);
        assertThat(tx.toArrayWithoutWitnesses(), is(txBytesUnsigned));
    }

    @Test
    public void getHashData() throws DeserializationException {
        NeoConfig.setMagicNumber(new byte[]{0x4e, 0x45, 0x4F, 0x00});
        byte[] txBytes = Numeric.hexStringToByteArray(
                "001dbfc570941343239213fa0e765f1027ce742f48db779a96c272890000000000064b130000000000b81920000101941343239213fa0e765f1027ce742f48db779a960155150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c406fcb8f6811ac85ed7a1308d14c7197531e83b2d7959c61cc980f30d78f4a9af5c5282272243ec1b51e399fc252caa00ca5fb332a107649adb8f5f8e746b12013290c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        Transaction tx = NeoSerializableInterface.from(txBytes, Transaction.class);
        byte[] expectedData = Numeric.hexStringToByteArray(
                "4e454f00001dbfc570941343239213fa0e765f1027ce742f48db779a96c272890000000000064b130000000000b81920000101941343239213fa0e765f1027ce742f48db779a960155150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238");
        assertThat(tx.getHashData(), is(expectedData));
    }

}