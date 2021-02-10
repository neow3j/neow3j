package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.contracts.FungibleToken;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class FungibleTokenTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(FungibleTokenTestContract.class.getName());
    }

    @Test
    public void callSymbolMethodOfFungibleToken() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is(NeoToken.SYMBOL));
    }

    @Test
    public void callDecimalsMethodOfFungibleToken() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0)
                        .asInteger().getValue().intValue(),
                is(NeoToken.DECIMALS));
    }

    @Test
    public void callTotalSupplyMethodOfFungibleToken() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(NeoToken.TOTAL_SUPPLY));
    }

    @Test
    public void callBalanceOfMethodOfFungibleToken() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash160(committee.getScriptHash()));
        assertThat(response.getInvocationResult().getStack().get(0)
                        .asInteger().getValue().intValue(),
                greaterThan(0));
    }

    @Test
    public void callTransferMethodOfFungibleToken() throws IOException {
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(hash160(committee.getScriptHash()),
                hash160(defaultAccount.getScriptHash()), integer(1));

        assertTrue(response.getInvocationResult().getStack().get(0).asBoolean().getValue());
    }

    @Test
    public void getScriptHashOfFungibleToken() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(NeoToken.SCRIPT_HASH.toString()));
    }

    static class FungibleTokenTestContract extends FungibleToken {

        public static String callSymbolMethodOfFungibleToken() {
            return CustomNeoToken.symbol();
        }

        public static int callDecimalsMethodOfFungibleToken() {
            return CustomNeoToken.decimals();
        }

        public static int callTotalSupplyMethodOfFungibleToken() {
            return CustomNeoToken.totalSupply();
        }

        public static int callBalanceOfMethodOfFungibleToken(Hash160 scriptHash) {
            return CustomNeoToken.balanceOf(scriptHash);
        }

        public static boolean callTransferMethodOfFungibleToken(Hash160 from, Hash160 to, int amount) {
            return CustomNeoToken.transfer(from, to, amount, new byte[]{});
        }

        public static Hash160 getScriptHashOfFungibleToken() {
            return CustomNeoToken.getHash();
        }

    }

    @ContractHash("0xf61eebf573ea36593fd43aa150c055ad7906ab83") // NEO script hash
    static class CustomNeoToken extends FungibleToken {

    }
}
