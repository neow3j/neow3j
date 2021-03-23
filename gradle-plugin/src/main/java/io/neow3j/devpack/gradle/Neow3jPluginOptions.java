package io.neow3j.devpack.gradle;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.options.Option;

abstract public class Neow3jPluginOptions {

    public static final String CLASSNAME_NAME = "className";
    public static final String DEBUG_NAME = "debug";
    public static final String OUTPUTDIR_NAME = "outputDir";

    @Option(option = CLASSNAME_NAME, description = "Sets the smart contract class name (fully "
            + "qualified name) to be compiled.")
    abstract public Property<String> getClassName();

    @Option(option = DEBUG_NAME, description = "Sets whether the neow3j compiler should generate "
            + "debugging symbols.")
    abstract public Property<Boolean> getDebug();

    @Option(option = OUTPUTDIR_NAME, description = "Sets the output directory for the compiled "
            + "smart contract.")
    abstract public Property<String> getOutputDir();

}
