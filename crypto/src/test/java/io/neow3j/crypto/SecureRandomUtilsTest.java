package io.neow3j.crypto;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SecureRandomUtilsTest {

    @Test
    public void testSecureRandom() {
        SecureRandomUtils.secureRandom().nextInt();
    }

    @Test
    public void testIsNotAndroidRuntime() {
        assertFalse(SecureRandomUtils.isAndroidRuntime());
    }
}
