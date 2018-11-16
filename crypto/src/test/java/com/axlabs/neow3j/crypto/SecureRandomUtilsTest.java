package com.axlabs.neow3j.crypto;

import org.junit.Test;

import static com.axlabs.neow3j.crypto.SecureRandomUtils.isAndroidRuntime;
import static com.axlabs.neow3j.crypto.SecureRandomUtils.secureRandom;
import static org.junit.Assert.assertFalse;

public class SecureRandomUtilsTest {

    @Test
    public void testSecureRandom() {
        secureRandom().nextInt();
    }

    @Test
    public void testIsNotAndroidRuntime() {
        assertFalse(isAndroidRuntime());
    }
}
