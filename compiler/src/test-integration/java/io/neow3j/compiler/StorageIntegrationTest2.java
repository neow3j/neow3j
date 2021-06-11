package io.neow3j.compiler;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.types.ContractParameter;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.protocol.core.response.InvocationResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

public class StorageIntegrationTest2 {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StorageIntegrationTest2.StorageIntegrationTestContract.class.getName());

    @Test
    public void putByteArrayKeyHash160Value() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = hash160(ct.getDefaultAccount());
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(ct.getDefaultAccount().getAddress()));
    }

    @Test
    public void putByteArrayKeyHash256Value() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = hash256(ct.getDeployTxHash());
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(),
                is(ct.getDeployTxHash().toLittleEndianArray()));
    }

    @Test
    public void putByteStringKeyHash160Value() throws IOException {
        int v = 10;
        ContractParameter key = byteArray("02");
        ContractParameter value = hash160(ct.getDefaultAccount());
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(ct.getDefaultAccount().getAddress()));
    }

    @Test
    public void putByteStringKeyHash256Value() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = hash256(ct.getDeployTxHash());
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(),
                is(ct.getDeployTxHash().toLittleEndianArray()));
    }

    @Test
    public void putStringKeyHash160Value() throws IOException {
        ContractParameter key = string("key");
        ContractParameter value = hash160(ct.getDefaultAccount());
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(ct.getDefaultAccount().getAddress()));
    }

    @Test
    public void putStringKeyHash256Value() throws IOException {
        ContractParameter key = string("key");
        ContractParameter value = hash256(ct.getDeployTxHash());
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(),
                is(ct.getDeployTxHash().toLittleEndianArray()));
    }

    static class StorageIntegrationTestContract {

        static StorageContext ctx = Storage.getStorageContext();

        public static ByteString putByteArrayKeyHash160Value(byte[] key, Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteArrayKeyHash256Value(byte[] key, Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteStringKeyHash160Value(ByteString key, Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteStringKeyHash256Value(ByteString key, Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putStringKeyHash160Value(String key, Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putStringKeyHash256Value(String key, Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }
    }

}
