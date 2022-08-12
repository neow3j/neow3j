package io.neow3j.compiler;

import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RelationalOperatorsTest {

    // These are the names of the methods inside of the smart contract under test.
    private final static String INTEGERS_MTHD_NAME = "integers";
    private final static String LONGS_MTHD_NAME = "longs";
    private final static String BOOLEANS_MTHD_NAME = "booleans";
    private static final String STRINGS_MTHD_NAME = "strings";

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(RelationalOperators.class.getName());

    @Test
    public void unequalSmallIntegers() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(
                INTEGERS_MTHD_NAME,
                ContractParameter.integer(1),
                ContractParameter.integer(0));

        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(array.get(0).getBoolean());
        assertTrue(array.get(1).getBoolean());
        assertFalse(array.get(2).getBoolean());
        assertFalse(array.get(3).getBoolean());
        assertTrue(array.get(4).getBoolean());
        assertTrue(array.get(5).getBoolean());
    }

    @Test
    public void equalLargeIntegers() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(
                INTEGERS_MTHD_NAME,
                ContractParameter.integer(new BigInteger("100000000000000000000")),
                ContractParameter.integer(new BigInteger("100000000000000000000")));

        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
        assertFalse(array.get(2).getBoolean());
        assertTrue(array.get(3).getBoolean());
        assertFalse(array.get(4).getBoolean());
        assertTrue(array.get(5).getBoolean());
    }

    @Test
    public void unequalSmallLongs() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(
                LONGS_MTHD_NAME,
                ContractParameter.integer(1),
                ContractParameter.integer(0));

        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(array.get(0).getBoolean());
        assertTrue(array.get(1).getBoolean());
        assertFalse(array.get(2).getBoolean());
        assertFalse(array.get(3).getBoolean());
        assertTrue(array.get(4).getBoolean());
        assertTrue(array.get(5).getBoolean());
    }

    @Test
    public void equalLargeLongs() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(
                LONGS_MTHD_NAME,
                ContractParameter.integer(new BigInteger("100000000000000000000")),
                ContractParameter.integer(new BigInteger("100000000000000000000")));

        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
        assertFalse(array.get(2).getBoolean());
        assertTrue(array.get(3).getBoolean());
        assertFalse(array.get(4).getBoolean());
        assertTrue(array.get(5).getBoolean());
    }

    @Test
    public void equalBooleans() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(
                BOOLEANS_MTHD_NAME,
                ContractParameter.bool(true),
                ContractParameter.bool(true));

        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
    }

    @Test
    public void unequalBooleans() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(
                BOOLEANS_MTHD_NAME,
                ContractParameter.bool(false),
                ContractParameter.bool(true));

        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertFalse(array.get(0).getBoolean());
        assertTrue(array.get(1).getBoolean());
    }

    @Test
    public void equalStrings() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(
                STRINGS_MTHD_NAME,
                ContractParameter.string("hello, world!"),
                ContractParameter.string("hello, world!"));

        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
    }

    static class RelationalOperators {

        public static boolean[] integers(int i, int j) {
            boolean[] b = new boolean[6];
            b[0] = i == j;
            b[1] = i != j;
            b[2] = i < j;
            b[3] = i <= j;
            b[4] = i > j;
            b[5] = i >= j;
            return b;
        }

        public static boolean[] longs(long i, long j) {
            boolean[] b = new boolean[6];
            b[0] = i == j;
            b[1] = i != j;
            b[2] = i < j;
            b[3] = i <= j;
            b[4] = i > j;
            b[5] = i >= j;
            return b;
        }

        public static boolean[] booleans(boolean i, boolean j) {
            boolean[] b = new boolean[2];
            b[0] = i == j;
            b[1] = i != j;
            return b;
        }

        public static boolean[] strings(String s1, String s2) {
            boolean[] b = new boolean[2];
            b[0] = s1 == s2;
            b[1] = s1 != s2;
            return b;
        }

    }

}
