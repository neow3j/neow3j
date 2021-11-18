package io.neow3j.test;

import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.types.ContractParameter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ContractTest(
        blockTime = 1,
        contracts = {ExampleContract1.class, ExampleContract2.class},
        batchFile = "example.batch",
        configFile = "example.neo-express"
)
public class ModuleTest {

    private static final String OWNER_ADDRESS = "NM7Aky765FG8NhhwtxjXRx7jEL1cnw7PBP";
    private static final String PERMISSION = "*";

    @RegisterExtension
    private static ContractTestExtension ext =
            new ContractTestExtension(new NeoExpressTestContainer());

    private static Neow3j neow3j;
    private static SmartContract sc1;
    private static SmartContract sc2;

    @DeployConfig(ExampleContract1.class)
    public static void config1(DeployConfiguration config) {
        config.setDeployParam(ContractParameter.integer(5));
    }

    @DeployConfig(ExampleContract2.class)
    public static void config2(DeployConfiguration config, DeployContext ctx) {
        SmartContract sc = ctx.getDeployedContract(ExampleContract1.class);
        config.setDeployParam(ContractParameter.hash160(sc.getScriptHash()));
        config.setSubstitution("<owner_address>", OWNER_ADDRESS);
        config.setSubstitution("<contract_hash>", PERMISSION);
    }

    @BeforeAll
    public static void setUp(Neow3j n) {
        neow3j = n;
        sc1 = ext.getDeployedContract(ExampleContract1.class);
        sc2 = ext.getDeployedContract(ExampleContract2.class);
    }

    @Test
    public void test() throws Throwable {
        InvocationResult result = sc1.callInvokeFunction("getInt").getInvocationResult();
        assertThat(result.getStack().get(0).getInteger().intValue(), is(5));

        result = sc2.callInvokeFunction("getDeployer").getInvocationResult();
        assertThat(result.getStack().get(0).getAddress(), is(sc1.getScriptHash().toAddress()));

        result = sc2.callInvokeFunction("getOwner").getInvocationResult();
        assertThat(result.getStack().get(0).getAddress(), is(OWNER_ADDRESS));

        assertThat(sc2.getManifest().getPermissions().get(0).getContract(), is(PERMISSION));
    }

}