package io.neow3j.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionTest {

    @Test
    public void testJoin() {
        final ArrayList<String> list = new ArrayList<>();
        assertEquals("", Collection.join(list, ","));

        list.add("test1");
        assertEquals("test1", Collection.join(list, ","));

        list.add("test2");
        assertEquals("test1,test2", Collection.join(list, ","));
        assertEquals("test1:sep:test2", Collection.join(list, ":sep:"));
        assertEquals("test1test2", Collection.join(list, ""));
    }

    @Test
    public void testJoinWithFunction() {
        final Collection.Function<Integer, String> parityFunction = x -> x % 2 == 0 ? "even" : "odd";

        final ArrayList<Integer> list = new ArrayList<>();
        assertEquals("", Collection.join(list, ",", parityFunction));

        list.add(1);
        assertEquals("odd", Collection.join(list, ",", parityFunction));

        list.add(2);
        assertEquals("odd,even", Collection.join(list, ",", parityFunction));
        assertEquals("odd:sep:even", Collection.join(list, ":sep:", parityFunction));
        assertEquals("oddeven", Collection.join(list, "", parityFunction));
    }

    @Test
    public void testTail() {
        assertArrayEquals(new String[]{}, Collection.tail(new String[]{}));
        assertArrayEquals(new String[]{}, Collection.tail(new String[]{"1"}));
        assertArrayEquals(new String[]{"2"}, Collection.tail(new String[]{"1", "2"}));
        assertArrayEquals(new String[]{"2", "3"}, Collection.tail(new String[]{"1", "2", "3"}));
    }

}
