package io.neow3j.compiler;

import io.neow3j.contract.ScriptHash;
import io.neow3j.devpack.Hash160;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Numeric;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ArraysConverterIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ArraysConverterIntegrationTestContract.class.getName());
    }

    @Test
    public void createStringArrayWithTwoEntries() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arrayStackItem.get(0).getString(), is("hello"));
        assertThat(arrayStackItem.get(1).getString(), is("world"));
    }

    @Test
    public void createStringArrayWithEmptySlots() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arrayStackItem.get(0).getByteArray(), is(new byte[]{}));
        assertThat(arrayStackItem.get(1).getByteArray(), is(new byte[]{}));
        assertThat(arrayStackItem.get(2).getByteArray(), is(new byte[]{}));
        assertThat(arrayStackItem.get(3).getByteArray(), is(new byte[]{}));
        assertThat(arrayStackItem.get(4).getByteArray(), is(new byte[]{}));
    }

    @Test
    public void getEmptyStringFromCreatedArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{}));
    }

    @Test
    public void createArrayOfByteArrays() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arrayStackItem.get(0).getByteArray(), is(new byte[]{0, 1, 3}));
        assertThat(arrayStackItem.get(1).getByteArray(), is(new byte[]{0}));
    }

    @Test
    public void createArrayOfIntegerArrays() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        List<StackItem> intArray = arrayStackItem.get(0).getList();
        assertThat(intArray.get(0).getInteger().intValue(), is(0));
        assertThat(intArray.get(1).getInteger().intValue(), is(1));
        assertThat(intArray.get(2).getInteger().intValue(), is(3));

        intArray = arrayStackItem.get(1).getList();
        assertThat(intArray.get(0).getInteger().intValue(), is(0));
    }

    @Test
    public void createArrayOfHash160Arrays() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        List<StackItem> hash160Array = arrayStackItem.get(0).getList();
        assertThat(hash160Array.get(0).getHexString(),
                is(ScriptHash.ZERO.toString()));
        assertThat(hash160Array.get(1).getHexString(),
                is(Numeric.reverseHexString("0f46dc4287b70117ce8354924b5cb3a47215ad93")));
        hash160Array = arrayStackItem.get(1).getList();
        assertThat(hash160Array.get(0).getHexString(),
                is(ScriptHash.ZERO.toString()));
    }

    @Test
    public void createObjectArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arrayStackItem.get(0).getInteger().intValue(), is(1));
        assertThat(arrayStackItem.get(1).getString(), is("hello, world!"));
        assertThat(arrayStackItem.get(2).getHexString(),
                is(ScriptHash.ZERO.toString()));
    }

    static class ArraysConverterIntegrationTestContract {

        public static String[] createStringArrayWithTwoEntries() {
            return new String[]{"hello", "world"};
        }

        public static String[] createStringArrayWithEmptySlots() {
            return new String[5];
        }

        public static String getEmptyStringFromCreatedArray() {
            String[] s = new String[5];
            return s[3];
        }

        public static byte[][] createArrayOfByteArrays() {
            return new byte[][]{new byte[]{0, 1, 3}, new byte[]{0}};
        }

        public static int[][] createArrayOfIntegerArrays() {
            return new int[][]{new int[]{0, 1, 3}, new int[]{0}};
        }

        public static Hash160[][] createArrayOfHash160Arrays() {
            return new Hash160[][]{
                    new Hash160[]{
                            Hash160.zero(),
                            addressToScriptHash("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj")
                    }, new Hash160[]{
                            Hash160.zero()
                    }};
        }

        public static Object[] createObjectArray() {
            return new Object[]{1, "hello, world!", Hash160.zero()};
        }

    }
}
