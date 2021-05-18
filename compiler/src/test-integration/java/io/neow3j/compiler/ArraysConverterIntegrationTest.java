package io.neow3j.compiler;

import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.TestProperties.cryptoLibHash;
import static io.neow3j.TestProperties.gasTokenHash;
import static io.neow3j.TestProperties.neoTokenHash;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ArraysConverterIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            ArraysConverterIntegrationTestContract.class.getName());

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
        assertThat(arrayStackItem.get(2).getHexString(),
                is(Hash160.ZERO.toString()));
    }

    @Test
    public void variableLengthParam() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(1),
                array(integer(10), integer(23), integer(100), integer(42)));
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
        List<NeoApplicationLog.Execution.Notification> notifications =
                log.getExecutions().get(0).getNotifications();
        assertThat(notifications.get(0).getState().getList().get(0).getHexString(),
                is(Numeric.reverseHexString(gasTokenHash())));
        assertThat(notifications.get(1).getState().getList().get(0).getHexString(),
                is(Numeric.reverseHexString(neoTokenHash())));
        assertThat(notifications.get(2).getState().getList().get(0).getHexString(),
                is(Numeric.reverseHexString(cryptoLibHash())));
    }

    @Test
    public void stringVarArgs() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                array(string("hello, "), string("world!")));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("hello, world!"));
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

    }

}
