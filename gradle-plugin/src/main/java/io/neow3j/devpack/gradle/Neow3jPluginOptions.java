package io.neow3j.devpack.gradle;

import static java.util.Arrays.asList;

import java.util.LinkedHashSet;
import java.util.Set;

public class Neow3jPluginOptions {

    public static final String CLASSNAME_NAME = "className";

    private String className;

    // Default behavior: generate debug symbols
    private Boolean debug = true;

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

}
