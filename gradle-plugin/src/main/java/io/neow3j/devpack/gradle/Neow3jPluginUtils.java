package io.neow3j.devpack.gradle;

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

    static String getClassName(String fqClassName) {
        int firstChar;
        firstChar = fqClassName.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            fqClassName = fqClassName.substring(firstChar);
        }
        if (fqClassName.length() == 0) {
            return null;
        }
        return fqClassName;
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
