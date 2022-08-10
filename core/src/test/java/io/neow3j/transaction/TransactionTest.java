package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.core.response.NeoBlockCount;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.OpCode;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.neow3j.crypto.Hash.sha256;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionTest {

    private Hash160 account1;
    private Hash160 account2;
    private Hash160 account3;

    // pubKey: 0x0200bb19db74b4ff21a51065635d5c63953324d0799ed0bae9e0f02e3bd3d32b70
    private final Account a4 = Account.fromWIF("L3pLaHgKBf7ENNKPH1jfPM8FC9QhPCqwFyWguQ8CDB1G66p78wd6");
    // pubKey: 0x022f66d6377f8737b5ee95df0ac4059b8523d77f96d095efe36430d7b2d3d72b15
    private final Account a5 = Account.fromWIF("KypPpzztxDj26DiCmTkbwQJT2TrgaNtw5Wp3K2nYiMvWu99Xv3rP");
    // pubKey: 0x03bf1d1b799412171f598be70916cbac39dba47a10d5e568aad46a9ec928c3b59d
    private final Account a6 = Account.fromWIF("KxjePibw7BEdaS8diPeqgozFWevVx6tLE226jYU6tFF1HSYQ5z5u");

    private final Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"));

    @BeforeAll
    public void setUp() {
        account1 = Hash160.fromAddress("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");
        account2 = Hash160.fromAddress("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke");
        account3 = Hash160.fromAddress("NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy");
    }

    @Test
    public void serializeWithoutAttributesAndWitnesses() {
        List<Signer> signers = new ArrayList<>();
        signers.add(AccountSigner.calledByEntry(account1));

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
        signers.add(AccountSigner.global(account1));
        signers.add(AccountSigner.calledByEntry(account2));

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
                + "01" + "01" // one attribute - high priority
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
        assertThat(tx.getAttributes(), hasSize(1));
        assertThat(tx.getAttributes().get(0).getType(), is(TransactionAttributeType.HIGH_PRIORITY));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(new Hash160("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertArrayEquals(new byte[]{(byte) OpCode.PUSH1.getCode()}, tx.getScript());
        assertThat(tx.getWitnesses(), is(
                asList(new Witness(new byte[]{0x00}, new byte[]{0x00}))));

        assertNull(tx.neow3j);
        Neow3j neow3j = Neow3j.build(new HttpService("http://localhost:40332"));
        tx.setNeow3j(neow3j);
        assertThat(tx.neow3j, is(neow3j));
    }

    @Test
    public void deserializeWithoutWitness() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "00" // version
                + "62bdaa0e"  // nonce
                + "c272890000000000"  // system fee
                + "a65a130000000000"  // network fee
                + "99232000"  // valid until block
                + "01" + "941343239213fa0e765f1027ce742f48db779a96" + "01"
                // one called by entry signer
                + "01" + "01" // one attribute - high priority
                + "01" + OpCode.PUSH1.toString()  // 1-byte script with PUSH1 OpCode
        );

        Transaction tx = NeoSerializableInterface.from(data, Transaction.class);
        assertThat(tx.getVersion(), is((byte) 0));
        assertThat(tx.getNonce(), is(246070626L));
        assertThat(tx.getSender(), is(new Hash160("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSystemFee(), is(9007810L));
        assertThat(tx.getNetworkFee(), is(1268390L));
        assertThat(tx.getValidUntilBlock(), is(2106265L));
        assertThat(tx.getAttributes(), hasSize(1));
        assertThat(tx.getAttributes().get(0).getType(), is(TransactionAttributeType.HIGH_PRIORITY));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(new Hash160("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertArrayEquals(new byte[]{(byte) OpCode.PUSH1.getCode()}, tx.getScript());
    }

    @Test
    public void deserializeWithZeroWitnesses() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "00" // version
                + "62bdaa0e"  // nonce
                + "c272890000000000"  // system fee
                + "a65a130000000000"  // network fee
                + "99232000"  // valid until block
                + "01" + "941343239213fa0e765f1027ce742f48db779a96" + "01"
                // one called by entry signer
                + "01" + "01" // one attribute - high priority
                + "01" + OpCode.PUSH1.toString()  // 1-byte script with PUSH1 OpCode
                + "00"
        );

        Transaction tx = NeoSerializableInterface.from(data, Transaction.class);
        assertThat(tx.getVersion(), is((byte) 0));
        assertThat(tx.getNonce(), is(246070626L));
        assertThat(tx.getSender(), is(new Hash160("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSystemFee(), is(9007810L));
        assertThat(tx.getNetworkFee(), is(1268390L));
        assertThat(tx.getValidUntilBlock(), is(2106265L));
        assertThat(tx.getAttributes(), hasSize(1));
        assertThat(tx.getAttributes().get(0).getType(), is(TransactionAttributeType.HIGH_PRIORITY));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(new Hash160("969a77db482f74ce27105f760efa139223431394")));
        assertThat(tx.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertArrayEquals(new byte[]{(byte) OpCode.PUSH1.getCode()}, tx.getScript());
    }

    @Test
    public void getSize() {
        List<Signer> signers = new ArrayList<>();
        signers.add(AccountSigner.global(account1));
        signers.add(AccountSigner.calledByEntry(account2));

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

    @Test
    public void failDeserializingWithTooManyTransactionAttributes() {
        StringBuilder txString = new StringBuilder(""
                + "00" // version 0
                + "62bdaa0e"  // nonce
                + "c272890000000000"  // system fee
                + "a65a130000000000"  // network fee
                + "99232000"  // valid until block
                + "11"); // 17 signers
        for (int i = 0; i <= 16; i++) {
            txString.append("941343239213fa0e765f1027ce742f48db779a96"); // signer script hash
            txString.append("01"); // called by entry scope
        }
        txString.append("00"); // no attributes
        // additional bytes are not needed for this test
        byte[] txBytes = hexStringToByteArray(txString.toString());

        DeserializationException thrown = assertThrows(DeserializationException.class,
                () -> NeoSerializableInterface.from(txBytes, Transaction.class));
        assertThat(thrown.getMessage(), containsString("A transaction can hold at most "));
    }

    @Test
    public void getTxId() {
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"),
                new Neow3jConfig().setNetworkMagic(5195086));

        List<Signer> signers = new ArrayList<>();
        signers.add(AccountSigner.calledByEntry(account3));

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
        signers.add(AccountSigner.calledByEntry(account3));
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
        signers.add(AccountSigner.none(Account.fromScriptHash(account1)));
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
        byte[] expectedData = concatenate(neow.getNetworkMagicNumberBytes(),
                sha256(txHexWithoutWitness));
        assertThat(tx.getHashData(), is(expectedData));
    }

    @Test
    public void testTooBigTransaction() {
        // The following transaction is 29 bytes without the script.
        // The script needs additional 4 bytes to specify its length.
        byte[] scriptForTooBigTx = new byte[NeoConstants.MAX_TRANSACTION_SIZE - 29 - 4 + 1];
        // This transaction exceeds the maximal allowed byte length by one.
        Transaction tx = new Transaction(neow, (byte) 0,
                0L,
                0L,
                new ArrayList<>(),
                0L,
                0L,
                new ArrayList<>(),
                scriptForTooBigTx,
                new ArrayList<>());

        assertThat(tx.getSize(), is(NeoConstants.MAX_TRANSACTION_SIZE + 1));

        TransactionConfigurationException thrown = assertThrows(TransactionConfigurationException.class, tx::send);
        assertThat(thrown.getMessage(), containsString("The transaction exceeds the maximum transaction size."));
    }

    @Test
    public void testMaxTransactionSize() throws IOException {
        HttpService mock = Mockito.mock(HttpService.class);
        Mockito.when(mock.send(Mockito.any(), Mockito.eq(NeoSendRawTransaction.class)))
                .thenReturn(new NeoSendRawTransaction());
        NeoBlockCount blockCount = new NeoBlockCount();
        blockCount.setResult(BigInteger.ONE);
        Mockito.when(mock.send(Mockito.any(), Mockito.eq(NeoBlockCount.class))).thenReturn(blockCount);
        Neow3j neow3j = Neow3j.build(mock);

        // The following transaction is 29 bytes without the script.
        // The script needs additional 4 bytes to specify its length.
        byte[] scriptForTooBigTx = new byte[NeoConstants.MAX_TRANSACTION_SIZE - 29 - 4];
        // This transaction has exactly the maximal allowed byte length.
        Transaction tx = new Transaction(neow3j, (byte) 0,
                0L,
                0L,
                new ArrayList<>(),
                0L,
                0L,
                new ArrayList<>(),
                scriptForTooBigTx,
                new ArrayList<>());

        assertThat(tx.getSize(), is(NeoConstants.MAX_TRANSACTION_SIZE));
        tx.send();
    }

    @Test
    public void testAddMultiSigWitnessWithPubKeySigMap() throws IOException {
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"), new Neow3jConfig().setNetworkMagic(768));

        Account multiSigAccount = Account.createMultiSigAccount(asList(
                        a4.getECKeyPair().getPublicKey(),
                        a5.getECKeyPair().getPublicKey(),
                        a6.getECKeyPair().getPublicKey()),
                3);

        Transaction dummyTx = new Transaction(neow,
                (byte) 0,
                0x01020304L,
                0x01020304L,
                asList(AccountSigner.calledByEntry(multiSigAccount)),
                BigInteger.TEN.pow(8).longValue(),
                1L,
                new ArrayList<>(),
                new byte[]{(byte) OpCode.PUSH1.getCode()},
                new ArrayList<>());

        byte[] dummyBytes = dummyTx.getHashData();
        Sign.SignatureData sig4 = Sign.signMessage(dummyBytes, a4.getECKeyPair());
        Sign.SignatureData sig5 = Sign.signMessage(dummyBytes, a5.getECKeyPair());
        Sign.SignatureData sig6 = Sign.signMessage(dummyBytes, a6.getECKeyPair());

        HashMap<ECKeyPair.ECPublicKey, Sign.SignatureData> pubKeySigMap = new HashMap<>();
        pubKeySigMap.put(a6.getECKeyPair().getPublicKey(), sig6);
        pubKeySigMap.put(a5.getECKeyPair().getPublicKey(), sig5);
        pubKeySigMap.put(a4.getECKeyPair().getPublicKey(), sig4);
        dummyTx.addMultiSigWitness(multiSigAccount.getVerificationScript(), pubKeySigMap);

        Witness expectedMultiSigWitness = Witness.createMultiSigWitness(asList(sig4, sig5, sig6),
                multiSigAccount.getVerificationScript());
        assertThat(dummyTx.getWitnesses(), hasSize(1));
        assertThat(dummyTx.getWitnesses().get(0), is(expectedMultiSigWitness));
    }

    @Test
    public void testAddMultiSigWitnessWithAccounts() throws IOException {
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"), new Neow3jConfig().setNetworkMagic(768));

        Account multiSigAccount = Account.createMultiSigAccount(asList(
                        a4.getECKeyPair().getPublicKey(),
                        a5.getECKeyPair().getPublicKey(),
                        a6.getECKeyPair().getPublicKey()),
                3);

        Transaction dummyTx = new Transaction(neow,
                (byte) 0,
                0x01020304L,
                0x01020304L,
                asList(AccountSigner.calledByEntry(multiSigAccount)),
                BigInteger.TEN.pow(8).longValue(),
                1L,
                new ArrayList<>(),
                new byte[]{(byte) OpCode.PUSH1.getCode()},
                new ArrayList<>());

        byte[] dummyBytes = dummyTx.getHashData();
        Sign.SignatureData sig4 = Sign.signMessage(dummyBytes, a4.getECKeyPair());
        Sign.SignatureData sig5 = Sign.signMessage(dummyBytes, a5.getECKeyPair());
        Sign.SignatureData sig6 = Sign.signMessage(dummyBytes, a6.getECKeyPair());

        dummyTx.addMultiSigWitness(multiSigAccount.getVerificationScript(), a5, a6, a4);
        dummyTx.addMultiSigWitness(multiSigAccount.getVerificationScript(), a6, a4, a5);

        Witness expectedMultiSigWitness = Witness.createMultiSigWitness(asList(sig4, sig5, sig6),
                multiSigAccount.getVerificationScript());
        assertThat(dummyTx.getWitnesses(), hasSize(2));
        assertThat(dummyTx.getWitnesses().get(0), is(expectedMultiSigWitness));
        assertThat(dummyTx.getWitnesses().get(1), is(expectedMultiSigWitness));
    }

    @Test
    public void toContractParameterContextJson() throws IOException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {

        Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"), new Neow3jConfig().setNetworkMagic(769));

        ECKeyPair.ECPublicKey pubKey = ECKeyPair.createEcKeyPair().getPublicKey();
        Account multiSigAccount = Account.createMultiSigAccount(asList(pubKey, pubKey, pubKey), 2);
        Account singleSigAccount1 = Account.create();
        Account singleSigAccount2 = Account.create();
        List<Signer> signers = new ArrayList<>();
        signers.add(AccountSigner.none(singleSigAccount1));
        signers.add(AccountSigner.calledByEntry(singleSigAccount2));
        signers.add(AccountSigner.calledByEntry(multiSigAccount));

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
        Witness acc1witness = Witness.create(tx.getHashData(), singleSigAccount1.getECKeyPair());
        tx.addWitness(acc1witness);
        ContractParametersContext ctx = tx.toContractParametersContext();
        assertThat(ctx.getType(), is("Neo.Network.P2P.Payloads.Transaction"));
        assertThat(ctx.getNetwork(), is(769L));
        assertThat(ctx.getData(), is(Base64.encode(tx.toArrayWithoutWitnesses())));
        assertThat(ctx.getHash(), is(tx.getTxId().toString()));
        assertThat(ctx.getItems().size(), is(3));

        // item 1
        ContractParametersContext.ContextItem item =
                ctx.getItems().get("0x" + singleSigAccount1.getScriptHash().toString());
        assertThat(item.getScript(), is(Base64.encode(singleSigAccount1.getVerificationScript().getScript())));
        assertThat(item.getParameters().size(), is(1));
        assertThat(item.getParameters().get(0).getType(), is(ContractParameterType.SIGNATURE));
        assertThat(item.getParameters().get(0).getValue(),
                is(acc1witness.getInvocationScript().getSignatures().get(0).getConcatenated()));
        assertThat(item.getSignatures().size(), is(1));
        assertThat(item.getSignatures().get(singleSigAccount1.getECKeyPair().getPublicKey().getEncodedCompressedHex()),
                is(Base64.encode(acc1witness.getInvocationScript().getSignatures().get(0).getConcatenated())));

        // item 2
        item = ctx.getItems().get("0x" + singleSigAccount2.getScriptHash().toString());
        assertThat(item.getScript(), is(Base64.encode(singleSigAccount2.getVerificationScript().getScript())));
        assertThat(item.getParameters().size(), is(1));
        assertThat(item.getParameters().get(0).getType(), is(ContractParameterType.SIGNATURE));
        assertThat(item.getParameters().get(0).getValue(), is(nullValue()));
        assertThat(item.getSignatures().size(), is(0));

        // item 3
        item = ctx.getItems().get("0x" + multiSigAccount.getScriptHash().toString());
        assertThat(item.getScript(), is(Base64.encode(multiSigAccount.getVerificationScript().getScript())));
        assertThat(item.getParameters().size(), is(2));
        assertThat(item.getParameters().get(0).getType(), is(ContractParameterType.SIGNATURE));
        assertThat(item.getParameters().get(0).getValue(), is(nullValue()));
        assertThat(item.getParameters().get(1).getType(), is(ContractParameterType.SIGNATURE));
        assertThat(item.getParameters().get(1).getValue(), is(nullValue()));
        assertThat(item.getSignatures().size(), is(0));
    }

    @Test
    public void toContractParameterContextJson_unsupportedContractSigners() throws IOException {
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:40332"), new Neow3jConfig().setNetworkMagic(769));

        Account singleSigAccount1 = Account.create();
        Hash160 dummyHash = new Hash160("f32bf2a3e36a9fd3411337ffcd48eed7bec727ce");
        List<Signer> signers = new ArrayList<>();
        signers.add(AccountSigner.none(singleSigAccount1));
        signers.add(ContractSigner.calledByEntry(dummyHash));

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
        Witness acc1witness = Witness.create(tx.getHashData(), singleSigAccount1.getECKeyPair());
        tx.addWitness(acc1witness);

        UnsupportedOperationException thrown =
                assertThrows(UnsupportedOperationException.class, tx::toContractParametersContext);
        assertThat(thrown.getMessage(), is("Cannot handle contract signers"));
    }

}
