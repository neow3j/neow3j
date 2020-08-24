package io.neow3j.devpack.gradle;

import static io.neow3j.devpack.gradle.Neow3jCompileTask.NEOW3J_COMPILE_TASK_NAME;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.util.GradleVersion;

public class Neow3jPlugin implements Plugin<Project> {

    private static final String PLUGIN_ID = "io.neow3j.devpack.gradle-plugin";
    private static final String GRADLE_MIN_VERSION = "5.2";

    @Override
    public void apply(Project project) {

        if (GradleVersion.current().compareTo(GradleVersion.version(GRADLE_MIN_VERSION)) < 0) {
            throw new UnsupportedOperationException(PLUGIN_ID +
                    " requires at least Gradle " + GRADLE_MIN_VERSION);
        }

        project.getTasks()
                .create(NEOW3J_COMPILE_TASK_NAME, Neow3jCompileTask.class);

    }

}
