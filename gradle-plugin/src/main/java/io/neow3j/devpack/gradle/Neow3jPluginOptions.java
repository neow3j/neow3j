package io.neow3j.devpack.gradle;

public class Neow3jPluginOptions {

    private String className;

    public Neow3jPluginOptions() {
    }

    public Neow3jPluginOptions(String path) {
        this.className = path;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
