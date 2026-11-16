package io.neow3j.devpack.gradle;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.neow3j.devpack.gradle.Neow3jPlugin.TASK_NAME;
import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Neow3jPluginTest {

    @Test
    public void hasTaskNeow3jCompile() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("io.neow3j.gradle-plugin");

        assertTrue(project.getPluginManager().hasPlugin("io.neow3j.gradle-plugin"));

        assertNotNull(project.getTasks().getByName(TASK_NAME));
    }

    @Test
    public void tracksChangesToTheJavaCompilerOutputDirectory() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("io.neow3j.gradle-plugin");
        Neow3jCompileTask task = (Neow3jCompileTask) project.getTasks().getByName(TASK_NAME);
        JavaCompile compileJava = (JavaCompile) project.getTasks().getByName(COMPILE_JAVA_TASK_NAME);
        File destination = project.file("custom-classes");

        compileJava.getDestinationDirectory().set(destination);

        assertEquals(destination, task.getBuildDirectory().getAsFile().get());
    }

}
