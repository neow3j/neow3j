package io.neow3j.neofs.lib;

import com.sun.jna.Native;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static io.neow3j.neofs.lib.NeoFSLibUtils.getArchNameForDarwin;
import static io.neow3j.neofs.lib.NeoFSLibUtils.getArchNameForLinux;
import static io.neow3j.neofs.lib.NeoFSLibUtils.getArchNameForWindows;

public class NeoFSLibLoader {

    private static final String NEOFS_SHARED_LIB_VERSION = "0.0.10";

    private static final String TEMPORARY_FILE_EXT = ".tmp";

    public static NeoFSLibInterface load() throws Exception {
        Path libPath = getLibPath(true);
        return Native.load(libPath.toAbsolutePath().toString(), NeoFSLibInterface.class);
    }

    private static Path getLibPath(boolean print) throws Exception {
        String arch = System.getProperty("os.arch").toLowerCase();
        String osName = System.getProperty("os.name").toLowerCase();
        String osVersion = System.getProperty("os.version").toLowerCase();
        String dataModel = System.getProperty("sun.arch.data.model").toLowerCase();

        if (print) {
            System.out.println("# Native Library Version: " + NEOFS_SHARED_LIB_VERSION);
            System.out.println("# OS Architecture:        " + arch);
            System.out.println("# OS Name:                " + osName);
            System.out.println("# OS Version:             " + osVersion);
            System.out.println("# Data Model:             " + dataModel);
        }

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

        // Method .getResourceAsStream() must be used here since that it's not possible
        // to find files within JARs with .getResource()
        InputStream stream = NeoFSLibLoader.class.getClassLoader().getResourceAsStream("libs/" + libFileName);

        File nativeLibTempFile = File.createTempFile(libFileName, TEMPORARY_FILE_EXT);
        nativeLibTempFile.deleteOnExit();
        nativeLibTempFile.setWritable(true);
        nativeLibTempFile.setExecutable(true);

        Path nativeLibTempFilePath = Paths.get(nativeLibTempFile.toURI());
        Files.copy(stream, nativeLibTempFilePath, StandardCopyOption.REPLACE_EXISTING);

        return nativeLibTempFilePath;
    }

}
