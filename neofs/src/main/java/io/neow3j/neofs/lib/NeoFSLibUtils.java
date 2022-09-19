package io.neow3j.neofs.lib;

import io.neow3j.neofs.lib.responses.PointerResponse;

import java.util.Optional;

public class NeoFSLibUtils {

    public static byte[] getResponseBytes(PointerResponse response) {
        return response.value.getByteArray(0, response.length);
    }

    public static Optional<String> getArchNameForDarwin(String arch) {
        // Apple M1 case
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return Optional.of("arm64");
            // Apple Intel case
        } else if (arch.contains("amd64") || arch.contains("x86_64")) {
            return Optional.of("amd64");
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> getArchNameForLinux(String arch) {
        // TODO: linux/armv5, linux/armv6, linux/armv7, linux/armv8
        if (arch.contains("arm64")) {
            return Optional.of("arm64");
        } else if (arch.contains("amd64")) {
            return Optional.of("amd64");
        } else if (arch.contains("386")) {
            return Optional.of("i386");
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> getArchNameForWindows(String arch) {
        if (arch.contains("386")) {
            return Optional.of("386");
        } else if (arch.contains("amd64")) {
            return Optional.of("amd64");
        } else {
            return Optional.empty();
        }
    }

}
