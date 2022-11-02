package io.neow3j.contract.types;

import io.neow3j.contract.exceptions.InvalidNeoNameException;
import io.neow3j.contract.exceptions.InvalidNeoNameServiceRootException;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NNSNameTest {

    @Test
    public void testInvalidName() {
        String invalidName = "neo..neo";
        InvalidNeoNameException thrown = assertThrows(InvalidNeoNameException.class, () -> new NNSName(invalidName));
        assertThat(thrown.getMessage(), is(format("'%s' is not a valid NNS name.", invalidName)));
    }

    @Test
    public void testSecondLevelDomain() {
        assertFalse(new NNSName("third.level.neo").isSecondLevelDomain());
        assertTrue(new NNSName("level.neo").isSecondLevelDomain());
    }

    @Test
    public void testInvalidLength() {
        // total length <3
        assertFalse(NNSName.isValidNNSName("me", false));

        // total length 255
        assertTrue(NNSName.isValidNNSName("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij" +
                ".abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij" +
                ".abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij" +
                ".abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij.neo", true));

        // total length 256
        assertFalse(NNSName.isValidNNSName("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij" +
                ".abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij" +
                ".abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij" +
                ".abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijk.neo", true));
    }

    @Test
    public void testFragmentLength() {
        // fragment length 63
        assertTrue(NNSName.isValidNNSName(
                "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijk.neo", false));
        // fragment length 64
        assertFalse(NNSName.isValidNNSName(
                "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl.neo", false));
    }

    @Test
    public void testNrFragments() {
        assertFalse(NNSName.isValidNNSName("neo", false));
        assertTrue(NNSName.isValidNNSName("neo1.neo2.neo3.neo4.neo5.neo6.neo7.neo", true));
        assertFalse(NNSName.isValidNNSName("neo1.neo2.neo3.neo4.neo5.neo6.neo7.neo8.neo", true));
    }

    @Test
    public void testRootStartNotAlpha() {
        assertFalse(NNSName.isValidNNSName("neo.4ever", false));
    }

    @Test
    public void testFragmentNotAlphaNum() {
        assertFalse(NNSName.isValidNNSName("neow3j%100.neo", false));
        assertFalse(NNSName.isValidNNSName("&neow3j100.neo", false));
    }

    @Test
    public void testSingleLengthRoot() {
        assertTrue(NNSName.isValidNNSName("neow3j.n", false));
    }

    @Test
    public void testGetBytes() {
        String name = "neow3j.neo";
        NNSName nnsName = new NNSName(name);
        assertThat(nnsName.getName(), is(name));
        assertThat(nnsName.getBytes(), is(name.getBytes(UTF_8)));
    }

    @Test
    public void testRoot() {
        NNSName.NNSRoot root = new NNSName.NNSRoot("neo");
        assertThat(root.getRoot(), is("neo"));
    }

    @Test
    public void testRootInvalid() {
        String invalidRoot = "rootrootrootroots"; // too long
        InvalidNeoNameServiceRootException thrown = assertThrows(InvalidNeoNameServiceRootException.class,
                () -> new NNSName.NNSRoot(invalidRoot));
        assertThat(thrown.getMessage(), is(format("'%s' is not a valid NNS root.", invalidRoot)));
    }

}
