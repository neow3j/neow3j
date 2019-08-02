package io.neow3j.wallet;

import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.crypto.transaction.RawScript;
import io.neow3j.crypto.transaction.RawTransaction;
import io.neow3j.crypto.transaction.RawTransactionInput;
import io.neow3j.crypto.transaction.RawTransactionOutput;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents.Balance;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents.UnspentTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents.Unspents;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.ContractTransaction;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class AssetTransferTest {

    /**
     * Alternative address.
     */
    private static final String ALT_ADDR = "AJQ6FoaSXDFzA6wLnyZ1nFN7SGSN2oNTc3";

    /**
     * An empty Neow3j instance which doesn't actually have connection to a RPC node.
     */
    private static final Neow3j EMPTY_NEOW3J = Neow3j.build(null);

    /**
     * Account with address AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y
     */
    private Account acct;

    @Before
    public void setup() {
        acct = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
    }

    @Test
    public void test_transfer_with_normal_account_without_fee() throws IOException, ErrorResponseException {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(acct.getAddress());
        acct.updateAssetBalances(neow3j);
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);
        AssetTransfer at = new AssetTransfer.Builder(neow3j).account(acct).output(output).build().sign();
        RawTransaction tx = at.getTransaction();

        RawTransactionOutput expectedChange = new RawTransactionOutput(NEOAsset.HASH_ID, "99999999", acct.getAddress());
        RawTransactionInput expectedInput = new RawTransactionInput("4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0);
        // The ordering of the inputs and outputs is important
        ContractTransaction expectedTx = new ContractTransaction.Builder()
                .outputs(Arrays.asList(output, expectedChange))
                .inputs(Arrays.asList(expectedInput))
                .build();
        SignatureData expectedSig = Sign.signMessage(expectedTx.toArrayWithoutScripts(), acct.getECKeyPair());

        // Test Witness
        assertEquals(1, tx.getScripts().size());
        RawScript script = tx.getScripts().get(0);
        assertArrayEquals(
                ArrayUtils.concatenate(OpCode.PUSHBYTES64.getValue(), expectedSig.getConcatenated()),
                script.getInvocationScript().getScript()
        );
        assertArrayEquals(ByteBuffer.allocate(1 + 33 + 1)
                .put(OpCode.PUSHBYTES33.getValue())
                .put(acct.getPublicKey().toByteArray())
                .put(OpCode.CHECKSIG.getValue()).array(), script.getVerificationScript().getScript()
        );

        // Test inputs
        assertEquals(1, tx.getInputs().size());
        assertEquals(expectedInput.getPrevHash(), tx.getInputs().get(0).getPrevHash());
        assertEquals(expectedInput.getPrevIndex(), tx.getInputs().get(0).getPrevIndex());

        // Test outputs
        assertEquals(2, tx.getOutputs().size());
        // Intended output
        assertEquals(NEOAsset.HASH_ID, tx.getOutputs().get(0).getAssetId());
        assertEquals("1", tx.getOutputs().get(0).getValue());
        assertEquals(ALT_ADDR, tx.getOutputs().get(0).getAddress());
        // Change
        assertEquals(NEOAsset.HASH_ID, tx.getOutputs().get(1).getAssetId());
        assertEquals("99999999", tx.getOutputs().get(1).getValue());
        assertEquals("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", tx.getOutputs().get(1).getAddress());
    }

    @Test
    public void test_transfer_with_fee() throws IOException, ErrorResponseException {
        // setup
        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(address);
        acct.updateAssetBalances(neow3j);
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);
        BigDecimal fee = BigDecimal.ONE;
        AssetTransfer at = new AssetTransfer.Builder(neow3j).account(acct).output(output).networkFee(fee).build().sign();
        RawTransaction tx = at.getTransaction();

        RawTransactionOutput expectedChangeNeo = new RawTransactionOutput(NEOAsset.HASH_ID, "99999999", acct.getAddress());
        RawTransactionOutput expectedChangeGas = new RawTransactionOutput(GASAsset.HASH_ID, "15983", acct.getAddress());
        RawTransactionInput expectedInput = new RawTransactionInput("4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0);
        RawTransactionInput expectedInputFee = new RawTransactionInput("c2f7fac79531d94d406367c7feafe425f893a580fa703c7b4df9572f5944df5a", 0);
        // The ordering of the inputs and outputs is important
        ContractTransaction expectedTx = new ContractTransaction.Builder()
                .outputs(Arrays.asList(output, expectedChangeGas, expectedChangeNeo))
                .inputs(Arrays.asList(expectedInputFee, expectedInput))
                .build();
        SignatureData expectedSig = Sign.signMessage(expectedTx.toArrayWithoutScripts(), acct.getECKeyPair());

        // Test Witness
        assertEquals(1, tx.getScripts().size());
        RawScript script = tx.getScripts().get(0);
        assertArrayEquals(
                ArrayUtils.concatenate(OpCode.PUSHBYTES64.getValue(), expectedSig.getConcatenated()),
                script.getInvocationScript().getScript()
        );
        assertArrayEquals(ByteBuffer.allocate(1 + 33 + 1)
                        .put(OpCode.PUSHBYTES33.getValue())
                        .put(acct.getPublicKey().toByteArray())
                        .put(OpCode.CHECKSIG.getValue()).array(),
                script.getVerificationScript().getScript()
        );

        // Test inputs
        assertEquals(2, tx.getInputs().size());
        RawTransactionInput i = tx.getInputs().get(0);
        assertEquals(expectedInputFee.getPrevHash(), i.getPrevHash());
        assertEquals(expectedInputFee.getPrevIndex(), i.getPrevIndex());
        i = tx.getInputs().get(1);
        assertEquals(expectedInput.getPrevHash(), i.getPrevHash());
        assertEquals(expectedInput.getPrevIndex(), i.getPrevIndex());

        // test outputs
        assertEquals(3, tx.getOutputs().size());
        RawTransactionOutput o = tx.getOutputs().get(0);
        assertEquals(output.getAddress(), o.getAddress());
        assertEquals(output.getValue(), o.getValue());
        assertEquals(output.getAssetId(), o.getAssetId());
        o = tx.getOutputs().get(1);
        assertEquals(expectedChangeGas.getAddress(), o.getAddress());
        assertEquals(expectedChangeGas.getValue(), o.getValue());
        assertEquals(expectedChangeGas.getAssetId(), o.getAssetId());
        o = tx.getOutputs().get(2);
        assertEquals(expectedChangeNeo.getAddress(), o.getAddress());
        assertEquals(expectedChangeNeo.getValue(), o.getValue());
        assertEquals(expectedChangeNeo.getAssetId(), o.getAssetId());
    }

    @Test
    public void test_transfer_with_single_output() throws IOException, ErrorResponseException {
        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(address);
        acct.updateAssetBalances(neow3j);
        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(acct)
                .asset(NEOAsset.HASH_ID)
                .amount(BigDecimal.ONE)
                .toAddress(ALT_ADDR)
                .build()
                .sign();
        RawTransaction tx = at.getTransaction();

        RawTransactionOutput expectedChange = new RawTransactionOutput(NEOAsset.HASH_ID, "99999999", acct.getAddress());
        RawTransactionInput expectedInput = new RawTransactionInput("4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0);
        // The ordering of the inputs and outputs is important
        RawTransactionOutput expectedOutput = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);
        ContractTransaction expectedTx = new ContractTransaction.Builder()
                .outputs(Arrays.asList(expectedOutput, expectedChange))
                .inputs(Arrays.asList(expectedInput))
                .build();
        SignatureData expectedSig = Sign.signMessage(expectedTx.toArrayWithoutScripts(), acct.getECKeyPair());

        // Test Witness
        assertEquals(1, tx.getScripts().size());
        RawScript script = tx.getScripts().get(0);
        assertArrayEquals(
                ArrayUtils.concatenate(OpCode.PUSHBYTES64.getValue(), expectedSig.getConcatenated()),
                script.getInvocationScript().getScript()
        );
        assertArrayEquals(ByteBuffer.allocate(1 + 33 + 1)
                .put(OpCode.PUSHBYTES33.getValue())
                .put(acct.getPublicKey().toByteArray())
                .put(OpCode.CHECKSIG.getValue()).array(), script.getVerificationScript().getScript()
        );

        // Test inputs
        assertEquals(1, tx.getInputs().size());
        assertEquals(expectedInput.getPrevHash(), tx.getInputs().get(0).getPrevHash());
        assertEquals(expectedInput.getPrevIndex(), tx.getInputs().get(0).getPrevIndex());

        // Test outputs
        assertEquals(2, tx.getOutputs().size());
        // Intended output
        assertEquals(NEOAsset.HASH_ID, tx.getOutputs().get(0).getAssetId());
        assertEquals("1", tx.getOutputs().get(0).getValue());
        assertEquals(ALT_ADDR, tx.getOutputs().get(0).getAddress());
        // Change
        assertEquals(NEOAsset.HASH_ID, tx.getOutputs().get(1).getAssetId());
        assertEquals("99999999", tx.getOutputs().get(1).getValue());
        assertEquals("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", tx.getOutputs().get(1).getAddress());
    }

    @Test(expected = IllegalStateException.class)
    public void test_missing_neow3j() {
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);

        AssetTransfer at = new AssetTransfer.Builder(null)
                .account(a)
                .output(output)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_missing_account() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .output(output)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_missing_outputs() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_partially_missing_output1() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .asset(NEOAsset.HASH_ID)
                .amount(BigDecimal.ONE)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_partially_missing_output2() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .asset(NEOAsset.HASH_ID)
                .toAddress(ALT_ADDR)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_partially_missing_output3() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .amount(BigDecimal.ONE)
                .toAddress(ALT_ADDR)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_erroneously_add_outputs1() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .asset(NEOAsset.HASH_ID)
                .output(output)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_erroneously_add_outputs2() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .output(output)
                .amount(BigDecimal.ONE)
                .build();
    }

    @Test
    public void test_transfer_from_multisig_account() {
        // TODO Claude 18.06.19: Implement
    }

    @Test
    public void test_transfer_with_no_change_needed() {
        // TODO Claude 18.06.19: Implement
    }

    @Test
    public void test_transfer_with_multiple_utxos_needed() {
        // TODO Claude 18.06.19: Implement
    }

    /**
     * This test uses a raw transaction string generated with neon-js. The transaction makes an
     * asset transfer of one NEO from a contract's balance to a normal address. The transaction
     * input, i.e. the contract's UTXO has to be mocked. As well as the contract state, of which we
     * only need the number of input parameters.
     */
    @Test
    public void transfer_from_contract() throws IOException {
        ScriptHash contractScriptHash = new ScriptHash("d994605e4f3960ba8d7422c4c8b1e94d48960a8d");
        Account contractAccount = Account.fromAddress(contractScriptHash.toAddress()).build();

        // Mock the Unspents response for the UTXO of the contract.
        UnspentTransaction utxo = new UnspentTransaction(
                "47cc41bfc0ad504032a73de8e3082a20172984730496ed98336e895c9a54b8b3", 0, BigDecimal.TEN);
        Balance balance = new Balance(Arrays.asList(utxo), NEOAsset.HASH_ID, NEOAsset.NAME,
                NEOAsset.NAME, BigDecimal.TEN);
        Unspents unspents = new Unspents(Arrays.asList(balance), contractScriptHash.toAddress());
        NeoGetUnspents unspentsResponse = new NeoGetUnspents();
        unspentsResponse.setResult(unspents);
        Request<?, NeoGetUnspents> unspentsRequestSpy = spy(new Request<>());
        doReturn(unspentsResponse).when(unspentsRequestSpy).send();

        // Mock the contract state response for the contract state.
        ContractState contractState = new ContractState(0, null, null,
                Arrays.asList(ContractParameterType.STRING), null, null, null, null, null, null, null);
        NeoGetContractState stateResponse = new NeoGetContractState();
        stateResponse.setResult(contractState);
        Request<?, NeoGetContractState> stateRequestSpy = spy(new Request<>());
        doReturn(stateResponse).when(stateRequestSpy).send();

        Neow3j neow3jSpy = spy(EMPTY_NEOW3J);
        doReturn(unspentsRequestSpy).when(neow3jSpy).getUnspents(contractAccount.getAddress());
        doReturn(stateRequestSpy).when(neow3jSpy).getContractState(contractScriptHash.toString());

        AssetTransfer at = new AssetTransfer.Builder(neow3jSpy)
                .account(acct)
                .amount(new BigDecimal("1"))
                .asset(NEOAsset.HASH_ID)
                .toAddress(acct.getAddress())
                .fromContract(contractScriptHash)
                .build()
                .sign();

        byte[] txBytes = at.getTransaction().toArray();
        String expectedTx = "8000012023ba2703c53263e8d6e522dc32203339dcd8eee901b3b8549a5c896e3398" +
                "ed960473842917202a08e3e83da7324050adc0bf41cc470000029b7cffdaa674beae0f930ebe6085" +
                "af9093e5fe56b34a5c220ccdcf6efc336fc500e1f5050000000023ba2703c53263e8d6e522dc3220" +
                "3339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500e9" +
                "a435000000008d0a96484de9b1c8c422748dba60394f5e6094d9020100004140bda6a86e1d1e325d" +
                "a2a10cec2ff7792c3bde45852b578ecf024a87183bc98304ce8ff1e491978140b7e29cc0f5dc05b8" +
                "5eab3e6cd9a72b13cee72082befba0022321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f" +
                "7fc2b7548ca2a46c4fcf4aac";

        assertEquals(expectedTx, Numeric.toHexStringNoPrefix(txBytes));
    }
}
