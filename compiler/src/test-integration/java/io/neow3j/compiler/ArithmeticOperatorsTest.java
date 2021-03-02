package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArithmeticOperatorsTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ArithmeticOperatorsTestContract.class.getName());
    }

    @Test
    public void allOperators() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.integer(-100),
                ContractParameter.integer(30));

        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(-70));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(-130));
        assertThat(array.get(2).asInteger().getValue().intValue(), is(-3000));
        assertThat(array.get(3).asInteger().getValue().intValue(), is(-3));
        assertThat(array.get(4).asInteger().getValue().intValue(), is(-10));
    }

    @Test
    public void allAssignmentOperators() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(ContractParameter.integer(-100));

        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(-90));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(110));
        assertThat(array.get(2).asInteger().getValue().intValue(), is(-1000));
        assertThat(array.get(3).asInteger().getValue().intValue(), is(0));
        assertThat(array.get(4).asInteger().getValue().intValue(), is(10));
    }

    @Test
    public void addAndIncrement() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.integer(-100),
                ContractParameter.integer(100));

        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(0));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(-99));
        assertThat(array.get(2).asInteger().getValue().intValue(), is(100));
    }

    @Test
    public void incrementAndAdd() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.integer(-100),
                ContractParameter.integer(100));

        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(-99));
        assertThat(array.get(2).asInteger().getValue().intValue(), is(100));
    }

    @Test
    public void subtractAndDecrement() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.integer(-100),
                ContractParameter.integer(100));

        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(-200));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(-101));
        assertThat(array.get(2).asInteger().getValue().intValue(), is(100));
    }

    @Test
    public void decrementAndSubtract() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.integer(-100),
                ContractParameter.integer(100));

        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(-201));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(-101));
        assertThat(array.get(2).asInteger().getValue().intValue(), is(100));
    }

    static class ArithmeticOperatorsTestContract {

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

}

