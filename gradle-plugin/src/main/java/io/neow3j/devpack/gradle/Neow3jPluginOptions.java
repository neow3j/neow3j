package io.neow3j.devpack.gradle;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

public class Neow3jPluginOptions {

    public static final String CLASSNAMES_NAME = "classNames";
    public static final String DEBUG_NAME = "debug";

    private Set<String> classNames;

    // Default behavior: generate debug symbols
    private Boolean debug = true;

    public Neow3jPluginOptions() {
    }

    public Neow3jPluginOptions(Set<String> classNames) {
        this.classNames = classNames;
    }

    public Set<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(String... classNames) {
        this.classNames = new HashSet<>(asList(classNames));
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

}
