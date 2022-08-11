package io.neow3j.compiler;

import io.neow3j.devpack.Map;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash256;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.map;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(MapTests.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void putAndGetFromMap() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("hello"), string("world"));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("world"));
    }

    @Test
    public void getMapValues() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"), string("olleh"), string("dlrow"));
        List<StackItem> items = response.getInvocationResult().getStack().get(0).getList();
        assertThat(items.get(0).getString(), is("world"));
        assertThat(items.get(1).getString(), is("dlrow"));
    }

    @Test
    public void getMapKeys() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"), string("olleh"), string("dlrow"));
        List<StackItem> items = response.getInvocationResult().getStack().get(0).getList();
        assertThat(items.get(0).getString(), is("hello"));
        assertThat(items.get(1).getString(), is("olleh"));
    }

    @Test
    public void mapContainsKey() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"), string("olleh"), string("dlrow"));
        assertThat(response.getInvocationResult().getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void removeFromMap() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"), string("olleh"), string("dlrow"));
        List<StackItem> items = response.getInvocationResult().getStack().get(0).getList();
        assertThat(items.get(0).getString(), is("olleh"));
    }

    @Test
    public void passMapAsParameter() throws Throwable {
        java.util.Map<Object, Object> map = new HashMap<>();
        map.put(byteArray("7365636f6e64"), true);
        map.put(integer(1), "string");

        Hash256 hash = ct.invokeFunctionAndAwaitExecution(testName, map(map));

        List<StackItem> values = ct.getNeow3j().getApplicationLog(hash).send().getApplicationLog()
                .getExecutions().get(0).getStack().get(0).getList();

        if (values.get(0).getType().equals(StackItemType.BYTE_STRING)) {
            assertThat(values.get(0).getHexString(), is("7365636f6e64"));
            assertThat(values.get(1).getInteger(), is(BigInteger.ONE));
            assertTrue(values.get(2).getBoolean());
            assertThat(values.get(3).getString(), is("string"));
        } else {
            assertThat(values.get(0).getInteger(), is(BigInteger.ONE));
            assertThat(values.get(1).getHexString(), is("7365636f6e64"));
            assertThat(values.get(2).getString(), is("string"));
            assertTrue(values.get(3).getBoolean());
        }
    }

    @Test
    public void returnMapStringValue() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> stack = response.getInvocationResult().getStack();
        assertThat(stack, hasSize(1));
        java.util.Map<StackItem, StackItem> map = stack.get(0).getMap();

        Optional<String> stringValue = map.entrySet().stream()
                .filter(e -> e.getKey().getString().equals("stringKey"))
                .map(e -> e.getValue().getString())
                .findFirst();
        assertTrue(stringValue.isPresent());
        assertThat(stringValue.get(), is("value"));
    }

    @Test
    public void returnMapIntegerValue() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> stack = response.getInvocationResult().getStack();
        assertThat(stack, hasSize(1));
        java.util.Map<StackItem, StackItem> map = stack.get(0).getMap();

        Optional<BigInteger> integerValue = map.entrySet().stream()
                .filter(e -> e.getKey().getString().equals("integerKey"))
                .map(e -> e.getValue().getInteger())
                .findFirst();
        assertTrue(integerValue.isPresent());
        assertThat(integerValue.get(), is(BigInteger.valueOf(42)));
    }

    @Test
    public void returnMapArrayValue() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> stack = response.getInvocationResult().getStack();
        assertThat(stack, hasSize(1));
        java.util.Map<StackItem, StackItem> map = stack.get(0).getMap();

        List<StackItem> value = map.entrySet().stream()
                .filter(e -> e.getKey().getString().equals("mapKey"))
                .map(java.util.Map.Entry::getValue)
                .collect(Collectors.toList());
        List<StackItem> arr = value.get(0).getList();
        assertThat(arr.get(0).getString(), is("arrValue1"));
        assertThat(arr.get(1).getString(), is("arrValue2"));
    }

    @Test
    public void returnMapMapValue() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> stack = response.getInvocationResult().getStack();
        assertThat(stack, hasSize(1));
        java.util.Map<StackItem, StackItem> map = stack.get(0).getMap();

        List<StackItem> value = map.entrySet().stream()
                .filter(e -> e.getKey().getString().equals("mapKey1"))
                .map(java.util.Map.Entry::getValue)
                .collect(Collectors.toList());

        java.util.Map<StackItem, StackItem> nestedMap = value.get(0).getMap();
        String nKey1Value = nestedMap.entrySet().stream()
                .filter(e -> e.getKey().getString().equals("nKey1"))
                .map(java.util.Map.Entry::getValue)
                .map(StackItem::getString)
                .findFirst()
                .get();
        assertThat(nKey1Value, is("nValue1"));

        String nKey2Value = nestedMap.entrySet().stream()
                .filter(e -> e.getKey().getString().equals("nKey2"))
                .map(java.util.Map.Entry::getValue)
                .map(StackItem::getString)
                .findFirst()
                .get();
        assertThat(nKey2Value, is("nValue2"));
    }

    static class MapTests {

        public static String putAndGetFromMap(String s1, String s2) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            return m.get(s1);
        }

        public static String[] getMapValues(String s1, String s2, String s3, String s4) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            m.put(s3, s4);
            return m.values();
        }

        public static String[] getMapKeys(String s1, String s2, String s3, String s4) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            m.put(s3, s4);
            return m.keys();
        }

        public static boolean mapContainsKey(String s1, String s2, String s3, String s4) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            m.put(s3, s4);
            return m.containsKey(s1);
        }

        public static String[] removeFromMap(String s1, String s2, String s3, String s4) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            m.put(s3, s4);
            m.remove(s1);
            return m.keys();
        }

        public static io.neow3j.devpack.List<Object> passMapAsParameter(Map<Object, Object> map) {
            io.neow3j.devpack.List<Object> keysAndValues = new io.neow3j.devpack.List<>();
            for (Object o : map.keys()) {
                keysAndValues.add(o);
            }
            for (Object o : map.values()) {
                keysAndValues.add(o);
            }
            return keysAndValues;
        }

        public static Map<String, String> returnMapStringValue() {
            Map<String, String> map = new Map<>();
            map.put("stringKey", "value");
            return map;
        }

        public static Map<String, Integer> returnMapIntegerValue() {
            Map<String, Integer> map = new Map<>();
            map.put("integerKey", 42);
            return map;
        }

        public static Map<String, Object> returnMapArrayValue() {
            Map<String, Object> map = new Map<>();
            String[] valueMap = new String[2];
            valueMap[0] = "arrValue1";
            valueMap[1] = "arrValue2";
            map.put("mapKey", valueMap);
            return map;
        }

        public static Map<String, Object> returnMapMapValue() {
            Map<String, Object> map = new Map<>();
            Map<String, Object> nestedMap = new Map<>();
            nestedMap.put("nKey1", "nValue1");
            nestedMap.put("nKey2", "nValue2");
            map.put("mapKey1", nestedMap);
            return map;
        }

    }

}
