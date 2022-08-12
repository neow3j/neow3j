package io.neow3j.crypto;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SecureRandomUtilsTest {

    @Test
    public void testSecureRandom() {
        SecureRandomUtils.secureRandom().nextInt();
    }

    @Test
    public void testIsNotAndroidRuntime() {
        assertFalse(SecureRandomUtils.isAndroidRuntime());
    }

    @Test
    public void testGenerateRandomBytes() {
        assertThat(SecureRandomUtils.generateRandomBytes(0), is(new byte[]{}));
        assertThat(SecureRandomUtils.generateRandomBytes(10).length, is(10));
    }

}
