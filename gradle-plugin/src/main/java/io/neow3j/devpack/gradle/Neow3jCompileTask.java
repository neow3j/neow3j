package io.neow3j.devpack.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

abstract public class Neow3jCompileTask extends DefaultTask {

    static final String CLASSNAME_NAME = "className";
    private static final String DEBUG_NAME = "debug";
    private static final String OUTPUTDIR_NAME = "outputDir";

    @Input
    @Option(option = CLASSNAME_NAME, description = "Sets the smart contract class name (fully qualified name) to be " +
            "compiled.")
    public abstract Property<String> getClassName();

    @Input
    @Optional
    @Option(option = DEBUG_NAME, description = "Sets whether the neow3j compiler should generate debugging symbols.")
    public abstract Property<Boolean> getDebug();

    @Input
    @Optional
    @Option(option = OUTPUTDIR_NAME, description = "Sets the output directory for the compiled smart contract.")
    public abstract RegularFileProperty getOutputDir();

    @TaskAction
    public void execute() {
        Neow3jCompileAction action = new Neow3jCompileAction();
        action.execute(this);
    }

}
