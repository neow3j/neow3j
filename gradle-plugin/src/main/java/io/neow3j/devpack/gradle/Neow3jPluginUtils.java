package io.neow3j.devpack.gradle;

import static io.neow3j.utils.ClassUtils.getClassName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;

public class Neow3jPluginUtils {

    protected static final String SUFFIX_FILENAME = ".nef";
    protected static final String DEFAULT_FILENAME = "output" + SUFFIX_FILENAME;

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

}
