package io.neow3j.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringsTest {

    @Test
    public void testToCsv() {
        assertThat(Strings.toCsv(Collections.<String>emptyList()), is(""));
        assertThat(Strings.toCsv(Collections.singletonList("a")), is("a"));
        assertThat(Strings.toCsv(Arrays.asList("a", "b", "c")), is("a, b, c"));
    }

    @Test
    public void testJoin() {
        assertThat(Strings.join(Arrays.asList("a", "b"), "|"), is("a|b"));
        assertNull(Strings.join(null, "|"));
        assertThat(Strings.join(Collections.singletonList("a"), "|"), is("a"));
    }

    @Test
    public void testCapitaliseFirstLetter() {
        assertThat(Strings.capitaliseFirstLetter(""), is(""));
        assertThat(Strings.capitaliseFirstLetter("a"), is("A"));
        assertThat(Strings.capitaliseFirstLetter("aa"), is("Aa"));
        assertThat(Strings.capitaliseFirstLetter("A"), is("A"));
        assertThat(Strings.capitaliseFirstLetter("Ab"), is("Ab"));
    }

    @Test
    public void testLowercaseFirstLetter() {
        assertThat(Strings.lowercaseFirstLetter(""), is(""));
        assertThat(Strings.lowercaseFirstLetter("A"), is("a"));
        assertThat(Strings.lowercaseFirstLetter("AA"), is("aA"));
        assertThat(Strings.lowercaseFirstLetter("a"), is("a"));
        assertThat(Strings.lowercaseFirstLetter("aB"), is("aB"));
    }

    @Test
    public void testRepeat() {
        assertThat(Strings.repeat('0', 0), is(""));
        assertThat(Strings.repeat('1', 3), is("111"));
    }

    @Test
    public void testZeros() {
        assertThat(Strings.zeros(0), is(""));
        assertThat(Strings.zeros(3), is("000"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testEmptyString() {
        assertTrue(Strings.isEmpty(null));
        assertTrue(Strings.isEmpty(""));
        assertFalse(Strings.isEmpty("hello world"));
    }

}
