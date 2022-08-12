package io.neow3j.test;

import io.neow3j.contract.NeoToken;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.ContractParameter;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigInteger;

import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@ContractTest(
        blockTime = 5,
        contracts = {TestContract1.class, TestContract2.class},
        batchFile = "test/example.batch",
        configFile = "test/example.neo-express"
)
public class ModuleTest {

    private static final String OWNER_ADDRESS = "NM7Aky765FG8NhhwtxjXRx7jEL1cnw7PBP";

    private static final String PERMISSION = "*";
    private static final String ALICE_SKEY = "84180ac9d6eb6fba207ea4ef9d2200102d1ebeb4b9c07e2c6a738a42742e27a5";

    @RegisterExtension
    private static final ContractTestExtension ext = new ContractTestExtension(new NeoExpressTestContainer());

    private static Neow3j neow3j;
    private static SmartContract sc1;
    private static SmartContract sc2;
    private static final Account deployer = new Account(ECKeyPair.create(hexStringToByteArray(ALICE_SKEY)));

    @DeployConfig(TestContract1.class)
    public static DeployConfiguration config1() {
        DeployConfiguration config = new DeployConfiguration();
        config.setDeployParam(array(integer(5), hash160(deployer)));
        config.setSigner(AccountSigner.calledByEntry(deployer));
        return config;
    }

    @DeployConfig(TestContract2.class)
    public static DeployConfiguration config2(DeployContext ctx) {
        DeployConfiguration config = new DeployConfiguration();
        SmartContract sc = ctx.getDeployedContract(TestContract1.class);
        config.setDeployParam(ContractParameter.hash160(sc.getScriptHash()));
        config.setSubstitution("<owner_address>", OWNER_ADDRESS);
        config.setSubstitution("<contract_hash>", PERMISSION);
        config.setSigner(AccountSigner.global(deployer));
        return config;
    }

    @BeforeAll
    public static void setUp() {
        neow3j = ext.getNeow3j();
        sc1 = ext.getDeployedContract(TestContract1.class);
        sc2 = ext.getDeployedContract(TestContract2.class);
    }

    @Test
    public void invokeBothContracts() throws Throwable {
        InvocationResult result = sc1.callInvokeFunction("getInt").getInvocationResult();
        assertThat(result.getStack().get(0).getInteger().intValue(), is(5));

        result = sc1.callInvokeFunction("getParentContract").getInvocationResult();
        assertThat(reverseHexString(result.getStack().get(0).getHexString()), is(sc2.getScriptHash().toString()));

        result = sc2.callInvokeFunction("getChildContract").getInvocationResult();
        assertThat(result.getStack().get(0).getAddress(), is(sc1.getScriptHash().toAddress()));

        result = sc2.callInvokeFunction("getOwner").getInvocationResult();
        assertThat(result.getStack().get(0).getAddress(), is(OWNER_ADDRESS));
    }

    @Test
    public void checkContractPermission() throws Throwable {
        assertThat(sc2.getManifest().getPermissions().get(0).getContract(), is(PERMISSION));
    }

    @Test
    public void transferTokensFromGenesisAccount() throws Throwable {
        Account newAcc = ext.createAccount();
        ContractTestExtension.GenesisAccount gen = ext.getGenesisAccount();
        NeoToken neoToken = new NeoToken(neow3j);
        NeoSendRawTransaction resp = neoToken
                .transfer(gen.getMultiSigAccount(), newAcc.getScriptHash(), BigInteger.ONE)
                .getUnsignedTransaction()
                .addMultiSigWitness(gen.getMultiSigAccount().getVerificationScript(), gen.getSignerAccounts())
                .send();

        Await.waitUntilTransactionIsExecuted(resp.getSendRawTransaction().getHash(), neow3j);
        assertThat(neoToken.getBalanceOf(newAcc), is(BigInteger.ONE));
    }

    @Test
    public void fastForward() throws Throwable {
        BigInteger startCount = neow3j.getBlockCount().send().getBlockCount();
        long startTime = neow3j.getBlock(startCount.subtract(BigInteger.ONE), false).send().getBlock().getTime();
        ext.fastForward(60, 10);
        BigInteger endCount = neow3j.getBlockCount().send().getBlockCount();
        long endTime = neow3j.getBlock(endCount.subtract(BigInteger.ONE), false).send().getBlock().getTime();
        assertThat(endCount, is(startCount.add(BigInteger.TEN)));
        assertThat(endTime, is(greaterThanOrEqualTo(startTime + 60 * 1000))); // milliseconds

        startCount = endCount;
        startTime = endTime;
        ext.fastForwardOneBlock(60);
        endCount = neow3j.getBlockCount().send().getBlockCount();
        endTime = neow3j.getBlock(endCount.subtract(BigInteger.ONE), false).send().getBlock().getTime();
        assertThat(endCount, is(startCount.add(BigInteger.ONE)));
        assertThat(endTime, greaterThanOrEqualTo(startTime + 60 * 1000)); // milliseconds

        startCount = endCount;
        startTime = endTime;
        ext.fastForward(30, 1, 1, 1, 10);
        endCount = neow3j.getBlockCount().send().getBlockCount();
        endTime = neow3j.getBlock(endCount.subtract(BigInteger.ONE), false).send().getBlock().getTime();
        assertThat(endCount, is(startCount.add(BigInteger.TEN)));
        assertThat(endTime, greaterThanOrEqualTo(startTime + (30 + 60 + 3600 + 86400) * 1000)); // milliseconds

        startCount = endCount;
        startTime = endTime;
        ext.fastForwardOneBlock(30, 1, 1, 1);
        endCount = neow3j.getBlockCount().send().getBlockCount();
        endTime = neow3j.getBlock(endCount.subtract(BigInteger.ONE), false).send().getBlock().getTime();
        assertThat(endCount, is(startCount.add(BigInteger.ONE)));
        assertThat(endTime, greaterThanOrEqualTo(startTime + (30 + 60 + 3600 + 86400) * 1000)); // milliseconds

        startCount = neow3j.getBlockCount().send().getBlockCount();
        ext.fastForward(10);
        endCount = neow3j.getBlockCount().send().getBlockCount();
        assertThat(endCount, is(startCount.add(BigInteger.TEN)));
    }

}
