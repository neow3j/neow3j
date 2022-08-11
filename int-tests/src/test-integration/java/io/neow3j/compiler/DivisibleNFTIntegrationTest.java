package io.neow3j.compiler;

import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.devpack.contracts.DivisibleNonFungibleToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
public class DivisibleNFTIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(TestContract.class.getName());

    @BeforeAll
    public static void setUp() throws Throwable {
        SmartContract sc = ct.deployContract(ConcreteDivisibleNFT.class.getName());
        ct.setHash(sc.getScriptHash());
    }

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void testTransfer() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(io.neow3j.types.Hash160.ZERO), hash160(io.neow3j.types.Hash160.ZERO), integer(10),
                byteArrayFromString("anyId"), any(null));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void testOwnerOf() throws IOException {
        List<StackItem> iter = ct.callAndTraverseIterator(testName, byteArrayFromString("io/neow3j/test"));
        assertThat(iter.get(0).getAddress(), is("NSdNMyrz7Bp8MXab41nTuz1mRCnsFr5Rsv"));
        assertThat(iter.get(1).getAddress(), is("NhxK1PEmijLVD6D4WSuPoUYJVk855L21ru"));
    }

    @Test
    public void testOwnerOfWithoutSessions() throws IOException {
        List<StackItem> iteratorArray = ct.getContract()
                .callFunctionAndUnwrapIterator("testOwnerOf", asList(byteArrayFromString("io/neow3j/test")), 1000);

        assertThat(iteratorArray, hasSize(2));
        assertThat(iteratorArray.get(0).getAddress(), is("NSdNMyrz7Bp8MXab41nTuz1mRCnsFr5Rsv"));
        assertThat(iteratorArray.get(1).getAddress(), is("NhxK1PEmijLVD6D4WSuPoUYJVk855L21ru"));

        iteratorArray = ct.getContract()
                .callFunctionAndUnwrapIterator("testOwnerOf", asList(byteArrayFromString("io/neow3j/test")), 2);

        assertThat(iteratorArray, hasSize(2));
        assertThat(iteratorArray.get(0).getAddress(), is("NSdNMyrz7Bp8MXab41nTuz1mRCnsFr5Rsv"));
        assertThat(iteratorArray.get(1).getAddress(), is("NhxK1PEmijLVD6D4WSuPoUYJVk855L21ru"));

        iteratorArray = ct.getContract()
                .callFunctionAndUnwrapIterator("testOwnerOf", asList(byteArrayFromString("io/neow3j/test")), 1);
        assertThat(iteratorArray, hasSize(1));
        assertThat(iteratorArray.get(0).getAddress(), is("NSdNMyrz7Bp8MXab41nTuz1mRCnsFr5Rsv"));
    }

    @Test
    public void testBalanceOf() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash160(Account.create()), byteArrayFromString("id1"));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(38));
    }

    @Permission(contract = "*")
    static class TestContract {

        public static boolean testTransfer(Hash160 from, Hash160 to, int amount, ByteString tokenId, Object data) {
            return new DivisibleNonFungibleToken(getHash()).transfer(from, to, amount, tokenId, data);
        }

        public static Iterator<Hash160> testOwnerOf(ByteString tokenId) {
            return new DivisibleNonFungibleToken(getHash()).ownerOf(tokenId);
        }

        public static int testBalanceOf(Hash160 account, ByteString tokenId) {
            return new DivisibleNonFungibleToken(getHash()).balanceOf(account, tokenId);
        }

        public static void setHash(Hash160 contractHash) {
            Storage.put(Storage.getStorageContext(), 0xff, contractHash);
        }

        private static Hash160 getHash() {
            return Storage.getHash160(Storage.getReadOnlyContext(), 0xff);
        }

    }

    static class ConcreteDivisibleNFT {
        static final StorageContext ctx = Storage.getStorageContext();
        static final byte[] mapPrefix = Helper.toByteArray((byte) 1);

        public static boolean transfer(Hash160 from, Hash160 to, int amount, ByteString tokenId, Object data) {
            return true;
        }

        public static Iterator<ByteString> ownerOf(ByteString tokenId) {
            StorageMap map = new StorageMap(ctx, mapPrefix);
            map.put(Helper.toByteArray((byte) 1), addressToScriptHash("NSdNMyrz7Bp8MXab41nTuz1mRCnsFr5Rsv"));
            map.put(Helper.toByteArray((byte) 2), addressToScriptHash("NhxK1PEmijLVD6D4WSuPoUYJVk855L21ru"));
            return (Iterator<ByteString>) Storage.find(ctx, mapPrefix, FindOptions.ValuesOnly);
        }

        public static int balanceOf(Hash160 account, ByteString tokenId) {
            return 38;
        }

    }

}
