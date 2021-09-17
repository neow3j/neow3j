package io.neow3j.test;

import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContractTest(blockTime = 1)
public class ExampleTest {

    private Neow3jExpress neow3j;
    private SmartContract contract;

    @RegisterExtension
    static ContractTestExtension ext = new ContractTestExtension(ExampleContract.class);

    public ExampleTest(Neow3jExpress neow3j, SmartContract contract) {
        this.neow3j = neow3j;
        this.contract = contract;
    }

    @Test
    public void test() throws Throwable {
        Account a = ext.getAccount("account1");
        Hash256 transferTx = ext.transfer(new BigInteger("1000000000"), "GAS", "genesis",
                "account1");
        Await.waitUntilTransactionIsExecuted(transferTx, neow3j);

        Hash256 txHash = contract.invokeFunction("method")
                .signers(AccountSigner.calledByEntry(a))
                .sign().send()
                .getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        assertEquals(log.getExecutions().get(0).getStack().get(0).getInteger().intValue(), 1);
    }
}
