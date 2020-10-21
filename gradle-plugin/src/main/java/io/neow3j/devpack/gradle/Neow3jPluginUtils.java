package io.neow3j.devpack.gradle;

import static io.neow3j.utils.ClassUtils.getClassName;

import io.neow3j.compiler.DebugInfo;
import io.neow3j.protocol.ObjectMapperFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;

public class Neow3jPluginUtils {

    protected static final String SUFFIX_FILENAME = ".nef";
    protected static final String DEFAULT_FILENAME = "output" + SUFFIX_FILENAME;
    protected static final String NEFDBGNFO_SUFFIX = ".nefdbgnfo";
    protected static final String DEBUG_JSON_SUFFIX = ".debug.json";

    static String getCompileOutputFileName(String fqClassName) {
        String className = getClassName(fqClassName);
        if (className != null && className.length() > 0) {
            return className + SUFFIX_FILENAME;
        }
        return DEFAULT_FILENAME;
    }

    static URL getBuildDirURL(File buildDir) {
        try {
            return buildDir.toURI().toURL();
        } catch (MalformedURLException e) {
            System.out.println("Error on converting ("
                    + buildDir.getAbsolutePath() + ") to URL: " + e);
        }
        return null;
    }

    static List<URL> getSourceSetsDirsURL(Project project) {
        final JavaPluginConvention pluginConv = project.getConvention()
                .getPlugin(JavaPluginConvention.class);
        List<URL> urls = new ArrayList<>();
        pluginConv.getSourceSets().forEach(ss -> {
            ss.getOutput().getClassesDirs().forEach(f -> {
                try {
                    urls.add(f.toURI().toURL());
                } catch (Exception e) {
                    System.out.println("Error on converting ("
                            + f.getAbsolutePath() + ") to URL: " + e);
                }
            });
        });
        return urls;
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
     * @param debugInfo The debug information to include in the ZIP file.
     * @param outDirString The directory (absolute path) where the ZIP file should be stored.
     * @param fileName The desired name of the ZIP file and the included JSON file. This
     *                 must not include the file name ending. The ending is added accordingly to
     *                 what the Neo Debugger expects.
     * @return the absolute path of the generated zip file.
     * @throws IOException If an error occurs when writing the file.
     */
    public static String generateDebugInfoZip(DebugInfo debugInfo, String outDirString,
            String fileName) throws IOException {

        File tmpFile = File.createTempFile("contract", ".debug.json");
        tmpFile.deleteOnExit();
        // Write the debug JSON to a temporary file.
        try (FileOutputStream s = new FileOutputStream(tmpFile)) {
            ObjectMapperFactory.getObjectMapper().writeValue(s, debugInfo);
        }
        // Then put it into a ZIP archive.
        String zipName = fileName + NEFDBGNFO_SUFFIX;
        String zipEntryName = fileName + DEBUG_JSON_SUFFIX;
        File zipOutputFile = Paths.get(outDirString, zipName).toFile();
        try (ZipOutputStream s = new ZipOutputStream(new FileOutputStream(zipOutputFile))) {
            s.putNextEntry(new ZipEntry(zipEntryName));
            byte[] bytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(debugInfo);
            s.write(bytes);
            s.closeEntry();
        }
        return zipOutputFile.getAbsolutePath();
    }

}
