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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@ContractTest(
        blockTime = 1,
        contracts = {TestContract1.class, TestContract2.class},
        batchFile = "test/example.batch",
        configFile = "test/example.neo-express"
)
public class ModuleTest {

    private static final String OWNER_ADDRESS = "NM7Aky765FG8NhhwtxjXRx7jEL1cnw7PBP";

    private static final String PERMISSION = "*";

    private static final String ALICE_SKEY = "84180ac9d6eb6fba207ea4ef9d2200102d1ebeb4b9c07e2c6a738a42742e27a5";

    // This test uses the fast-forward feature of NeoExpress. NeoExpress uses seconds in this feature while the Neo
    // nodes use milliseconds. The comparison of timestamps is done in seconds and this test here allow a tolerance
    // of 5 seconds.
    private static final int FAST_FORWARD_TOLERANCE = 5;

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
        config.setSubstitution("owner_address", OWNER_ADDRESS);
        config.setSubstitution("contract_hash", PERMISSION);
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
    @Order(1)
    public void testFastForwardOneBlockWithSeconds() throws Exception {
        // Forward a single block
        BigInteger startIndex = currentBlockIndex(neow3j);
        long startTime = getBlockTimeInSeconds(neow3j, startIndex);
        int secondsToForward = 3600;
        ext.fastForwardOneBlock(secondsToForward);
        BigInteger endIndex = currentBlockIndex(neow3j);
        long endTime = getBlockTimeInSeconds(neow3j, endIndex);

        assertThat(format("Block count did not increase by exactly 1 (%ss)", secondsToForward),
                endIndex, greaterThanOrEqualTo(startIndex.add(BigInteger.ONE))
        );
        assertThat(format("Blocktime of fastforwarded block is not %s seconds later", secondsToForward),
                endTime + FAST_FORWARD_TOLERANCE, greaterThanOrEqualTo(startTime + 60 * 60)
        );
    }

    @Test
    @Order(2)
    public void testFastForwardOneBlockWithTimeUnits() throws Exception {
        // Forward a single block and time with distinct time units
        BigInteger startIndex = currentBlockIndex(neow3j);
        long startTime = getBlockTimeInSeconds(neow3j, startIndex);
        ext.fastForwardOneBlock(30, 1, 1, 5);
        BigInteger endIndex = currentBlockIndex(neow3j);
        long endTime = getBlockTimeInSeconds(neow3j, endIndex);

        assertThat("Block count did not increase by 1 (5d+)",
                endIndex, greaterThanOrEqualTo(startIndex.add(BigInteger.ONE))
        );
        assertThat("Blocktime of fastforwarded single block is not expected time later",
                endTime + FAST_FORWARD_TOLERANCE, greaterThanOrEqualTo(startTime + (30 + 60 + 3600 + 5 * 86400))
        );
    }

    @Test
    @Order(3)
    public void testFastForwardMultipleBlocksWithoutTimeChange() throws Exception {
        // Fast forward blocks (no time change)
        BigInteger startIndex = currentBlockIndex(neow3j);
        int nrBlocksToForward = 4200;
        ext.fastForward(nrBlocksToForward);
        BigInteger endIndex = currentBlockIndex(neow3j);

        assertThat(format("Block count did not increase by %s", nrBlocksToForward),
                endIndex, greaterThanOrEqualTo(startIndex.add(BigInteger.valueOf(nrBlocksToForward)))
        );
    }

    @Test
    @Order(4)
    public void fastForwardMultipleBlocksWithSeconds() throws Throwable {
        // Forward blocks and time
        BigInteger startIndex = currentBlockIndex(neow3j);
        // count = current index
        long startTime = getBlockTimeInSeconds(neow3j, startIndex);
        int nrBlocksToForward = 100;
        int secondsToForward = 600;
        ext.fastForward(secondsToForward, nrBlocksToForward);
        BigInteger endIndex = currentBlockIndex(neow3j);
        long endTime = getBlockTimeInSeconds(neow3j, endIndex);

        assertThat(format("Block count did not increase by %s (%ss)", nrBlocksToForward, secondsToForward),
                endIndex, greaterThanOrEqualTo(startIndex.add(BigInteger.valueOf(nrBlocksToForward)))
        );
        assertThat(format("Blocktime of fastforwarded block is not %s seconds later", secondsToForward),
                endTime + FAST_FORWARD_TOLERANCE, greaterThanOrEqualTo(startTime + 10 * 60)
        );
    }

    @Test
    @Order(5)
    public void fastForwardMultipleBlocksWithTimeUnits() throws Throwable {
        Await.waitUntilBlockCountIsGreaterThan(neow3j, currentBlockIndex(neow3j).add(BigInteger.ONE));
        // Forward blocks and time with distinct time units
        BigInteger startIndex = currentBlockIndex(neow3j);
        long startTime = getBlockTimeInSeconds(neow3j, startIndex);
        int nrBlocksToForward = 2500;
        ext.fastForward(30, 1, 1, 1, nrBlocksToForward);
        BigInteger endIndex = currentBlockIndex(neow3j);
        long endTime = getBlockTimeInSeconds(neow3j, endIndex);

        assertThat(format("Block count did not increase by %s (1d+)", nrBlocksToForward),
                endIndex, greaterThanOrEqualTo(startIndex.add(BigInteger.valueOf(nrBlocksToForward)))
        );

        assertThat(format("Blocktime of fastforwarded %s blocks is not the expected time later", nrBlocksToForward),
                endTime + FAST_FORWARD_TOLERANCE, greaterThanOrEqualTo(startTime + (30 + 60 + 3600 + 86400))
        );
    }

    private long getBlockTimeInSeconds(Neow3j neow3j, BigInteger blockIndex) throws IOException {
        return neow3j.getBlock(blockIndex, false).send().getBlock().getTime() / 1000;
    }

    private BigInteger currentBlockIndex(Neow3j neow3j) throws IOException {
        return neow3j.getBlockCount().send().getBlockCount().subtract(BigInteger.ONE);
    }

}
