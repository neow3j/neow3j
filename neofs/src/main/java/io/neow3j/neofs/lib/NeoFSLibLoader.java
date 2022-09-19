package io.neow3j.neofs.lib;

import com.sun.jna.Native;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getArchNameForDarwin;
import static io.neow3j.neofs.lib.NeoFSLibUtils.getArchNameForLinux;
import static io.neow3j.neofs.lib.NeoFSLibUtils.getArchNameForWindows;

public class NeoFSLibLoader {

    public static NeoFSLibInterface load() throws Exception {
        Path libPath = getLibPath();
        return Native.load(libPath.toAbsolutePath().toString(), NeoFSLibInterface.class);
    }

    private static Path getLibPath() throws Exception {
        String arch = System.getProperty("os.arch").toLowerCase();
        String osName = System.getProperty("os.name").toLowerCase();
        String osVersion = System.getProperty("os.version").toLowerCase();
        String dataModel = System.getProperty("sun.arch.data.model").toLowerCase();

        System.out.println("# OS Architecture: " + arch);
        System.out.println("# OS Name: " + osName);
        System.out.println("# OS Version: " + osVersion);
        System.out.println("# Data Model: " + dataModel);

        Optional<String> platformName = Optional.empty();
        Optional<String> archName = Optional.empty();

        if (osName.contains("mac os")) {
            platformName = Optional.of("darwin");
            archName = getArchNameForDarwin(arch);
        } else if (osName.contains("windows")) {
            platformName = Optional.of("windows");
            archName = getArchNameForWindows(arch);
        } else if (osName.contains("linux")) {
            platformName = Optional.of("linux");
            archName = getArchNameForLinux(arch);
        }

        if (!platformName.isPresent()) {
            System.out.printf("You're running on '%s' but this platform is not supported.", osName);
            System.exit(1);
        }

        if (!archName.isPresent()) {
            System.out.printf("You're running on '%s' but this arch is not supported.", arch);
            System.exit(1);
        }

        String libFileName = String.format("libneofs-%s-%s.so", platformName.get(), archName.get());

        URL url = NeoFSLibLoader.class.getClassLoader().getResource("libs");
        return Paths.get(Paths.get(url.toURI()).toString(), libFileName);
    }

}
