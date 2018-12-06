package io.neow3j.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;
import java.util.Optional;

public class SecurityProviderChecker {

    private static final String ANDROID_BC_CLASS_PREFIX = "com.android.org.bouncycastle";

    public static void addBouncyCastle() {
        if (isDefaultAndroidBC()) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        if (!isAnyBC()) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private static boolean isDefaultAndroidBC() {
        return getBCProvider()
                .map(SecurityProviderChecker::checkProviderStartsWith)
                .orElse(false);
    }

    private static Boolean checkProviderStartsWith(Provider provider) {
        return provider.getClass()
                .getCanonicalName()
                .startsWith(ANDROID_BC_CLASS_PREFIX);
    }

    private static boolean isAnyBC() {
        return getBCProvider().isPresent();
    }

    private static Optional<Provider> getBCProvider() {
        return Optional.ofNullable(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME));
    }

}
