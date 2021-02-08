package io.neow3j.devpack.gradle;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Neow3jPluginOptions {

    public static final String CLASSNAME_NAME = "className";

    private String className;

    // Default behavior: generate debug symbols
    private Boolean debug = true;

    private String outputDir;

    public Neow3jPluginOptions() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void className(String className) {
        this.className = className;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public void debug(Boolean debug) {
        setDebug(debug);
    }

    public Path getOutputDir() {
        if (this.outputDir != null) {
            return Paths.get(outputDir);
        }
        return null;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public void outputDir(String outputDir) {
        setOutputDir(outputDir);
    }
}
