package io.neow3j.compiler;

import io.neow3j.types.ContractParameter;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.protocol.core.response.InvocationResult;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StorageContextIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StorageContextIntegrationTestContract.class.getName());

    private static final byte PREFIX1 = 0x01;
    private static final String KEY1 = "01";
    private static final String DATA1 = "0001020304";

    private static final byte[] PREFIX2 = new byte[]{(byte) 0xa1, (byte) 0xb1};
    private static final int PREFIX2_INT = -20063;
    private static final String KEY2 = "02";
    private static final String DATA2 = "0ab105c802";

    private static final String PREFIX3 = "prefix";
    private static final String KEY3 = "03";
    private static final String DATA3 = "0ab105c802";

    @BeforeClass
    public static void setUp() throws Throwable {
        byte[] bytes = concatenate(PREFIX1, hexStringToByteArray(KEY1));
        ContractParameter key = byteArray(bytes);
        ContractParameter data = byteArray(DATA1);
        ct.invokeFunctionAndAwaitExecution("storeData", key, data);

        bytes = concatenate(PREFIX2, hexStringToByteArray(KEY2));
        key = byteArray(bytes);
        data = byteArray(DATA2);
        ct.invokeFunctionAndAwaitExecution("storeData", key, data);

        bytes = concatenate(PREFIX3.getBytes(StandardCharsets.UTF_8), hexStringToByteArray(KEY3));
        key = byteArray(bytes);
        data = byteArray(DATA3);
        ct.invokeFunctionAndAwaitExecution("storeData", key, data);
    }

    @Test
    public void getReadOnlyStorageContext() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        // The method tries to write with a read-only storage context, i.e., it should FAULT.
        assertThat(res.getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void createMapWithBytePrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, integer(PREFIX1), byteArray(KEY1))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void createMapWithByteArrayPrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, byteArray(PREFIX2), byteArray(KEY2))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA2));
    }

    @Test
    public void createMapWithByteStringPrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, byteArray(PREFIX2), byteArray(KEY2))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA2));
    }

    @Test
    public void createMapWithIntegerPrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, integer(PREFIX2_INT), byteArray(KEY2))
                        .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA2));
    }

    @Test
    public void createMapWithStringPrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, string(PREFIX3), byteArray(KEY3))
                .getInvocationResult();
        // The method tries to write with a read-only storage context, i.e., it should FAULT.
        assertThat(res.getStack().get(0).getHexString(), is(DATA3));
    }


    static class StorageContextIntegrationTestContract {

        static StorageContext ctx = Storage.getStorageContext();

        public static void storeData(byte[] key, byte[] data) {
            Storage.put(ctx, key, data);
        }

        public static void getReadOnlyStorageContext() {
            StorageContext ctx = Storage.getStorageContext();
            ctx = ctx.asReadOnly();
            Storage.put(ctx, "key", "value");
        }

        public static ByteString createMapWithBytePrefix(byte prefix, ByteString key) {
            StorageContext ctx = Storage.getStorageContext();
            StorageMap map = ctx.createMap(prefix);
            return map.get(key);
        }

        public static ByteString createMapWithByteArrayPrefix(byte[] prefix, ByteString key) {
            StorageContext ctx = Storage.getStorageContext();
            StorageMap map = ctx.createMap(prefix);
            return map.get(key);
        }

        public static ByteString createMapWithByteStringPrefix(ByteString prefix, ByteString key) {
            StorageContext ctx = Storage.getStorageContext();
            StorageMap map = ctx.createMap(prefix);
            return map.get(key);
        }

        public static ByteString createMapWithIntegerPrefix(int prefix, ByteString key) {
            StorageContext ctx = Storage.getStorageContext();
            StorageMap map = ctx.createMap(prefix);
            return map.get(key);
        }

        public static ByteString createMapWithStringPrefix(String prefix, ByteString key) {
            StorageContext ctx = Storage.getStorageContext();
            StorageMap map = ctx.createMap(prefix);
            return map.get(key);
        }

    }
}
