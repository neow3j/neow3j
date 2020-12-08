package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.neo.Enumerator;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class EnumeratorTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(EnumeratorTestContract.class.getName());
    }

    @Test
    public void createEnumeratorAndIterateThroughValues() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                array(integer(0), integer(1), integer(2), integer(3)));
        ArrayStackItem arr = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arr.size(), is(4));
        assertThat(arr.get(0).asInteger().getValue().intValue(), is(0));
        assertThat(arr.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(2).asInteger().getValue().intValue(), is(2));
        assertThat(arr.get(3).asInteger().getValue().intValue(), is(3));
    }

    @Test
    public void createEnumeratorsAndConcat() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                array(integer(0), integer(1), integer(2), integer(3)),
                array(integer(4), integer(5), integer(6), integer(7)));

        ArrayStackItem arr = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arr.get(0).asInteger().getValue().intValue(), is(0));
        assertThat(arr.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(2).asInteger().getValue().intValue(), is(2));
        assertThat(arr.get(3).asInteger().getValue().intValue(), is(3));
        assertThat(arr.get(4).asInteger().getValue().intValue(), is(4));
        assertThat(arr.get(5).asInteger().getValue().intValue(), is(5));
        assertThat(arr.get(6).asInteger().getValue().intValue(), is(6));
        assertThat(arr.get(7).asInteger().getValue().intValue(), is(7));
    }

    static class EnumeratorTestContract {

        public static int[] createEnumeratorAndIterateThroughValues(Integer[] ints) {
            int[] res = new int[ints.length];
            Enumerator<Integer> e = new Enumerator<>(ints);
            int i = 0;
            while (e.next()) {
                res[i++] = e.getValue();
            }
            return res;
        }

        public static int[] createEnumeratorsAndConcat(Integer[] ints1, Integer[] ints2) {
            Enumerator<Integer> e1 = new Enumerator<>(ints1);
            Enumerator<Integer> e2 = new Enumerator<>(ints2);
            int[] res = new int[ints1.length + ints2.length];
            int i = 0;
            while (e1.next()) {
                res[i++] = e1.getValue();
            }
            while (e2.next()) {
                res[i++] = e2.getValue();
            }
            return res;
        }

    }

}
