package com.axlabs.neow3j.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

public class SecurityProviderChecker {

    public static void addBouncyCastle() {
        if (isDefaultAndroidBC()) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        if (!isAnyBC()) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private static boolean isDefaultAndroidBC() {
        Provider provider = getBCProvider();
        return provider.getClass()
                .getCanonicalName()
                .startsWith("com.android.org.bouncycastle");
    }

    private static boolean isAnyBC() {
        return getBCProvider() != null;
    }

    private static Provider getBCProvider() {
        return Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

}
