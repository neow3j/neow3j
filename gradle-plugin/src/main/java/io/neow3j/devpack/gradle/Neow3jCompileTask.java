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

    private Neow3jPluginOptions options;
    private File projectBuildDir;
    private Path compilerOutputDir;

    public Neow3jCompileTask() {
        this.options = getProject().getExtensions()
                .create(NEOW3J_COMPILER_OPTIONS_NAME, Neow3jPluginOptions.class);
        this.projectBuildDir = this.getProject().getBuildDir();
        this.compilerOutputDir = Paths.get(projectBuildDir.getAbsolutePath(), "neow3j");
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

    public Path getCompilerOutputDir() {
        return compilerOutputDir;
    }

}
