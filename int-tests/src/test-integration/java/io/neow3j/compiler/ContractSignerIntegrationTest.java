package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.OnNEP17Payment;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.ContractSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigInteger;
import java.util.ArrayList;

import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ContractSignerIntegrationTest {

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(ContractSignerOnlyContract.class.getName());

    @Test
    public void transactionWithOnlyContractSigner() throws Throwable {
        Hash256 fundingTx = ct.transferGas(ct.getContract().getScriptHash(), new BigInteger("1000000000"));
        waitUntilTransactionIsExecuted(fundingTx, ct.getNeow3j());

        byte[] script = new ScriptBuilder()
                .contractCall(ct.getContract().getScriptHash(), "ping", new ArrayList<>())
                .toArray();
        Transaction tx = new TransactionBuilder(ct.getNeow3j())
                .script(script)
                .signers(ContractSigner.calledByEntry(ct.getContract().getScriptHash()))
                .sign();

        NeoSendRawTransaction response = tx.send();
        assertFalse(response.hasError());
        Hash256 txHash = response.getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog();
        assertThat(log.getExecutions().get(0).getState(), is(NeoVMStateType.HALT));
        assertThat(log.getExecutions().get(0).getStack().get(0).getBoolean(), is(true));
    }

    static class ContractSignerOnlyContract {

        public static boolean ping() {
            return true;
        }

        @OnVerification
        public static boolean verify() {
            return true;
        }

        @OnNEP17Payment
        public static void onPayment(Hash160 sender, int amount, Object data) {
        }

    }

}
