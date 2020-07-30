package io.neow3j.devpack.gradle;

import io.neow3j.compiler.Compiler;
import java.io.IOException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GradleVersion;
import io.neow3j.compiler.*;

public class Neow3jPlugin implements Plugin<Project> {

    private static final String PLUGIN_ID = "io.neow3j.devpack.gradle-plugin";

    @Override
    public void apply(Project project) {

        if (GradleVersion.current().compareTo(GradleVersion.version("5.2")) < 0) {
            throw new UnsupportedOperationException(PLUGIN_ID + " requires at least Gradle 5.2");
        }

        Neow3jPluginOptions opts = project.getExtensions()
                .create("neow3jDevpack", Neow3jPluginOptions.class);

        project.task("neow3jCompile")
                .dependsOn(JavaCompile.class)
                .doLast(t -> {
                    Compiler n = new Compiler();
                    try {
                        n.compileClass(opts.getClassName());
                    } catch (IOException e) {
                        System.out.println("Compilation failed: " + e);
                    }
                });

        project.getPluginManager().apply(JavaLibraryPlugin.class);
    }

}
