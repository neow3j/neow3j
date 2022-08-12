package io.neow3j.crypto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;

public class Base64Test {

    @Test
    public void encodeHexValue() {
        String encoded = Base64.encode(
                "150c14242dbf5e2f6ac2568b59b7822278d571b75f17be0c14242dbf5e2f6ac2568b59b7822278d571b75f17be13c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238");
        String expected = "FQwUJC2/Xi9qwlaLWbeCInjVcbdfF74MFCQtv14vasJWi1m3giJ41XG3Xxe"
                + "+E8AMCHRyYW5zZmVyDBSJdyDYzXb08Aq/o3wO3YicII/em0FifVtSOA==";
        assertThat(encoded, is(expected));
    }

    @Test
    public void encode() {
        String encoded = Base64.encode(Numeric.hexStringToByteArray(
                "150c14242dbf5e2f6ac2568b59b7822278d571b75f17be0c14242dbf5e2f6ac2568b59b7822278d571b75f17be13c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238"));
        String expected = "FQwUJC2/Xi9qwlaLWbeCInjVcbdfF74MFCQtv14vasJWi1m3giJ41XG3Xxe"
                + "+E8AMCHRyYW5zZmVyDBSJdyDYzXb08Aq/o3wO3YicII/em0FifVtSOA==";
        assertThat(encoded, is(expected));
    }

    @Test
    public void decode() {
        byte[] decoded = Base64.decode("FQwUJC2/Xi9qwlaLWbeCInjVcbdfF74MFCQtv14vasJWi1m3giJ41XG3Xxe"
                + "+E8AMCHRyYW5zZmVyDBSJdyDYzXb08Aq/o3wO3YicII/em0FifVtSOA==");
        String expected =
                "150c14242dbf5e2f6ac2568b59b7822278d571b75f17be0c14242dbf5e2f6ac2568b59b7822278d571b75f17be13c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238";
        assertThat(Numeric.toHexStringNoPrefix(decoded), is(expected));
    }
}
