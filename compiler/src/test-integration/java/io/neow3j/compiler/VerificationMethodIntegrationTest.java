package io.neow3j.compiler;

import static org.junit.Assert.fail;

import io.neow3j.contract.NeoToken;
import io.neow3j.crypto.Sign;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.Features;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.devpack.neo.Runtime;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.BeforeClass;
import org.junit.Test;

public class VerificationMethodIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(VerificationMethodIntegrationTestContract.class.getName());
    }

    @Test
    public void callVerifyWithContractOwner() throws Throwable {
        String contractAddress = "";
        String contractScriptHash = "";

        // Send NEO to the contract.
        String txHash = new NeoToken(neow3j).transfer(wallet, contractAddress, BigDecimal.TEN)
                .sign().send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash);

        // Withdraw NEO from the contract. This should call the contract's verify method.
        Transaction tx = new NeoToken(neow3j).transfer(wallet, contractAddress, BigDecimal.TEN)
                .signers(Signer.calledByEntry(defaultAccount.getScriptHash()),
                        Signer.calledByEntry(contractScriptHash))
                .getUnsignedTransaction();
        tx.addWitness(Witness.create(tx.getHashData(), defaultAccount.getECKeyPair()));
        txHash = tx.send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash);
        neow3j.getApplicationLog(txHash).send().getApplicationLog().getNotifications();
        // TODO: Check if the application log contains the `verify` event.
        // TODO: Check if the application log show that the transaction was successful.
        fail();
    }


    @Test
    public void callVerifyWithOtherSigner() throws Throwable {
        String contractAddress = "";
        String contractScriptHash = "";

        // Send NEO to the contract.
        String txHash = new NeoToken(neow3j).transfer(wallet, contractAddress, BigDecimal.TEN)
                .sign().send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash);

        // Withdraw NEO from the contract. This should call the contract's verify method.
        Transaction tx = new NeoToken(neow3j).transfer(wallet, contractAddress, BigDecimal.TEN)
                .signers(Signer.calledByEntry(defaultAccount.getScriptHash()),
                        Signer.calledByEntry(contractScriptHash))
                .getUnsignedTransaction();
        tx.addWitness(Witness.create(tx.getHashData(), defaultAccount.getECKeyPair()));
        txHash = tx.send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash);
        neow3j.getApplicationLog(txHash).send().getApplicationLog();
        // TODO: Check if the application log contains the `verify` event.
        // TODO: Check if the application log show that the transaction was not successful.
        fail();
    }

    @Features(payable = true, hasStorage = true)
    static class VerificationMethodIntegrationTestContract {

        @DisplayName("verify")
        public static Event1Arg<String> onVerify;

        // default account
        static byte[] ownerScriptHash =
                StringLiteralHelper.addressToScriptHash("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");

        @OnVerification
        public static boolean verify() {
            onVerify.notify("Verifying...");
            return Runtime.checkWitness(ownerScriptHash);
        }

    }
}
