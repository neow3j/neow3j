package io.neow3j.test;

import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ContractTest(
        blockTime = 1,
        contracts = {ExampleContract1.class, ExampleContract2.class},
        batchFile = "example.batch",
        neoxpConfig = "example.neo-express"
)
public class ModuleTest {

    @RegisterExtension
    private static ContractTestExtension ext = new ContractTestExtension();

    private static Neow3jExpress neow3j;
    private static SmartContract sc1;
    private static SmartContract sc2;

    @DeployConfig(ExampleContract1.class)
    public static ContractParameter config1(DeployContext ctx) {
        return ContractParameter.integer(5);
    }

    @DeployConfig(ExampleContract2.class)
    public static ContractParameter config2(DeployContext ctx) {
        SmartContract sc = ctx.getDeployedContract(ExampleContract1.class);
        return ContractParameter.hash160(sc.getScriptHash());
    }

    @BeforeAll
    public static void setUp(Neow3jExpress neow3jExpress, DeployContext ctx) {
        neow3j = neow3jExpress;
        sc1 = ctx.getDeployedContract(ExampleContract1.class);
        sc2 = ctx.getDeployedContract(ExampleContract2.class);
    }

    @Test
    public void test() throws Throwable {
        Hash256 transferTx = ext.transfer(new BigInteger("1000000000"), "GAS", "genesis", "Alice");
        Await.waitUntilTransactionIsExecuted(transferTx, neow3j);

        InvocationResult result = sc1.callInvokeFunction("getInt").getInvocationResult();
        assertThat(result.getStack().get(0).getInteger().intValue(), is(5));

        result = sc2.callInvokeFunction("getOwner").getInvocationResult();
        assertThat(result.getStack().get(0).getAddress(), is(sc1.getScriptHash().toAddress()));
    }

}