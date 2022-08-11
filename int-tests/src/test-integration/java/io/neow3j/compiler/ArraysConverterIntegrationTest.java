package io.neow3j.compiler;

import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.Notification;
import io.neow3j.protocol.core.stackitem.ArrayStackItem;
import io.neow3j.protocol.core.stackitem.BufferStackItem;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.IntegerStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static io.neow3j.test.TestProperties.cryptoLibHash;
import static io.neow3j.test.TestProperties.gasTokenHash;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArraysConverterIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            ArraysConverterIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void createStringArrayWithTwoEntries() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arrayStackItem.get(0).getString(), is("hello"));
        assertThat(arrayStackItem.get(1).getString(), is("world"));
    }

    @Test
    public void createStringArrayWithEmptySlots() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arrayStackItem.get(0).getByteArray(), is(new byte[]{}));
        assertThat(arrayStackItem.get(1).getByteArray(), is(new byte[]{}));
        assertThat(arrayStackItem.get(2).getByteArray(), is(new byte[]{}));
        assertThat(arrayStackItem.get(3).getByteArray(), is(new byte[]{}));
        assertThat(arrayStackItem.get(4).getByteArray(), is(new byte[]{}));
    }

    @Test
    public void getEmptyStringFromCreatedArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{}));
    }

    @Test
    public void createArrayOfByteArrays() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arrayStackItem.get(0).getByteArray(), is(new byte[]{0, 1, 3}));
        assertThat(arrayStackItem.get(1).getByteArray(), is(new byte[]{0}));
    }

    @Test
    public void createArrayOfIntegerArrays() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
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
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        List<StackItem> hash160Array = arrayStackItem.get(0).getList();
        assertThat(hash160Array.get(0).getHexString(),
                is(Hash160.ZERO.toString()));
        assertThat(hash160Array.get(1).getHexString(),
                is(Numeric.reverseHexString("0f46dc4287b70117ce8354924b5cb3a47215ad93")));
        hash160Array = arrayStackItem.get(1).getList();
        assertThat(hash160Array.get(0).getHexString(),
                is(Hash160.ZERO.toString()));
    }

    @Test
    public void createObjectArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> arrayStackItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arrayStackItem.get(0).getInteger().intValue(), is(1));
        assertThat(arrayStackItem.get(1).getString(), is("hello, world!"));
        assertThat(arrayStackItem.get(2).getHexString(), is(Hash160.ZERO.toString()));
    }

    @Test
    public void variableLengthParam() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(1), array(integer(10), integer(23),
                integer(100), integer(42)));
        int sum = response.getInvocationResult().getStack().get(0).getInteger().intValue();
        assertThat(sum, is(186));
    }

    @Test
    public void callRuntimeNotifyWithVarArgs() throws Throwable {
        Hash160 hash1 = new Hash160(gasTokenHash());
        Hash160 hash2 = new Hash160(neoTokenHash());
        Hash160 hash3 = new Hash160(cryptoLibHash());
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution(testName,
                array(hash160(hash1), hash160(hash2), hash160(hash3)));
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog();
        List<Notification> notifications = log.getExecutions().get(0).getNotifications();
        assertThat(notifications.get(0).getState().getList().get(0).getHexString(),
                is(Numeric.reverseHexString(gasTokenHash())));
        assertThat(notifications.get(1).getState().getList().get(0).getHexString(),
                is(Numeric.reverseHexString(neoTokenHash())));
        assertThat(notifications.get(2).getState().getList().get(0).getHexString(),
                is(Numeric.reverseHexString(cryptoLibHash())));
    }

    @Test
    public void stringVarArgs() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, array(string("hello, "), string("world!")));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("hello, world!"));
    }

    @Test
    public void multiArrayInit() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(), is(new byte[]{1, 2, 3, 4, 5, 6}));
    }

    @Test
    public void multiArrayThreeDimensions() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(), is(new byte[]{1, 3, 8, 12, 6}));
    }

    @Test
    public void multiArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list, hasSize(3));
        assertThat(list.get(0).getType(), is(StackItemType.ANY));
        assertNull(list.get(0).getValue());
        assertThat(list.get(1).getType(), is(StackItemType.ARRAY));

        List<StackItem> list1 = list.get(1).getList();
        assertThat(list1, hasSize(2));
        assertThat(list1.get(0).getInteger().intValue(), is(3));
        assertThat(list1.get(1).getInteger().intValue(), is(5));

        List<StackItem> list2 = list.get(2).getList();
        assertThat(list2, hasSize(4));
        IntegerStackItem oneIntItem = new IntegerStackItem(BigInteger.ONE);
        assertThat(list2.get(0), is(oneIntItem));
        assertThat(list2.get(3).getInteger(), is(BigInteger.valueOf(77L)));
    }

    @Test
    public void multiArrayString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list, hasSize(3));
        assertThat(list.get(0).getType(), is(StackItemType.ANY));
        assertNull(list.get(0).getValue());
        assertThat(list.get(1).getType(), is(StackItemType.ARRAY));

        List<StackItem> list1 = list.get(1).getList();
        assertThat(list1, hasSize(2));
        assertThat(list1.get(0).getString(), is("abc"));
        assertThat(list1.get(1).getString(), is("def"));

        List<StackItem> list2 = list.get(2).getList();
        assertThat(list2, hasSize(4));
        assertThat(list2.get(0).getString(), is("zz"));
        assertThat(list2.get(3).getString(), is("helloworld"));
    }

    @Test
    public void multiArrayObject() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list, hasSize(3));
        assertThat(list.get(0).getType(), is(StackItemType.ANY));
        assertNull(list.get(0).getValue());
        assertThat(list.get(1).getType(), is(StackItemType.ARRAY));

        List<StackItem> list1 = list.get(1).getList();
        assertThat(list1, hasSize(2));
        assertThat(list1.get(0).getString(), is("abc"));
        assertThat(list1.get(1).getString(), is("def"));

        List<StackItem> list2 = list.get(2).getList();
        assertThat(list2, hasSize(3));
        assertThat(list2.get(0).getString(), is("m"));
        assertThat(list2.get(1).getString(), is("neow"));
        assertThat(list2.get(2).getString(), is("hello moon!"));
    }

    @Test
    public void multiArrayObjectThreeDimensions() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> outer = response.getInvocationResult().getStack().get(0).getList();
        assertThat(outer, hasSize(3));
        assertThat(outer.get(0).getType(), is(StackItemType.ARRAY));

        List<StackItem> second = outer.get(0).getList();
        assertThat(second, hasSize(2));
        assertThat(second.get(0).getType(), is(StackItemType.ARRAY));
        assertThat(second.get(0).getList().get(1).getString(), is("def"));
        assertThat(second.get(1).getList().get(2).getByteArray(), is(new byte[]{1, 42}));
        assertThat(second.get(1).getList().get(3).getString(), is("helloworld"));

        assertThat(outer.get(1).getType(), is(StackItemType.ANY));
        assertNull(outer.get(1).getValue());
        assertThat(outer.get(2).getType(), is(StackItemType.ANY));
        assertNull(outer.get(2).getValue());
    }

    @Test
    public void multiArrayObjectThreeDimensionsInit() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> outer = response.getInvocationResult().getStack().get(0).getList();
        assertThat(outer, hasSize(1));

        List<StackItem> second = outer.get(0).getList();
        assertThat(second, hasSize(2));
        assertThat(second.get(0).getList().get(0).getString(), is("abc"));
        assertThat(second.get(0).getList().get(1).getString(), is("def"));
        ArrayStackItem expectedArrayStackItem = new ArrayStackItem(
                asList(new IntegerStackItem(BigInteger.ONE),
                        new ByteStringStackItem(toHexStringNoPrefix("neow".getBytes())),
                        new BufferStackItem(new byte[]{1, 42}),
                        new ByteStringStackItem(toHexStringNoPrefix("helloworld".getBytes()))));
        assertThat(second.get(1), is(expectedArrayStackItem));
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

        public static io.neow3j.devpack.Hash160[][] createArrayOfHash160Arrays() {
            return new io.neow3j.devpack.Hash160[][]{
                    new io.neow3j.devpack.Hash160[]{
                            io.neow3j.devpack.Hash160.zero(),
                            addressToScriptHash("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj")
                    }, new io.neow3j.devpack.Hash160[]{
                    io.neow3j.devpack.Hash160.zero()
            }};
        }

        public static Object[] createObjectArray() {
            return new Object[]{1, "hello, world!", io.neow3j.devpack.Hash160.zero()};
        }

        public static int variableLengthParam(int a, int... integers) {
            int sum = integers[0];
            for (int i : integers) {
                sum += i;
            }
            return sum + a;
        }

        static Event1Arg<io.neow3j.devpack.Hash160> event;

        public static void callRuntimeNotifyWithVarArgs(io.neow3j.devpack.Hash160... hashes) {
            for (io.neow3j.devpack.Hash160 hash : hashes) {
                event.fire(hash);
            }
        }

        public static String stringVarArgs(String... strings) {
            String s = "";
            for (String st : strings) {
                s += st;
            }
            return s;
        }

        public static byte[] multiArrayInit() {
            byte[][] bytes = {new byte[]{1, 2}, new byte[]{3, 4}, new byte[]{5, 6}};
            return new byte[]{bytes[0][0], bytes[0][1], bytes[1][0], bytes[1][1], bytes[2][0],
                    bytes[2][1]};
        }

        public static byte[] multiArrayThreeDimensions() {
            byte[][][] bytes = new byte[][][]{
                    new byte[][]{new byte[]{1, 2, 3}, new byte[]{4, 5, 6}},
                    new byte[][]{new byte[]{7, 8, 9}, new byte[]{10, 11, 12}}
            };
            return new byte[]{bytes[0][0][0], bytes[0][0][2], bytes[1][0][1], bytes[1][1][2],
                    bytes[0][1][2]};
        }

        public static int[][] multiArray() {
            int[][] ints = new int[3][];
            int[] val1 = {3, 5};
            int[] val2 = {1, 42, 3, 77};
            ints[1] = val1;
            ints[2] = val2;
            return ints;
        }

        public static String[][] multiArrayString() {
            String[][] strs = new String[3][];
            String[] val1 = {"abc", "def"};
            String[] val2 = {"zz", "neow", "neo", "helloworld"};
            strs[1] = val1;
            strs[2] = val2;
            return strs;
        }

        public static Object[][] multiArrayObject() {
            Object[][] strs = new Object[3][];
            String[] val1 = {"abc", "def"};
            String[] val2 = {"m", "neow", "hello moon!"};
            strs[1] = val1;
            strs[2] = val2;
            return strs;
        }

        public static Object[][][] multiArrayObjectThreeDimensions() {
            Object[][][] outer = new Object[3][][];
            Object[] val1 = {"abc", "def"};
            Object[] val2 = {1, "neow", new byte[]{1, 42}, "helloworld"};
            Object[][] middle = new Object[][]{val1, val2};
            outer[0] = middle;
            return outer;
        }

        public static Object[][][] multiArrayObjectThreeDimensionsInit() {
            Object[][][] outer =
                    new Object[][][]{
                            new Object[][]{
                                    new Object[]{"abc", "def"},
                                    new Object[]{1, "neow", new byte[]{1, 42}, "helloworld"}}};
            return outer;
        }

    }

}
