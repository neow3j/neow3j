package io.neow3j.compiler;

import io.neow3j.contract.SmartContract;
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
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NFTIntegrationTest {

    private static final io.neow3j.types.Hash160 dummyScriptHash =
            new io.neow3j.types.Hash160("3e2b5b33a98bdcf205c848dd3b2a3613d7e4b957");

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            NonFungibleTokenTestContract.class.getName());

    @BeforeClass
    public static void setUp() throws Throwable {
        SmartContract sm = ct.deployContract(ConcreteNonFungibleToken.class.getName());
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
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(dummyScriptHash));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(42));
    }

    @Test
    public void testTokensOf() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(dummyScriptHash));
        List<StackItem> iter = response.getInvocationResult().getStack().get(0).getIterator();
        assertThat(iter, hasItems(
                new ByteStringStackItem(toHexStringNoPrefix(
                        "token1".getBytes(StandardCharsets.UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix(
                        "token2".getBytes(StandardCharsets.UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix(
                        "token3".getBytes(StandardCharsets.UTF_8)))));
    }

    @Test
    public void testTransfer() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(dummyScriptHash),
                byteArrayFromString("anyId"), any(null));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void testTokens() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> iter = response.getInvocationResult().getStack().get(0).getIterator();
        assertThat(iter, hasItems(
                new ByteStringStackItem(toHexStringNoPrefix(
                        "tokenOne".getBytes(StandardCharsets.UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix(
                        "tokenTwo".getBytes(StandardCharsets.UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix(
                        "tokenThree".getBytes(StandardCharsets.UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix(
                        "tokenFour".getBytes(StandardCharsets.UTF_8)))));
    }

    @Test
    public void testProperties() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArrayFromString("anyId"));
        java.util.Map<StackItem, StackItem> map =
                response.getInvocationResult().getStack().get(0).getMap();
        ByteStringStackItem nameKey = new ByteStringStackItem(
                toHexStringNoPrefix("name".getBytes(StandardCharsets.UTF_8)));
        ByteStringStackItem nameValue = new ByteStringStackItem(
                toHexStringNoPrefix("neow3jToken1".getBytes(StandardCharsets.UTF_8)));
        assertThat(map.get(nameKey), is(nameValue));
    }

    @Permission(contract = "*")
    static class NonFungibleTokenTestContract {

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

        public static boolean testTransfer(Hash160 to, ByteString tokenId, Object data) {
            return CustomNonFungibleToken.transfer(to, tokenId, data);
        }

        public static Iterator<ByteString> testTokens() {
            return CustomNonFungibleToken.tokens();
        }

        public static Map<String, String> testProperties(ByteString tokenId) {
            return CustomNonFungibleToken.properties(tokenId);
        }
    }

    @SuppressWarnings("unchecked")
    static class ConcreteNonFungibleToken {
        static final StorageContext ctx = Storage.getStorageContext();
        static final byte[] tokensOfMap = Helper.toByteArray((byte) 1);

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
            StorageMap map = ctx.createMap(tokensOfMap);
            map.put(Helper.toByteArray((byte) 1), Helper.toByteArray("token1"));
            map.put(Helper.toByteArray((byte) 2), Helper.toByteArray("token2"));
            map.put(Helper.toByteArray((byte) 3), Helper.toByteArray("token3"));
            return (Iterator<ByteString>) Storage.find(ctx, tokensOfMap, FindOptions.ValuesOnly);
        }

        public static boolean transfer(Hash160 to, ByteString tokenId, Object data) {
            return true;
        }

        public static Iterator<ByteString> tokens() {
            StorageMap map = ctx.createMap(tokensOfMap);
            map.put(Helper.toByteArray((byte) 1), Helper.toByteArray("tokenOne"));
            map.put(Helper.toByteArray((byte) 2), Helper.toByteArray("tokenTwo"));
            map.put(Helper.toByteArray((byte) 3), Helper.toByteArray("tokenThree"));
            map.put(Helper.toByteArray((byte) 4), Helper.toByteArray("tokenFour"));
            return (Iterator<ByteString>) Storage.find(ctx, tokensOfMap, FindOptions.ValuesOnly);
        }

        public static Map<String, String> properties(ByteString tokenId) {
            Map<String, String> properties = new Map<>();
            properties.put("name", "neow3jToken1");
            return properties;
        }
    }

    @ContractHash("ae174ce9d30c3b2bf6feddf022b9417113ca1604")
    static class CustomNonFungibleToken extends NonFungibleToken {
    }

}
