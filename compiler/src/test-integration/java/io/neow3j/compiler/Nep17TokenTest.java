package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.contracts.Nep17Token;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class Nep17TokenTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(Nep17TokenTestContract.class.getName());
    }

    @Test
    public void callSymbolMethodOfNep17Token() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is(NeoToken.SYMBOL));
    }

    @Test
    public void callDecimalsMethodOfNep17Token() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0)
                        .asInteger().getValue().intValue(),
                is(NeoToken.DECIMALS));
    }

    @Test
    public void callTotalSupplyMethodOfNep17Token() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(NeoToken.TOTAL_SUPPLY));
    }

    @Test
    public void callBalanceOfMethodOfNep17Token() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash160(committee.getScriptHash()));
        assertThat(response.getInvocationResult().getStack().get(0)
                        .asInteger().getValue().intValue(),
                greaterThan(0));
    }

    @Test
    public void callTransferMethodOfNep17Token() throws IOException {
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(hash160(committee.getScriptHash()),
                hash160(defaultAccount.getScriptHash()), integer(1));

        assertTrue(response.getInvocationResult().getStack().get(0).asBoolean().getValue());
    }

    @Test
    public void getScriptHashOfNep17Token() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(NeoToken.SCRIPT_HASH.toString()));
    }

    static class Nep17TokenTestContract extends Nep17Token {

        public static String callSymbolMethodOfNep17Token() {
            return CustomNeoToken.symbol();
        }

        public static int callDecimalsMethodOfNep17Token() {
            return CustomNeoToken.decimals();
        }

        public static int callTotalSupplyMethodOfNep17Token() {
            return CustomNeoToken.totalSupply();
        }

        public static int callBalanceOfMethodOfNep17Token(byte[] scriptHash) {
            return CustomNeoToken.balanceOf(scriptHash);
        }

        public static boolean callTransferMethodOfNep17Token(byte[] from, byte[] to, int amount) {
            return CustomNeoToken.transfer(from, to, amount, new byte[]{});
        }

        public static byte[] getScriptHashOfNep17Token() {
            return CustomNeoToken.getHash();
        }

    }

    @ContractHash("0x0a46e2e37c9987f570b4af253fb77e7eef0f72b6") // NEO script hash
    static class CustomNeoToken extends Nep17Token {

    }
}
