package io.neow3j.test;

import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContractTest(
        blockTime = 1,
        contracts = {ExampleContract1.class, ExampleContract2.class},
        batchFile = "example.batch",
        neoxpConfig = "example.neo-express"
)
public class ModuleTest {

    @RegisterExtension
    private static ContractTestExtension ext = new ContractTestExtension();

    private Neow3jExpress neow3j;

    private SmartContract contract1;
    private SmartContract contract2;

    public ModuleTest(Neow3jExpress neow3j, SmartContract contract) {
        this.neow3j = neow3j;
        contract1 = ext.getContractUnderTest(ExampleContract1.class);
        contract2 = ext.getContractUnderTest(ExampleContract2.class);
    }

    @DeployConfig(ExampleContract1.class)
    public static ContractParameter config1(DeployContext ctx) {
        return ContractParameter.integer(0);
    }

    @DeployConfig(ExampleContract2.class)
    public static ContractParameter config2(DeployContext ctx) {
        SmartContract sc = ctx.getDeployedContract(ExampleContract1.class);
        return ContractParameter.hash160(sc.getScriptHash());
    }

    @Test
    public void test() throws Throwable {
        Account a = ext.getAccount("Alice");
        Hash256 transferTx = ext.transfer(new BigInteger("1000000000"), "GAS", "genesis", "Alice");
        Await.waitUntilTransactionIsExecuted(transferTx, neow3j);

        Hash256 txHash = contract1.invokeFunction("method")
                .signers(AccountSigner.calledByEntry(a))
                .sign().send()
                .getSendRawTransaction().getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        assertEquals(log.getExecutions().get(0).getStack().get(0).getInteger().intValue(), 1);
    }

}
