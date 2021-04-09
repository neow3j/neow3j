package io.neow3j.devpack.gradle;

import io.neow3j.compiler.DebugInfo;
import io.neow3j.protocol.ObjectMapperFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;

import static java.util.Optional.ofNullable;

public class Neow3jPluginUtils {

    protected static final String NEF_SUFFIX = ".nef";
    protected static final String DEFAULT_FILENAME = "output" + NEF_SUFFIX;
    protected static final String NEFDBGNFO_SUFFIX = ".nefdbgnfo";
    protected static final String DEBUG_JSON_SUFFIX = ".debug.json";

    static List<File> getOutputDirs(Project project) {
        final JavaPluginConvention pluginConv = project.getConvention()
                .getPlugin(JavaPluginConvention.class);
        List<File> dirs = new ArrayList<>();
        pluginConv.getSourceSets()
                .forEach(ss -> dirs.addAll(ss.getOutput().getClassesDirs().getFiles()));
        return dirs;
    }

    static void writeToFile(File file, byte[] content) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        try {
            outputStream.write(content);
        } finally {
            outputStream.close();
        }
    }

    /**
     * Generates a ZIP file containing a JSON file with the given debug information in it.
     *
     * @param debugInfo    The debug information to include in the ZIP file.
     * @param contractName The name of the contract the debug info belongs to.
     * @param outDir       The directory (absolute path) where the ZIP file should be stored.
     * @return the absolute path of the generated zip file.
     * @throws IOException If an error occurs when writing the file.
     */
    public static String writeDebugInfoZip(DebugInfo debugInfo, String contractName, Path outDir)
            throws IOException {

        // Write the debug JSON to a temporary file.
        File tmpFile = File.createTempFile("contract", DEBUG_JSON_SUFFIX);
        tmpFile.deleteOnExit();
        try (FileOutputStream s = new FileOutputStream(tmpFile)) {
            ObjectMapperFactory.getObjectMapper().writeValue(s, debugInfo);
        }
        // Then put it into a ZIP archive.
        String zipName = contractName + NEFDBGNFO_SUFFIX;
        String zipEntryName = contractName + DEBUG_JSON_SUFFIX;
        File zipOutputFile = Paths.get(outDir.toString(), zipName).toFile();
        try (ZipOutputStream s = new ZipOutputStream(new FileOutputStream(zipOutputFile))) {
            s.putNextEntry(new ZipEntry(zipEntryName));
            byte[] bytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(debugInfo);
            s.write(bytes);
            s.closeEntry();
        }
        return zipOutputFile.getAbsolutePath();
    }

}
