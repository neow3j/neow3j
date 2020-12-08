package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ListIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ListTestContract.class.getName());
    }

    @Test
    public void createStringListWithOneEntry() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.size(), is(1));
        assertThat(list.get(0).asByteString().getAsString(), is("Hello, World!"));
    }

    @Test
    public void getSizeOfStringList() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(array(string("hello"), string("world")));
        assertThat(
                response.getInvocationResult().getStack().get(0).asInteger().getValue().intValue(),
                is(2));
    }

    @Test
    public void removeFirstItemOfStringList() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(array(string("hello"), string("world")),
                integer(0));
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.size(), is(1));
        assertThat(list.get(0).asByteString().getAsString(), is("world"));
    }

    @Test
    public void clearStringList() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(array(string("hello"), string("world")));
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.size(), is(0));
    }

    @Test
    public void receiveByteArrayListAsParameter() throws IOException {
        byte[] bytes = new byte[]{0x00, 0x01, 0x02, 0x03};
        ContractParameter param = byteArray(bytes);
        NeoInvokeFunction response = callInvokeFunction(array(param, param, param));
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.size(), is(3));
        assertThat(list.get(0).asByteString().getValue(), is(bytes));
        assertThat(list.get(1).asByteString().getValue(), is(bytes));
        assertThat(list.get(2).asByteString().getValue(), is(bytes));
    }

    @Test
    public void createListFromIntegerArray() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                array(integer(1), integer(2), integer(3), integer(4)));
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.size(), is(4));
        assertThat(list.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(list.get(1).asInteger().getValue().intValue(), is(2));
        assertThat(list.get(2).asInteger().getValue().intValue(), is(3));
        assertThat(list.get(3).asInteger().getValue().intValue(), is(4));
    }

    @Test
    public void getItemFromByteArrayList() throws IOException {
        byte[] bytes = new byte[]{0x00, 0x01, 0x02, 0x03};
        ContractParameter param = byteArray(bytes);
        NeoInvokeFunction response = callInvokeFunction(array(param, param, param), integer(2));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getValue(),
                is(bytes));
    }


    @Test
    public void overwriteItemInIntegerList() throws IOException {
        ContractParameter param = integer(1);
        NeoInvokeFunction response = callInvokeFunction(array(param, param, param), integer(1),
                integer(0));
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.size(), is(3));
        assertThat(list.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(list.get(1).asInteger().getValue().intValue(), is(0));
        assertThat(list.get(2).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void setItemOutOfRangeInIntegerList() throws IOException {
        ContractParameter param = integer(1);
        NeoInvokeFunction response = callInvokeFunction("overwriteItemInIntegerList", array(param,
                param, param), integer(3));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void cloneIntegerList() throws IOException {
        ContractParameter param = integer(1);
        NeoInvokeFunction response = callInvokeFunction(array(param, param, param));
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(list.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(list.get(2).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void integerListToArray() throws IOException {
        ContractParameter param = integer(1);
        NeoInvokeFunction response = callInvokeFunction(array(param, param, param));
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(list.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(list.get(2).asInteger().getValue().intValue(), is(1));
    }

    @Test
    public void createAndFillObjectList() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        ArrayStackItem list = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(list.get(0).asByteString().getAsString(), is("hello, world!"));
        assertThat(list.get(1).asInteger().getValue().intValue(), is(10));
        assertThat(list.get(2).asMap().get("hello").asByteString().getAsString(), is("world"));
    }
    
    static class ListTestContract {

        public static List<String> createStringListWithOneEntry() {
            List<String> l = new List<>();
            l.add("Hello, World!");
            return l;
        }

        public static int getSizeOfStringList(List<String> l) {
            return l.size();
        }

        public static List<String> removeFirstItemOfStringList(List<String> l, int i) {
            l.remove(i);
            return l;
        }

        public static List<String> clearStringList(List<String> l) {
            l.clear();
            return l;
        }

        public static byte[] getItemFromByteArrayList(List<byte[]> l, int i) {
            return l.get(i);
        }

        public static List<Integer> createListFromIntegerArray(Integer[] ints) {
            return new List<>(ints);
        }

        public static List<byte[]> receiveByteArrayListAsParameter(List<byte[]> l) {
            return l;
        }

        public static List<Integer> overwriteItemInIntegerList(List<Integer> list, int i, int v) {
            list.set(i, v);
            return list;
        }

        public static List<Integer> cloneIntegerList(List<Integer> list) {
            return list.clone();
        }

        public static Integer[] integerListToArray(List<Integer> list) {
            return list.toArray();
        }

        public static List<Object> createAndFillObjectList() {
            List<Object> list = new List<>();
            list.add("hello, world!");
            list.add(10);
            Map<String, String> map = new Map<>();
            map.put("hello", "world");
            list.add(map);
            return list;
        }

    }

}
