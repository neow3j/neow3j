package io.neow3j.devpack.gradle;

import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

abstract public class Neow3jCompileTask extends DefaultTask {

    public static final String NEOW3J_COMPILE_TASK_NAME = "neow3jCompile";
    public static final String NEOW3J_COMPILER_OPTIONS_NAME = "neow3jCompiler";
    public static final String NEOW3J_DEFAULT_OUTPUT_DIR = "neow3j";

    @Input
    abstract public Property<String> getClassName();

    @Input
    @Optional
    abstract public Property<Boolean> getDebug();

    @Input
    @Optional
    abstract public Property<Path> getOutputDir();

    @Input
    @Optional
    abstract public Property<Path> getProjectBuildDir();

    @TaskAction
    public void execute() {
        Neow3jCompileAction action = new Neow3jCompileAction();
        action.execute(this);
    }

}
