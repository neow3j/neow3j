package io.neow3j.compiler;

import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.contracts.FungibleToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FungibleTokenTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(FungibleTokenTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void callSymbolMethodOfFungibleToken() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is(NeoToken.SYMBOL));
    }

    @Test
    public void callDecimalsMethodOfFungibleToken() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(NeoToken.DECIMALS));
    }

    @Test
    public void callTotalSupplyMethodOfFungibleToken() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(), is(NeoToken.TOTAL_SUPPLY));
    }

    @Test
    public void callBalanceOfMethodOfFungibleToken() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(ct.getCommittee().getScriptHash()));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), greaterThan(0));
    }

    @Test
    public void callTransferMethodOfFungibleToken() throws IOException {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(ct.getCommittee().getScriptHash()),
                hash160(ct.getDefaultAccount().getScriptHash()), integer(1));

        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void getHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(neoTokenHash())));
    }

    @Permission(nativeContract = NativeContract.NeoToken, methods = "*")
    static class FungibleTokenTestContract {

        static FungibleToken token = new FungibleToken("ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5");

        public static String callSymbolMethodOfFungibleToken() {
            return token.symbol();
        }

        public static int callDecimalsMethodOfFungibleToken() {
            return token.decimals();
        }

        public static int callTotalSupplyMethodOfFungibleToken() {
            return token.totalSupply();
        }

        public static int callBalanceOfMethodOfFungibleToken(Hash160 scriptHash) {
            return token.balanceOf(scriptHash);
        }

        public static boolean callTransferMethodOfFungibleToken(Hash160 from, Hash160 to, int amount) {
            return token.transfer(from, to, amount, new byte[]{});
        }

        public static Hash160 getHash() {
            return token.getHash();
        }

    }

}
