package io.neow3j.devpack.gradle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class Neow3jPluginTest {

    @Test
    public void hasTaskNeow3jCompile(){
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("io.neow3j.gradle-plugin");

        assertTrue(project.getPluginManager()
                .hasPlugin("io.neow3j.gradle-plugin"));

        assertNotNull(project.getTasks().getByName("neow3jCompile"));
    }

}