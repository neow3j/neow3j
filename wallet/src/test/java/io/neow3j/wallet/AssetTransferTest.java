package io.neow3j.wallet;

import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.crypto.transaction.RawScript;
import io.neow3j.crypto.transaction.RawTransaction;
import io.neow3j.crypto.transaction.RawTransactionInput;
import io.neow3j.crypto.transaction.RawTransactionOutput;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.transaction.ContractTransaction;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

public class AssetTransferTest {

    @Test
    public void test_transfer_with_normal_account_without_fee() throws IOException, ErrorResponseException {
        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(address);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        a.updateAssetBalances(neow3j);
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", "AJQ6FoaSXDFzA6wLnyZ1nFN7SGSN2oNTc3");
        AssetTransfer at = new AssetTransfer.Builder().neow3j(neow3j).account(a).output(output).build();
        RawTransaction tx = at.getTransaction();

        RawTransactionOutput expectedChange = new RawTransactionOutput(NEOAsset.HASH_ID, "99999999", a.getAddress());
        RawTransactionInput expectedInput = new RawTransactionInput("4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0);
        // The ordering of the inputs and outputs is important
        ContractTransaction expectedTx = new ContractTransaction.Builder()
                .outputs(Arrays.asList(output, expectedChange))
                .inputs(Arrays.asList(expectedInput))
                .build();
        SignatureData expectedSig = Sign.signMessage(expectedTx.toArray(), a.getECKeyPair());

        // Test Witness
        assertEquals(1, tx.getScripts().size());
        RawScript script = tx.getScripts().get(0);
        assertEquals(1, script.getInvocation().size());
        assertEquals(expectedSig, script.getInvocation().get(0).getSignature());
        assertEquals(1, script.getVerification().getAmountSignatures());
        assertEquals(a.getPublicKey(), script.getVerification().getPublicKeys().get(0));

        // Test inputs
        assertEquals(1, tx.getInputs().size());
        assertEquals(expectedInput.getPrevHash(), tx.getInputs().get(0).getPrevHash());
        assertEquals(expectedInput.getPrevIndex(), tx.getInputs().get(0).getPrevIndex());

        // Test outputs
        assertEquals(2, tx.getOutputs().size());
        // Intended output
        assertEquals(NEOAsset.HASH_ID,tx.getOutputs().get(0).getAssetId());
        assertEquals("1", tx.getOutputs().get(0).getValue());
        assertEquals("AJQ6FoaSXDFzA6wLnyZ1nFN7SGSN2oNTc3", tx.getOutputs().get(0).getAddress());
        // Change
        assertEquals(NEOAsset.HASH_ID,tx.getOutputs().get(1).getAssetId());
        assertEquals("99999999", tx.getOutputs().get(1).getValue());
        assertEquals("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", tx.getOutputs().get(1).getAddress());
    }


    @Test
    public void test_transfer_with_fee() throws IOException, ErrorResponseException {
        // setup
        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(address);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        a.updateAssetBalances(neow3j);
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", "AJQ6FoaSXDFzA6wLnyZ1nFN7SGSN2oNTc3");
        BigDecimal fee = BigDecimal.ONE;
        AssetTransfer at = new AssetTransfer.Builder().neow3j(neow3j).account(a).output(output).fee(fee).build();
        RawTransaction tx = at.getTransaction();

        RawTransactionOutput expectedChangeNeo = new RawTransactionOutput(NEOAsset.HASH_ID, "99999999", a.getAddress());
        RawTransactionOutput expectedChangeGas = new RawTransactionOutput(GASAsset.HASH_ID, "15983", a.getAddress());
        RawTransactionInput expectedInput = new RawTransactionInput("4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0);
        RawTransactionInput expectedInputFee = new RawTransactionInput("c2f7fac79531d94d406367c7feafe425f893a580fa703c7b4df9572f5944df5a", 0);
        // The ordering of the inputs and outputs is important
        ContractTransaction expectedTx = new ContractTransaction.Builder()
                .outputs(Arrays.asList(output, expectedChangeGas, expectedChangeNeo))
                .inputs(Arrays.asList(expectedInputFee, expectedInput))
                .build();
        SignatureData expectedSig = Sign.signMessage(expectedTx.toArray(), a.getECKeyPair());

        // Test Witness
        assertEquals(1, tx.getScripts().size());
        RawScript script = tx.getScripts().get(0);
        assertEquals(1, script.getInvocation().size());
        assertEquals(expectedSig, script.getInvocation().get(0).getSignature());
        assertEquals(1, script.getVerification().getAmountSignatures());
        assertEquals(a.getPublicKey(), script.getVerification().getPublicKeys().get(0));

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
    public void test_transfer_from_multisig_account() {
//        String multiSigAddress = Keys.getMultiSigAddress(2, SampleKeys.PUBLIC_KEY_1, SampleKeys.PUBLIC_KEY_2);
//        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(multiSigAddress);
//        List<BigInteger> publicKeys = Arrays.asList(SampleKeys.PUBLIC_KEY_1, SampleKeys.PUBLIC_KEY_2);
//        Account a = Account.fromMultiSigKeys(publicKeys, 2).build();
//        RawTransactionOutput txo = new RawTransactionOutput(NEOAsset.HASH_ID, "1", "AJQ6FoaSXDFzA6wLnyZ1nFN7SGSN2oNTc3");
//        Keys.getVerificationScriptFromPublicKeys(2, SampleKeys.PUBLIC_KEY_1, SampleKeys.PUBLIC_KEY_2);
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
}
