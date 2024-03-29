package io.neow3j.devpack.gradle;

import io.neow3j.compiler.DebugInfo;
import io.neow3j.protocol.ObjectMapperFactory;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Neow3jPluginUtils {

    protected static final String NEF_SUFFIX = ".nef";
    protected static final String NEFDBGNFO_SUFFIX = ".nefdbgnfo";
    protected static final String DEBUG_JSON_SUFFIX = ".debug.json";

    static List<File> getOutputDirs(Project project) {
        List<File> dirs = new ArrayList<>();
        getSourceSets(project).forEach(ss -> dirs.addAll(ss.getOutput().getClassesDirs().getFiles()));
        return dirs;
    }

    public static SourceSetContainer getSourceSets(Project project) {
        try {
            // First try with JavaPluginExtension (for Gradle 8+)
            JavaPluginExtension pluginExt = project.getExtensions().getByType(JavaPluginExtension.class);
            return pluginExt.getSourceSets();
        } catch (NoSuchMethodError | UnsupportedOperationException e) {
            // Fallback for Gradle 6 using JavaPluginConvention
            JavaPluginConvention pluginConv = project.getConvention().findPlugin(JavaPluginConvention.class);
            if (pluginConv != null) {
                return pluginConv.getSourceSets();
            }
        }
        throw new IllegalStateException("Gradle plugin 'java' missing. Make sure to apply it to your project.");
    }

    static void writeToFile(File file, byte[] content) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(content);
        }
    }

    /**
     * Generates a ZIP file containing a JSON file with the given debug information in it.
     *
     * @param debugInfo    the debug information to include in the ZIP file.
     * @param contractName the name of the contract the debug info belongs to.
     * @param outDir       the directory (absolute path) where the ZIP file should be stored.
     * @return the absolute path of the generated zip file.
     * @throws IOException if an error occurs when writing the file.
     */
    public static String writeDebugInfoZip(DebugInfo debugInfo, String contractName, Path outDir) throws IOException {
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
        try (ZipOutputStream s = new ZipOutputStream(Files.newOutputStream(zipOutputFile.toPath()))) {
            s.putNextEntry(new ZipEntry(zipEntryName));
            byte[] bytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(debugInfo);
            s.write(bytes);
            s.closeEntry();
        }
        return zipOutputFile.getAbsolutePath();
    }

}
