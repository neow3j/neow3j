package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.types.ContractParameter;
import io.neow3j.wallet.Account;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StorageMapIntegrationTest2 {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StorageMapIntegrationTest2.StorageMapIntegrationTestContract.class.getName());

    @Test
    public void putByteArrayKeyHash160Value() throws IOException {
        Account v = ct.getDefaultAccount();
        ContractParameter key = byteArray("02");
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.getAddress()));
    }

    @Test
    public void putByteArrayKeyHash256Value() throws IOException {
        String v = ct.getDeployTxHash().toString();
        ContractParameter key = byteArray("02");
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(reverseHexString(v)));
    }

    @Test
    public void putByteStringKeyHash160Value() throws IOException {
        Account v = ct.getDefaultAccount();
        ContractParameter key = byteArray("02");
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.getAddress()));
    }

    @Test
    public void putByteStringKeyHash256Value() throws IOException {
        String v = ct.getDeployTxHash().toString();
        ContractParameter key = byteArray("02");
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(reverseHexString(v)));
    }

    @Test
    public void putStringKeyHash160Value() throws IOException {
        Account v = ct.getDefaultAccount();
        ContractParameter key = string("aa");
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.getAddress()));
    }

    @Test
    public void putStringKeyHash256Value() throws IOException {
        String v = ct.getDeployTxHash().toString();
        ContractParameter key = string("aa");
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(reverseHexString(v)));
    }

    static class StorageMapIntegrationTestContract {

        static ByteString prefix = hexToBytes("0001");
        static StorageContext ctx = Storage.getStorageContext();
        static StorageMap map = new StorageMap(ctx, prefix.toByteArray());

        public static ByteString putByteArrayKeyHash160Value(byte[] key, Hash160 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteArrayKeyHash256Value(byte[] key, Hash256 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteStringKeyHash160Value(ByteString key, Hash160 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteStringKeyHash256Value(ByteString key, Hash256 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putStringKeyHash160Value(String key, Hash160 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putStringKeyHash256Value(String key, Hash256 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }
    }

}
