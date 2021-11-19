package io.neow3j.compiler;

import io.neow3j.devpack.Map;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash256;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.map;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MapTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(MapTests.class.getName());

    @Test
    public void putAndGetFromMap() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("world"));
    }

    @Test
    public void getMapValues() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"),
                        string("olleh"), string("dlrow"));
        List<StackItem> items = response.getInvocationResult().getStack().get(0).getList();
        assertThat(items.get(0).getString(), is("world"));
        assertThat(items.get(1).getString(), is("dlrow"));
    }

    @Test
    public void getMapKeys() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"),
                        string("olleh"), string("dlrow"));
        List<StackItem> items = response.getInvocationResult().getStack().get(0).getList();
        assertThat(items.get(0).getString(), is("hello"));
        assertThat(items.get(1).getString(), is("olleh"));
    }

    @Test
    public void mapContainsKey() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"),
                        string("olleh"), string("dlrow"));
        assertThat(response.getInvocationResult().getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void removeFromMap() throws IOException {
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, string("hello"), string("world"),
                        string("olleh"), string("dlrow"));
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

        assertThat(values.get(0).getHexString(), is("7365636f6e64"));
        assertThat(values.get(1).getString(), is("string"));
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

        public static Object[] passMapAsParameter(Map<Object, Object> map) {
            return map.values();
        }

    }

}
