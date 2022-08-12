package io.neow3j.compiler;

import io.neow3j.contract.GasToken;
import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.devpack.contracts.FungibleToken;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.Notification;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.test.TestProperties.gasTokenHash;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.test.TestProperties.stdLibHash;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContractInterfaceIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            ContractInterfaceIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void callSymbolOfFungibleToken() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("NEO"));
    }

    @Test
    public void callSymbolOfFungibleTokenWithStaticHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("NEO"));
    }

    @Test
    public void callSymbolOfStaticFungibleToken() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("GAS"));
    }

    @Test
    public void transferFungibleToken() throws Throwable {
        ct.signWithCommitteeAccount();
        Account acc = Account.create();
        Hash256 hash = ct.invokeFunction(testName, hash160(ct.getCommittee()), hash160(acc),
                integer(BigInteger.valueOf(10_00000000)));
        Await.waitUntilTransactionIsExecuted(hash, ct.getNeow3j());

        NeoApplicationLog.Execution exec = ct.getNeow3j().getApplicationLog(hash).send().getApplicationLog()
                .getExecutions().get(0);
        assertThat(exec.getState(), is(NeoVMStateType.HALT));
        assertTrue(exec.getStack().get(0).getBoolean());

        assertThat(exec.getNotifications().get(0).getContract(), is(GasToken.SCRIPT_HASH));
        assertThat(exec.getNotifications().get(0).getEventName(), is("Transfer"));
        assertThat(exec.getNotifications().get(0).getState().getList().get(0).getAddress(),
                is(ct.getCommittee().getAddress()));
        assertThat(exec.getNotifications().get(0).getState().getList().get(1).getAddress(), is(acc.getAddress()));
        assertThat(exec.getNotifications().get(0).getState().getList().get(2).getInteger(),
                is(BigInteger.valueOf(10_00000000)));
    }

    @Test
    public void transferFungibleTokenWithStaticHashToClaimGas() throws Throwable {
        ct.signWithCommitteeAccount();
        Hash256 hash = ct.invokeFunction(testName, hash160(ct.getCommittee()));
        Await.waitUntilTransactionIsExecuted(hash, ct.getNeow3j());

        NeoApplicationLog.Execution exec = ct.getNeow3j().getApplicationLog(hash).send().getApplicationLog()
                .getExecutions().get(0);
        assertThat(exec.getState(), is(NeoVMStateType.HALT));
        assertTrue(exec.getStack().get(0).getBoolean());

        assertThat(exec.getNotifications().get(0).getContract(), is(NeoToken.SCRIPT_HASH));
        assertThat(exec.getNotifications().get(0).getEventName(), is("Transfer"));
        assertThat(exec.getNotifications().get(0).getState().getList().get(0).getAddress(),
                is(ct.getCommittee().getAddress()));
        assertThat(exec.getNotifications().get(0).getState().getList().get(1).getAddress(),
                is(ct.getCommittee().getAddress()));
        assertThat(exec.getNotifications().get(0).getState().getList().get(2).getInteger(), is(BigInteger.ZERO));

        assertThat(exec.getNotifications().get(1).getContract(), is(GasToken.SCRIPT_HASH));
        assertThat(exec.getNotifications().get(1).getEventName(), is("Transfer"));
        assertThat(exec.getNotifications().get(1).getState().getList().get(0).getType(), is(StackItemType.ANY));
        assertNull(exec.getNotifications().get(1).getState().getList().get(0).getValue());
        assertThat(exec.getNotifications().get(1).getState().getList().get(1).getAddress(),
                is(ct.getCommittee().getAddress()));
    }

    @Test
    public void transferStaticFungibleToken() throws Throwable {
        ct.signWithCommitteeAccount();
        Account acc = Account.create();
        Hash256 hash = ct.invokeFunction(testName, hash160(ct.getCommittee()), hash160(acc));
        Await.waitUntilTransactionIsExecuted(hash, ct.getNeow3j());

        NeoApplicationLog.Execution exec = ct.getNeow3j().getApplicationLog(hash).send().getApplicationLog()
                .getExecutions().get(0);
        assertThat(exec.getState(), is(NeoVMStateType.HALT));
        assertTrue(exec.getStack().get(0).getBoolean());

        assertThat(exec.getNotifications(), hasSize(1));
        Notification notification = exec.getNotifications().get(0);

        assertThat(notification.getContract(), is(GasToken.SCRIPT_HASH));
        assertThat(notification.getEventName(), is("Transfer"));
        assertThat(notification.getState().getList().get(0).getAddress(), is(ct.getCommittee().getAddress()));
        assertThat(notification.getState().getList().get(1).getAddress(), is(acc.getAddress()));
        assertThat(notification.getState().getList().get(2).getInteger(), is(BigInteger.valueOf(2_00000000)));
    }

    @Test
    public void transferStaticFungibleTokenAndAssertReturn() throws Throwable {
        ct.signWithCommitteeAccount();
        Account acc = Account.create();
        Hash256 hash = ct.invokeFunction(testName, hash160(ct.getCommittee()), hash160(acc), integer(12345678));
        Await.waitUntilTransactionIsExecuted(hash, ct.getNeow3j());

        NeoApplicationLog.Execution exec = ct.getNeow3j().getApplicationLog(hash).send().getApplicationLog()
                .getExecutions().get(0);
        assertThat(exec.getState(), is(NeoVMStateType.HALT));
        assertTrue(exec.getStack().get(0).getBoolean());

        assertThat(exec.getNotifications(), hasSize(1));
        Notification notification = exec.getNotifications().get(0);

        assertThat(notification.getContract(), is(GasToken.SCRIPT_HASH));
        assertThat(notification.getEventName(), is("Transfer"));
        assertThat(notification.getState().getList().get(0).getAddress(), is(ct.getCommittee().getAddress()));
        assertThat(notification.getState().getList().get(1).getAddress(), is(acc.getAddress()));
        assertThat(notification.getState().getList().get(2).getInteger(), is(BigInteger.valueOf(12345678)));
    }

    @Test
    public void callDecimalsOfFungibleTokenInOneLine() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(8));
    }

    @Test
    public void getUnclaimedGasFromNeoTokenWithParam() throws Throwable {
        BigInteger blockCount = ct.getNeow3j().getBlockCount().send().getBlockCount();
        BigInteger unclaimedGas = new NeoToken(ct.getNeow3j()).unclaimedGas(ct.getCommittee(), blockCount.longValue());
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(NeoToken.SCRIPT_HASH),
                hash160(ct.getCommittee()),
                integer(blockCount));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(), is(unclaimedGas));
    }

    @Test
    public void getHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(reverseHexString(stdLibHash())));
    }

    @Test
    public void getHashFromContractInterfaceInInitsslot() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(reverseHexString(gasTokenHash())));
    }

    @Test
    public void getHashFromContractInterfaceWithHashFromInitsslotValue() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(reverseHexString(neoTokenHash())));
    }

    @Permission(nativeContract = NativeContract.NeoToken)
    @Permission(nativeContract = NativeContract.GasToken)
    static class ContractInterfaceIntegrationTestContract {

        static final String cst = "ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5";
        static final CustomFungibleToken customFungibleToken =
                new CustomFungibleToken("d2a4cff31913016155e38e474a2c06d08be276cf");

        public static String callSymbolOfFungibleToken() {
            CustomFungibleToken customFungibleToken =
                    new CustomFungibleToken("ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5");
            return customFungibleToken.symbol();
        }

        public static String callSymbolOfFungibleTokenWithStaticHash() {
            CustomFungibleToken customFungibleToken = new CustomFungibleToken(cst);
            return customFungibleToken.symbol();
        }

        public static String callSymbolOfStaticFungibleToken() {
            return customFungibleToken.symbol();
        }

        public static boolean transferFungibleToken(Hash160 from, Hash160 to, int amount) {
            CustomFungibleToken customFungibleToken =
                    new CustomFungibleToken("d2a4cff31913016155e38e474a2c06d08be276cf");
            return customFungibleToken.transfer(from, to, amount, "mydata");
        }

        public static boolean transferFungibleTokenWithStaticHashToClaimGas(Hash160 gasClaimer) {
            CustomFungibleToken customFungibleToken = new CustomFungibleToken(cst);
            int amount = 0;
            return customFungibleToken.transfer(gasClaimer, gasClaimer, amount, null);
        }

        public static boolean transferStaticFungibleToken(Hash160 from, Hash160 to) {
            int amount = 80000000 + 1_20000000;
            return customFungibleToken.transfer(from, to, amount, null);
        }

        public static String transferStaticFungibleTokenAndAssertReturn(Hash160 from, Hash160 to, int amount) {
            assert customFungibleToken.transfer(from, to, amount, null);
            return "Success";
        }

        public static int callDecimalsOfFungibleTokenInOneLine() {
            return new CustomFungibleToken("d2a4cff31913016155e38e474a2c06d08be276cf").decimals();
        }

        public static int getUnclaimedGasFromNeoTokenWithParam(Hash160 contractHash, Hash160 account, int blockIndex) {
            CustomFungibleToken customFungibleToken = new CustomFungibleToken(contractHash);
            assert customFungibleToken.getHash() == new io.neow3j.devpack.contracts.NeoToken().getHash();
            return customFungibleToken.unclaimedGas(account, blockIndex);
        }

        public static Hash160 getHash() {
            return new CustomContract("acce6fd80d44e1796aa0c2c625e9e4e0ce39efc0").getHash();
        }

        public static Hash160 getHashFromContractInterfaceInInitsslot() {
            return customFungibleToken.getHash();
        }

        public static Hash160 getHashFromContractInterfaceWithHashFromInitsslotValue() {
            return new CustomContract(cst).getHash();
        }
    }

    static class CustomContract extends ContractInterface {
        public CustomContract(String contractHash) {
            super(contractHash);
        }
    }

    static class CustomFungibleToken extends FungibleToken {

        public CustomFungibleToken(Hash160 contractHash) {
            super(contractHash);
        }

        public CustomFungibleToken(String contractHash) {
            super(contractHash);
        }

        public native int unclaimedGas(Hash160 scriptHash, int blockIndex);

    }

}
