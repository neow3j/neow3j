package io.neow3j.devpack.gradle;

import static java.util.Arrays.asList;

import java.util.LinkedHashSet;
import java.util.Set;

public class Neow3jPluginOptions {

    public static final String CLASSNAMES_NAME = "classNames";
    public static final String DEBUG_NAME = "debug";

    private LinkedHashSet<String> classNames;

    // Default behavior: generate debug symbols
    private Boolean debug = true;

    public Neow3jPluginOptions() {
    }

    public Set<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(String... classNames) {
        this.classNames = new LinkedHashSet<>(asList(classNames));
    }

    public void classNames(String... classNames) {
        setClassNames(classNames);
    }

    public void setClassName(String className) {
        setClassNames(className);
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
