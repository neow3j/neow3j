package io.neow3j.devpack.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import static io.neow3j.devpack.gradle.Neow3jPlugin.TASK_NAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Neow3jPluginTest {

    @Test
    public void hasTaskNeow3jCompile() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("io.neow3j.gradle-plugin");

        assertTrue(project.getPluginManager().hasPlugin("io.neow3j.gradle-plugin"));

        assertNotNull(project.getTasks().getByName(TASK_NAME));
    }

}