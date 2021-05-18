package io.neow3j.transaction;

import io.neow3j.script.OpCode;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.http.HttpService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.neow3j.crypto.Hash.sha256;
import static io.neow3j.transaction.Signer.feeOnly;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class TransactionTest {

    private Hash160 account1;
    private Hash160 account2;
    private Hash160 account3;

    private Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"));

    @Before
    public void setUp() {
        account1 = Hash160.fromAddress("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");
        account2 = Hash160.fromAddress("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke");
        account3 = Hash160.fromAddress("NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy");
    }

    @Test
    public void serializeWithoutAttributesAndWitnesses() {
        List<Signer> signers = new ArrayList<>();
        signers.add(Signer.calledByEntry(account1));

        List<Witness> witnesses = new ArrayList<>();
        witnesses.add(new Witness(new byte[]{0x00}, new byte[]{0x00}));

        Transaction tx = new Transaction(neow,
                (byte) 0,
                0x01020304L,
                0x01020304L,
                signers,
                BigInteger.TEN.pow(8).longValue(),
                1L,
                new ArrayList<>(),
                new byte[]{(byte) OpCode.PUSH1.getCode()},
                new ArrayList<>());

        byte[] actual = tx.toArray();
        byte[] expected = hexStringToByteArray(""
                + "00" // version
                + "04030201"  // nonce
                + "00e1f50500000000"  // system fee (1 GAS)
                + "0100000000000000"  // network fee (1 GAS fraction)
                + "04030201"  // valid until block
                + "01" + "93ad1572a4b35c4b925483ce1701b78742dc460f" + "01"
                // one calledByEntry signer with scope
                + "00"
                + "01" + OpCode.PUSH1.toString() // 1-byte script with PUSH1 OpCode
                + "00"); // no witnesses

        assertArrayEquals(expected, actual);
    }

    @Test
    public void serializeWithAttributesAndWitnesses() {
        List<Signer> signers = new ArrayList<>();
        signers.add(Signer.global(account1));
        signers.add(Signer.calledByEntry(account2));

        List<Witness> witnesses = new ArrayList<>();
        witnesses.add(new Witness(new byte[]{0x00}, new byte[]{0x00}));

        Transaction tx = new Transaction(neow,
                (byte) 0,
                0x01020304L,
                0x01020304L,
                signers,
                BigInteger.TEN.pow(8).longValue(),
                1L,
                new ArrayList<>(),
                new byte[]{(byte) OpCode.PUSH1.getCode()},
                witnesses);

        byte[] actual = tx.toArray();
        byte[] expected = hexStringToByteArray(""
                + "00" // version
                + "04030201"  // nonce
                + "00e1f50500000000"  // system fee (1 GAS)
                + "0100000000000000"  // network fee (1 GAS fraction)
                + "04030201"  // valid until block
                + "02"  // 2 signers
                + "93ad1572a4b35c4b925483ce1701b78742dc460f" + "80" // global signer
                + "09a55874c2da4b86e5d49ff530a1b153eb12c7d6" + "01" // calledByEntry signer
                + "00"
                + "01" + OpCode.PUSH1.toString() // 1-byte script with PUSH1 OpCode
                + "01" // 1 witness
                + "01000100" // witness
        );

        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
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
        assertThat(tx.getSender(), is(new Hash160("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSystemFee(), is(9007810L));
        assertThat(tx.getNetworkFee(), is(1268390L));
        assertThat(tx.getValidUntilBlock(), is(2106265L));
        assertThat(tx.getAttributes(), hasSize(0));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(new Hash160("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertArrayEquals(new byte[]{(byte) OpCode.PUSH1.getCode()}, tx.getScript());
        assertThat(tx.getWitnesses(), is(
                Arrays.asList(new Witness(new byte[]{0x00}, new byte[]{0x00}))));
    }

    @Test
    public void getSize() {
        List<Signer> signers = new ArrayList<>();
        signers.add(Signer.global(account1));
        signers.add(Signer.calledByEntry(account2));

        List<Witness> witnesses = new ArrayList<>();
        witnesses.add(new Witness(new byte[]{0x00}, new byte[]{0x00}));

        Transaction tx = new Transaction(neow,
                (byte) 0,
                0x01020304L,
                0x01020304L,
                signers,
                BigInteger.TEN.pow(8).longValue(),
                1L,
                new ArrayList<>(),
                new byte[]{(byte) OpCode.PUSH1.getCode()},
                witnesses);

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
        byte[] txBytes = hexStringToByteArray(txString.toString());
        NeoSerializableInterface.from(txBytes, Transaction.class);
    }

    @Test
    public void getTxId() throws IOException {
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"),
                new Neow3jConfig().setNetworkMagic(5195086));

        List<Signer> signers = new ArrayList<>();
        signers.add(Signer.calledByEntry(account3));

        Transaction tx = new Transaction(neow,
                (byte) 0,
                226292130L,
                2103398,
                signers,
                9007990L,
                1244390L,
                new ArrayList<>(),
                hexStringToByteArray(
                        "110c146cd3d4f4f7e35c5ee7d0e725c11dc880cef1e8b10c14c6a1c24a5b87fb8ccd7ac5f7948ffe526d4e01f713c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5238"),
                new ArrayList<>());

        assertThat(tx.getTxId(),
                is(new Hash256("22ffa2d8680cea4928e2e74ceee560eedfa6e35f199640a7fe725c1f9da0b19e")));
    }

    @Test
    public void toArrayWithoutWitness() {
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"),
                new Neow3jConfig().setNetworkMagic(5195086));

        List<Signer> signers = new ArrayList<>();
        signers.add(Signer.calledByEntry(account3));
        Transaction tx = new Transaction(neow,
                (byte) 0,
                226292130L,
                2103398,
                signers,
                9007990L,
                1244390L,
                new ArrayList<>(),
                hexStringToByteArray(
                        "110c146cd3d4f4f7e35c5ee7d0e725c11dc880cef1e8b10c14c6a1c24a5b87fb8ccd7ac5f7948ffe526d4e01f713c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5238"),
                new ArrayList<>());

        byte[] expectedUnsignedBytes = hexStringToByteArray(
                "00a2f17c0d7673890000000000e6fc120000000000661820000175715e89bbba44a25dc9ca8d4951f104c25c253d010055110c146cd3d4f4f7e35c5ee7d0e725c11dc880cef1e8b10c14c6a1c24a5b87fb8ccd7ac5f7948ffe526d4e01f713c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5238");
        assertThat(tx.toArrayWithoutWitnesses(), is(expectedUnsignedBytes));
    }

    @Test
    public void getHashData() throws IOException {
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"),
                new Neow3jConfig().setNetworkMagic(769));

        List<Signer> signers = new ArrayList<>();
        signers.add(feeOnly(account1));
        Transaction tx = new Transaction(neow, (byte) 0,
                0L,
                0L,
                signers,
                0L,
                0L,
                new ArrayList<>(),
                new byte[]{1, 2, 3},
                new ArrayList<>());

        byte[] txHexWithoutWitness = hexStringToByteArray(
                "000000000000000000000000000000000000000000000000000193ad1572a4b35c4b925483ce1701b78742dc460f000003010203");
        byte[] expectedData = concatenate(neow.getNetworkMagicNumber(),
                sha256(txHexWithoutWitness));
        assertThat(tx.getHashData(), is(expectedData));
    }

}
