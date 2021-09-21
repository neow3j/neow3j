package io.neow3j.test;

import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Ignore
@ContractTest(
        blockTime = 1,
        contractClass = ExampleContract.class,
        batchFile = "example.batch",
        neoxpConfig = "example.neo-express"
)
public class ExampleTest {

    @RegisterExtension
    private static ContractTestExtension ext = new ContractTestExtension();

    private Neow3jExpress neow3j;

    private SmartContract contract;

    public ExampleTest(Neow3jExpress neow3j, SmartContract contract) {
        this.neow3j = neow3j;
        this.contract = contract;
    }

    @Test
    public void test() throws Throwable {
        Account a = ext.getAccount("Alice");
        Hash256 transferTx = ext.transfer(new BigInteger("1000000000"), "GAS", "genesis", "Alice");
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
