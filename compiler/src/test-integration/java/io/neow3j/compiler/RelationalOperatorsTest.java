package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.framework.annotations.EntryPoint;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.BeforeClass;
import org.junit.Test;

public class RelationalOperatorsTest extends CompilerTest {

    // These are the names of the methods inside of the smart contract under test.
    private final static String INTEGERS_MTHD_NAME = "integers";
    private final static String LONGS_MTHD_NAME = "longs";
    private final static String BOOLEANS_MTHD_NAME = "booleans";
    private static final String STRINGS_MTHD_NAME = "strings";

    @BeforeClass
    public static void setUp() throws Exception {
        setUp(RelationalOperators.class.getName());
    }

    @Test
    public void unequalSmallIntegers() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                INTEGERS_MTHD_NAME,
                ContractParameter.integer(1),
                ContractParameter.integer(0));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void equalLargeIntegers() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                INTEGERS_MTHD_NAME,
                ContractParameter.integer(new BigInteger("100000000000000000000")),
                ContractParameter.integer(new BigInteger("100000000000000000000")));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void unequalSmallLongs() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                LONGS_MTHD_NAME,
                ContractParameter.integer(1),
                ContractParameter.integer(0));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void equalLargeLongs() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                LONGS_MTHD_NAME,
                ContractParameter.integer(new BigInteger("100000000000000000000")),
                ContractParameter.integer(new BigInteger("100000000000000000000")));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void equalBooleans() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                BOOLEANS_MTHD_NAME,
                ContractParameter.bool(true),
                ContractParameter.bool(true));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void unequalBooleans() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                BOOLEANS_MTHD_NAME,
                ContractParameter.bool(false),
                ContractParameter.bool(true));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void equalStrings() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                STRINGS_MTHD_NAME,
                ContractParameter.string("hello, world!"),
                ContractParameter.string("hello, world!"));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }
}

class RelationalOperators {

    @EntryPoint
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