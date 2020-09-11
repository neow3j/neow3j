package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.framework.annotations.EntryPoint;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ArithmeticOperatorsTest extends CompilerTest {

    @BeforeClass
    public static void setUp() throws Exception {
        setUp(ArithmeticOperators.class.getName());
    }

    @Test
    public void allOperators() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                getTestName(),
                ContractParameter.integer(-100),
                ContractParameter.integer(30));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Ignore("The assignment operators in connection with arrays produce a DUP2 JVM opcode which is "
            + "not correctly handled by the compiler at the moment.")
    @Test
    public void allAssignmentOperators() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                getTestName(),
                ContractParameter.integer(-100));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void addAndIncrement() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                getTestName(),
                ContractParameter.integer(-100),
                ContractParameter.integer(100));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void incrementAndAdd() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                getTestName(),
                ContractParameter.integer(-100),
                ContractParameter.integer(100));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void subtractAndDecrement() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                getTestName(),
                ContractParameter.integer(-100),
                ContractParameter.integer(100));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void decrementAndSubtract() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(
                getTestName(),
                ContractParameter.integer(-100),
                ContractParameter.integer(100));

        ArrayStackItem expected = loadExpectedResultFile(ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

}

class ArithmeticOperators {

    @EntryPoint
    public static int[] allOperators(int i, int j) {
        int[] arr = new int[5];
        arr[0] = i + j;
        arr[1] = i - j;
        arr[2] = i * j;
        arr[3] = i / j;
        arr[4] = i % j;
        return arr;
    }

    public static int[] allAssignmentOperators(int i) {
        int[] arr = new int[]{10, 10, 10, 10, 10};
        arr[0] += i;
        arr[1] -= i;
        arr[2] *= i;
        arr[3] /= i;
        arr[4] %= i;
        return arr;
    }

    public static int[] addAndIncrement(int i1, int i2) {
        int i = i1++ + i2;
        return new int[]{i, i1, i2};
    }

    public static int[] incrementAndAdd(int i1, int i2) {
        int i = ++i1 + i2;
        return new int[]{i, i1, i2};
    }

    public static int[] subtractAndDecrement(int i1, int i2) {
        int i = i1-- - i2;
        return new int[]{i, i1, i2};
    }

    public static int[] decrementAndSubtract(int i1, int i2) {
        int i = --i1 - i2;
        return new int[]{i, i1, i2};
    }

}
