package io.neow3j.devpack.gradle;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

abstract public class Neow3jPluginExtension {

    public abstract Property<String> getClassName();

    public abstract Property<Boolean> getDebug();

    public abstract RegularFileProperty getOutputDir();

}
