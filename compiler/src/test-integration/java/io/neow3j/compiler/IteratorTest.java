package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.Map;
import io.neow3j.devpack.neo.Enumerator;
import io.neow3j.devpack.neo.Iterator;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class IteratorTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(IteratorTestContract.class.getName());
    }

    @Test
    public void createIteratorFromArrayAndIterateThrough() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                array(integer(0), integer(1), integer(2)));
        ArrayStackItem arr = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arr.size(), is(6));
        assertThat(arr.get(0).asInteger().getValue().intValue(), is(0));
        assertThat(arr.get(1).asInteger().getValue().intValue(), is(0));
        assertThat(arr.get(2).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(3).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(4).asInteger().getValue().intValue(), is(2));
        assertThat(arr.get(5).asInteger().getValue().intValue(), is(2));
    }

    @Test
    public void createIteratorFromMapAndIterateThrough() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                array(integer(3), integer(6), integer(9)),
                array(integer(3), integer(4), integer(5)));

        ArrayStackItem arr = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arr.get(0).asInteger().getValue().intValue(), is(3));
        assertThat(arr.get(1).asInteger().getValue().intValue(), is(3));
        assertThat(arr.get(2).asInteger().getValue().intValue(), is(6));
        assertThat(arr.get(3).asInteger().getValue().intValue(), is(4));
        assertThat(arr.get(4).asInteger().getValue().intValue(), is(9));
        assertThat(arr.get(5).asInteger().getValue().intValue(), is(5));
    }

    @Test
    public void createIteratorsFromArraysAndConcatenate() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                array(integer(1), integer(2), integer(3)),
                array(integer(1), integer(2), integer(3)));

        ArrayStackItem arr = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arr.get(0).asInteger().getValue().intValue(), is(0));
        assertThat(arr.get(1).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(2).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(3).asInteger().getValue().intValue(), is(2));
        assertThat(arr.get(4).asInteger().getValue().intValue(), is(2));
        assertThat(arr.get(5).asInteger().getValue().intValue(), is(3));
        assertThat(arr.get(6).asInteger().getValue().intValue(), is(0));
        assertThat(arr.get(7).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(8).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(9).asInteger().getValue().intValue(), is(2));
        assertThat(arr.get(10).asInteger().getValue().intValue(), is(2));
        assertThat(arr.get(11).asInteger().getValue().intValue(), is(3));
    }

    @Test
    public void createIteratorsFromArrayAndGetValues() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(array(integer(1), integer(2), integer(3)));
        ArrayStackItem arr = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arr.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(arr.get(1).asInteger().getValue().intValue(), is(2));
        assertThat(arr.get(2).asInteger().getValue().intValue(), is(3));
    }

    @Test
    public void createIteratorsFromMapAndGetKeys() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                array(integer(3), integer(6), integer(9)),
                array(integer(3), integer(4), integer(5)));

        ArrayStackItem arr = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(arr.get(0).asInteger().getValue().intValue(), is(3));
        assertThat(arr.get(1).asInteger().getValue().intValue(), is(6));
        assertThat(arr.get(2).asInteger().getValue().intValue(), is(9));
    }

    static class IteratorTestContract {

        public static int[] createIteratorFromArrayAndIterateThrough(Integer[] ints) {
            Iterator<Integer, Integer> it = new Iterator<>(ints);

            int[] idxAndVals = new int[ints.length* 2];
            int i = 0;
            while (it.next()) {
                idxAndVals[i++] = it.getKey();
                idxAndVals[i++] = it.getValue();
            }
            return idxAndVals;
        }

        public static int[] createIteratorFromMapAndIterateThrough(Integer[] ints1, Integer[] ints2) {
            Map<Integer, Integer> map = new Map<>();
            for (int i = 0; i < ints1.length; i++) {
                map.put(ints1[i], ints2[i]);
            }
            Iterator<Integer, Integer> it = new Iterator<>(map);

            int[] idxAndVals = new int[ints1.length* 2];
            int i = 0;
            while (it.next()) {
                idxAndVals[i++] = it.getKey();
                idxAndVals[i++] = it.getValue();
            }
            return idxAndVals;
       }

        public static int[] createIteratorsFromArraysAndConcatenate(Integer[] ints1, Integer[] ints2) {
            Iterator<Integer, Integer> it1 = new Iterator<>(ints1);
            Iterator<Integer, Integer> it2 = it1.concat(new Iterator<>(ints2));

            int[] idxAndVals = new int[ints1.length * 4];
            int i = 0;
            while (it2.next()) {
                idxAndVals[i++] = it2.getKey();
                idxAndVals[i++] = it2.getValue();
            }
            return idxAndVals;
        }

        public static int[] createIteratorsFromArrayAndGetValues(Integer[] ints) {
            Iterator<Integer, Integer> it = new Iterator<>(ints);
            Enumerator<Integer> e = it.getValues();

            int[] res = new int[ints.length];
            int i = 0;
            while (e.next()) {
                res[i++] = e.getValue();
            }
            return res;
        }

        public static int[] createIteratorsFromMapAndGetKeys(Integer[] ints1, Integer[] ints2) {
            Map<Integer, Integer> map = new Map<>();
            for (int i = 0; i < ints1.length; i++) {
                map.put(ints1[i], ints2[i]);
            }
            Iterator<Integer, Integer> it = new Iterator<>(map);
            Enumerator<Integer> e = it.getKeys();

            int[] res = new int[ints1.length];
            int i = 0;
            while (e.next()) {
                res[i++] = e.getValue();
            }
            return res;
        }

    }

}
