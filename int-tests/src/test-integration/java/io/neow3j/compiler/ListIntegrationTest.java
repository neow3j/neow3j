package io.neow3j.compiler;

import io.neow3j.devpack.Map;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.NeoVMStateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;

import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ListIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(ListTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void createStringListWithOneEntry() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> list =
                response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getString(), is("Hello, World!"));
    }

    @Test
    public void getSizeOfStringList() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, array(string("hello"), string("world")));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(2));
    }

    @Test
    public void removeFirstItemOfStringList() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, array(string("hello"), string("world")),
                        integer(0));
        List<StackItem> list =
                response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getString(), is("world"));
    }

    @Test
    public void clearStringList() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, array(string("hello"), string("world")));
        List<StackItem> list =
                response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.size(), is(0));
    }

    @Test
    public void receiveByteArrayListAsParameter() throws IOException {
        byte[] bytes = new byte[]{0x00, 0x01, 0x02, 0x03};
        ContractParameter param = byteArray(bytes);
        NeoInvokeFunction response = ct.callInvokeFunction(testName, array(param, param, param));
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.size(), is(3));
        assertThat(list.get(0).getByteArray(), is(bytes));
        assertThat(list.get(1).getByteArray(), is(bytes));
        assertThat(list.get(2).getByteArray(), is(bytes));
    }

    @Test
    public void createListFromIntegerArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                array(integer(1), integer(2), integer(3), integer(4)));
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.size(), is(4));
        assertThat(list.get(0).getInteger().intValue(), is(1));
        assertThat(list.get(1).getInteger().intValue(), is(2));
        assertThat(list.get(2).getInteger().intValue(), is(3));
        assertThat(list.get(3).getInteger().intValue(), is(4));
    }

    @Test
    public void getItemFromByteArrayList() throws IOException {
        byte[] bytes = new byte[]{0x00, 0x01, 0x02, 0x03};
        ContractParameter param = byteArray(bytes);
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, array(param, param, param), integer(2));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(bytes));
    }


    @Test
    public void overwriteItemInIntegerList() throws IOException {
        ContractParameter param = integer(1);
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, array(param, param, param), integer(1),
                        integer(0));
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.size(), is(3));
        assertThat(list.get(0).getInteger().intValue(), is(1));
        assertThat(list.get(1).getInteger().intValue(), is(0));
        assertThat(list.get(2).getInteger().intValue(), is(1));
    }

    @Test
    public void setItemOutOfRangeInIntegerList() throws IOException {
        ContractParameter param = integer(1);
        NeoInvokeFunction response =
                ct.callInvokeFunction("overwriteItemInIntegerList", array(param,
                        param, param), integer(3));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void cloneIntegerList() throws IOException {
        ContractParameter param = integer(1);
        NeoInvokeFunction response = ct.callInvokeFunction(testName, array(param, param, param));
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.get(0).getInteger().intValue(), is(1));
        assertThat(list.get(1).getInteger().intValue(), is(1));
        assertThat(list.get(2).getInteger().intValue(), is(1));
    }

    @Test
    public void integerListToArray() throws IOException {
        ContractParameter param = integer(1);
        NeoInvokeFunction response = ct.callInvokeFunction(testName, array(param, param, param));
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.get(0).getInteger().intValue(), is(1));
        assertThat(list.get(1).getInteger().intValue(), is(1));
        assertThat(list.get(2).getInteger().intValue(), is(1));
    }

    @Test
    public void createAndFillObjectList() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list.get(0).getString(), is("hello, world!"));
        assertThat(list.get(1).getInteger().intValue(), is(10));
        ByteStringStackItem key = new ByteStringStackItem("hello".getBytes(UTF_8));
        assertThat(list.get(2).getMap().get(key).getString(), is("world"));
    }

    @Test
    public void reverseList() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> list = response.getInvocationResult().getStack().get(0).getList();
        assertThat(list, hasSize(5));
        assertThat(list.get(0).getInteger().intValue(), is(4));
        assertThat(list.get(1).getString(), is("three"));
        assertThat(list.get(2).getInteger().intValue(), is(2));
        assertThat(list.get(3).getInteger().intValue(), is(1));
        assertThat(list.get(4).getInteger().intValue(), is(5));
    }

    static class ListTestContract {

        public static io.neow3j.devpack.List<String> createStringListWithOneEntry() {
            io.neow3j.devpack.List<String> l = new io.neow3j.devpack.List<>();
            l.add("Hello, World!");
            return l;
        }

        public static int getSizeOfStringList(io.neow3j.devpack.List<String> l) {
            return l.size();
        }

        public static io.neow3j.devpack.List<String> removeFirstItemOfStringList(
                io.neow3j.devpack.List<String> l, int i) {
            l.remove(i);
            return l;
        }

        public static io.neow3j.devpack.List<String> clearStringList(
                io.neow3j.devpack.List<String> l) {
            l.clear();
            return l;
        }

        public static byte[] getItemFromByteArrayList(io.neow3j.devpack.List<byte[]> l, int i) {
            return l.get(i);
        }

        public static io.neow3j.devpack.List<Integer> createListFromIntegerArray(Integer[] ints) {
            return new io.neow3j.devpack.List<>(ints);
        }

        public static io.neow3j.devpack.List<byte[]> receiveByteArrayListAsParameter(
                io.neow3j.devpack.List<byte[]> l) {
            return l;
        }

        public static io.neow3j.devpack.List<Integer> overwriteItemInIntegerList(
                io.neow3j.devpack.List<Integer> list, int i, int v) {
            list.set(i, v);
            return list;
        }

        public static io.neow3j.devpack.List<Integer> cloneIntegerList(
                io.neow3j.devpack.List<Integer> list) {
            return list.clone();
        }

        public static Integer[] integerListToArray(io.neow3j.devpack.List<Integer> list) {
            return list.toArray();
        }

        public static io.neow3j.devpack.List<Object> createAndFillObjectList() {
            io.neow3j.devpack.List<Object> list = new io.neow3j.devpack.List<>();
            list.add("hello, world!");
            list.add(10);
            Map<String, String> map = new Map<>();
            map.put("hello", "world");
            list.add(map);
            return list;
        }

        public static io.neow3j.devpack.List<Object> reverseList() {
            io.neow3j.devpack.List<Object> list = new io.neow3j.devpack.List<>();
            list.add(true);
            list.add(2);
            list.add("three");
            list.add(4);
            list.reverse();
            list.add(5);
            return list;
        }

    }

}
