package io.neow3j.devpack.gradle;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;

@SuppressWarnings("unchecked")
public class Neow3jCompileTask extends DefaultTask {

    public static final String NEOW3J_COMPILE_TASK_NAME = "neow3jCompile";
    public static final String NEOW3J_COMPILER_OPTIONS_NAME = "neow3jCompiler";
    public static final String NEOW3J_DEFAULT_OUTPUT_DIR = "neow3j";

    private Neow3jPluginOptions options;
    private File projectBuildDir;
    private Path compilerDefaultOutputDir;

    public Neow3jCompileTask() {
        this.options = getProject().getExtensions()
                .create(NEOW3J_COMPILER_OPTIONS_NAME, Neow3jPluginOptions.class);
        this.projectBuildDir = this.getProject().getBuildDir();
        this.compilerDefaultOutputDir = Paths.get(projectBuildDir.getAbsolutePath(), NEOW3J_DEFAULT_OUTPUT_DIR);
        this.getProject().getPluginManager().apply(JavaLibraryPlugin.class);
        this.dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        Action action = new Neow3jCompileAction();
        this.doLast(action);
    }

    public Neow3jPluginOptions getOptions() {
        return this.options;
    }

    public File getProjectBuildDir() {
        return projectBuildDir;
    }

    public Path getCompilerDefaultOutputDir() {
        return this.compilerDefaultOutputDir;
    }

}
