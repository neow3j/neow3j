package io.neow3j.devpack.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Neow3jPlugin implements Plugin<Project> {

    private static final String PLUGIN_ID = "io.neow3j.devpack.gradle-plugin";
    static final String TASK_NAME = "neow3jCompile";
    private static final String GRADLE_MIN_VERSION = "6.0";
    static final String EXTENSION_NAME = "neow3jCompiler";
    static final String DEFAULT_OUTPUT_DIR = "neow3j";

    @Override
    public void apply(@NotNull Project project) {

        if (GradleVersion.current().compareTo(GradleVersion.version(GRADLE_MIN_VERSION)) < 0) {
            throw new UnsupportedOperationException(PLUGIN_ID + " requires at least Gradle " + GRADLE_MIN_VERSION);
        }

        Neow3jPluginExtension extension = project.getExtensions().create(EXTENSION_NAME, Neow3jPluginExtension.class);
        // Get the build directory from the project's layout.
        DirectoryProperty buildDir = project.getLayout().getBuildDirectory();
        // Set default values that will be overwritten by the values set in dev's build.gradle.
        extension.getDebug().set(true);
        extension.getOutputDir().set(new File(buildDir.getAsFile().get(), DEFAULT_OUTPUT_DIR));
        extension.getCacheable().set(false);

        project.getTasks().create(TASK_NAME, Neow3jCompileTask.class, task -> {
            task.getCacheable().set(extension.getCacheable());
            task.getOutputs().cacheIf(value -> extension.getCacheable().get());
            task.getClassName().set(extension.getClassName());
            task.getDebug().set(extension.getDebug());
            task.getOutputDir().set(extension.getOutputDir());
            task.getProject().getPluginManager().apply(JavaLibraryPlugin.class);
            task.dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        });
    }

}
