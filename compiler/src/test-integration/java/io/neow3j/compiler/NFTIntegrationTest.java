package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.devpack.contracts.NonFungibleToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Await;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NFTIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            NonFungibleTokenTestContract.class.getName());

    @BeforeClass
    public static void setUp() throws Throwable {
        Hash256 gasTxHash = ct.transferGas(ct.getDefaultAccount().getScriptHash(),
                new BigInteger("10000"));
        Hash256 neoTxHash = ct.transferNeo(ct.getDefaultAccount().getScriptHash(),
                new BigInteger("10000"));
        Await.waitUntilTransactionIsExecuted(gasTxHash, ct.getNeow3j());
        Await.waitUntilTransactionIsExecuted(neoTxHash, ct.getNeow3j());
        ct.deployContract(ConcreteNonFungibleToken.class.getName());
    }

    @Test
    public void testSymbol() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("CNFT"));
    }

    @Test
    public void testDecimals() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(5));
    }

    @Test
    public void testTotalSupply() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(101));
    }

    @Test
    public void testBalanceOf() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(42));
    }

    @Test
    public void testTokensOf() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> iter = response.getInvocationResult().getStack().get(0).getIterator();
        // TODO: 01.07.21 Michael: Assert correct return
    }

    @Test
    public void testTransfer() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void testTokens() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> iter = response.getInvocationResult().getStack().get(0).getIterator();
        // TODO: 01.07.21 Michael: Assert correct return
    }

    @Test
    public void testProperties() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        java.util.Map<StackItem, StackItem> map =
                response.getInvocationResult().getStack().get(0).getMap();
        assertThat(map.get("name"), is("neow3jToken1"));
    }

    @Permission(contract = "0df34998dc7d4beeece4ebaa09268c6c78c99564")
    static class NonFungibleTokenTestContract {
        static final StorageContext ctx = Storage.getStorageContext();
        static final byte mapPrefix = (byte) 1;

        public static String testSymbol() {
            return CustomNonFungibleToken.symbol();
        }

        public static int testDecimals() {
            return CustomNonFungibleToken.decimals();
        }

        public static int testTotalSupply() {
            return CustomNonFungibleToken.totalSupply();
        }

        public static int testBalanceOf(Hash160 account) {
            return CustomNonFungibleToken.balanceOf(account);
        }

        public static Iterator<ByteString> testTokensOf(Hash160 account) {
            return CustomNonFungibleToken.tokensOf(account);
        }

        public static boolean transfer(Hash160 to, ByteString tokenId, Object data) {
            return CustomNonFungibleToken.transfer(to, tokenId, data);
        }

        public static Iterator<ByteString> testTokens() {
            return CustomNonFungibleToken.tokens();
        }

        public static Map<String, String> testProperties(ByteString tokenId) {
            return CustomNonFungibleToken.properties(tokenId);
        }

    }

    static class ConcreteNonFungibleToken {
        static final StorageContext ctx = Storage.getStorageContext();
        static final byte[] mapPrefix = Helper.toByteArray((byte) 1);

        public static String symbol() {
            return "CNFT";
        }

        public static int decimals() {
            return 5;
        }

        public static int totalSupply() {
            return 101;
        }

        public static int balanceOf(io.neow3j.devpack.Hash160 account) {
            return 42;
        }

        public static Iterator<ByteString> tokensOf(io.neow3j.devpack.Hash160 account) {
            StorageMap map = ctx.createMap(mapPrefix);
            map.put(Helper.toByteArray((byte) 1), Helper.toByteArray("tokenOne"));
            map.put(Helper.toByteArray((byte) 2), Helper.toByteArray("tokenTwo"));
            map.put(Helper.toByteArray((byte) 3), Helper.toByteArray("tokenThree"));
            return (Iterator<ByteString>) Storage.find(ctx,
                    mapPrefix,
                    FindOptions.ValuesOnly);
        }

        public static boolean transfer(Hash160 to, ByteString tokenId, Object data) {
            return true;
        }

        public static Iterator<ByteString> tokens() {
            StorageMap map = ctx.createMap(mapPrefix);
            map.put(Helper.toByteArray((byte) 1), Helper.toByteArray("tokenOne"));
            map.put(Helper.toByteArray((byte) 2), Helper.toByteArray("tokenTwo"));
            map.put(Helper.toByteArray((byte) 3), Helper.toByteArray("tokenThree"));
            map.put(Helper.toByteArray((byte) 4), Helper.toByteArray("tokenFour"));
            return (Iterator<ByteString>) Storage.find(ctx,
                    mapPrefix,
                    FindOptions.ValuesOnly);
        }

        public static Map<String, String> properties(ByteString tokenId) {
            Map<String, String> properties = new Map<>();
            properties.put("name", "neow3jToken1");
            return properties;
        }
    }

    @ContractHash("0df34998dc7d4beeece4ebaa09268c6c78c99564")
    static class CustomNonFungibleToken extends NonFungibleToken {
    }

}
