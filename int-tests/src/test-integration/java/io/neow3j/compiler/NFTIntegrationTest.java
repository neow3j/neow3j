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
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.devpack.contracts.NonFungibleToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.map;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NFTIntegrationTest {

    private static final io.neow3j.types.Hash160 dummyScriptHash =
            new io.neow3j.types.Hash160("3e2b5b33a98bdcf205c848dd3b2a3613d7e4b957");

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(NonFungibleTokenTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @BeforeAll
    public static void setUp() throws Throwable {
        SmartContract sc = ct.deployContract(ConcreteNonFungibleToken.class.getName());
        ct.setHash(sc.getScriptHash());

        // Populate the tokens stored in the contract for the tokens() method.
        HashMap<Integer, String> map = new HashMap<>();
        map.put(1, "tokenOne");
        map.put(2, "tokenTwo");
        map.put(3, "tokenThree");
        map.put(4, "tokenFour");
        ct.invokeFunctionAndAwaitExecution("storeTokens", map(map));

        // Populate the tokens stored in the contract for the tokensOf() method.
        ArrayList<String> list = new ArrayList<>();
        list.add("tokenTen");
        list.add("tokenEleven");
        list.add("tokenTwelve");
        ct.invokeFunctionAndAwaitExecution("storeTokensOf", hash160(dummyScriptHash), array(list));
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
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(101));
    }

    @Test
    public void testBalanceOf() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(dummyScriptHash));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(42));
    }

    @Test
    public void testTokensOf() throws IOException {
        List<StackItem> iter = ct.callAndTraverseIterator(testName, hash160(dummyScriptHash));
        assertThat(iter, hasItems(
                new ByteStringStackItem(toHexStringNoPrefix("tokenTen".getBytes(UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix("tokenEleven".getBytes(UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix("tokenTwelve".getBytes(UTF_8)))));
    }

    @Test
    public void testTransfer() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(dummyScriptHash), byteArrayFromString("anyId"), any(null));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void testTokens() throws Throwable {
        List<StackItem> iter = ct.callAndTraverseIterator(testName);
        assertThat(iter, hasItems(
                new ByteStringStackItem(toHexStringNoPrefix("tokenOne".getBytes(UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix("tokenTwo".getBytes(UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix("tokenThree".getBytes(UTF_8))),
                new ByteStringStackItem(toHexStringNoPrefix("tokenFour".getBytes(UTF_8)))));
    }

    @Test
    public void testProperties() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArrayFromString("anyId"));
        java.util.Map<StackItem, StackItem> map = response.getInvocationResult().getStack().get(0).getMap();
        ByteStringStackItem nameKey = new ByteStringStackItem(toHexStringNoPrefix("name".getBytes(UTF_8)));
        ByteStringStackItem nameValue = new ByteStringStackItem(toHexStringNoPrefix("neow3jToken1".getBytes(UTF_8)));
        assertThat(map.get(nameKey), is(nameValue));
    }

    @Permission(contract = "*")
    static class NonFungibleTokenTestContract {

        public static String testSymbol() {
            return new NonFungibleToken(getHash()).symbol();
        }

        public static int testDecimals() {
            return new NonFungibleToken(getHash()).decimals();
        }

        public static int testTotalSupply() {
            return new NonFungibleToken(getHash()).totalSupply();
        }

        public static int testBalanceOf(Hash160 account) {
            return new NonFungibleToken(getHash()).balanceOf(account);
        }

        // Used to populate the tokens that then get returned by the tokens() method.
        public static void storeTokens(Map<Integer, byte[]> tokenMap) {
            new TestNFTContract(getHash()).storeTokens(tokenMap);
        }

        // Used to populate the tokens that then get returned by the tokensOf() method.
        public static void storeTokensOf(Hash160 owner, List<String> tokens) {
            new TestNFTContract(getHash()).storeTokensOf(owner, tokens);
        }

        public static Iterator<ByteString> testTokensOf(Hash160 account) {
            return new NonFungibleToken(getHash()).tokensOf(account);
        }

        public static boolean testTransfer(Hash160 to, ByteString tokenId, Object data) {
            return new NonFungibleToken(getHash()).transfer(to, tokenId, data);
        }

        public static Iterator<ByteString> testTokens() {
            return new NonFungibleToken(getHash()).tokens();
        }

        public static Map<String, String> testProperties(ByteString tokenId) {
            return new NonFungibleToken(getHash()).properties(tokenId);
        }

        public static void setHash(Hash160 contractHash) {
            Storage.put(Storage.getStorageContext(), 0xff, contractHash);
        }

        private static Hash160 getHash() {
            return Storage.getHash160(Storage.getReadOnlyContext(), 0xff);
        }
    }

    static class TestNFTContract extends NonFungibleToken {
        public TestNFTContract(Hash160 contractHash) {
            super(contractHash);
        }

        public native void storeTokens(Map<Integer, byte[]> tokens);

        public native void storeTokensOf(Hash160 owner, List<String> tokens);
    }

    @SuppressWarnings("unchecked")
    static class ConcreteNonFungibleToken {
        static final StorageContext ctx = Storage.getStorageContext();
        static final byte[] tokensPrefix = Helper.toByteArray((byte) 1);
        static final byte[] tokensOfPrefix = Helper.toByteArray((byte) 2);

        public static String symbol() {
            return "CNFT";
        }

        public static int decimals() {
            return 5;
        }

        public static int totalSupply() {
            return 101;
        }

        public static int balanceOf(Hash160 account) {
            return 42;
        }

        public static void storeTokensOf(Hash160 owner, io.neow3j.devpack.List<String> tokens) {
            byte[] prefix = Helper.concat(tokensOfPrefix, owner.toByteArray());
            StorageMap map = new StorageMap(ctx, prefix);
            int nrTokens = tokens.size();
            for (int i = 0; i < nrTokens; i++) {
                map.put(tokens.get(i), 1);
            }
        }

        public static Iterator<ByteString> tokensOf(Hash160 owner) {
            byte[] concat = Helper.concat(tokensOfPrefix, owner.toByteArray());
            return (Iterator<ByteString>) Storage.find(ctx, concat, FindOptions.RemovePrefix | FindOptions.KeysOnly);
        }

        public static boolean transfer(Hash160 to, ByteString tokenId, Object data) {
            return true;
        }

        public static void storeTokens(Map<Hash160, byte[]> tokens) {
            StorageMap map = new StorageMap(ctx, tokensPrefix);
            Hash160[] keys = tokens.keys();
            for (Hash160 key : keys) {
                map.put(key, tokens.get(key));
            }
        }

        public static Iterator<ByteString> tokens() {
            return (Iterator<ByteString>) Storage.find(ctx, tokensPrefix, FindOptions.ValuesOnly);
        }

        public static Map<String, String> properties(ByteString tokenId) {
            Map<String, String> properties = new Map<>();
            properties.put("name", "neow3jToken1");
            return properties;
        }

    }

}
