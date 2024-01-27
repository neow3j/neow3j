package io.neow3j.devpack.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.neow3j.devpack.gradle.Neow3jPlugin.TASK_NAME;
import static java.util.Objects.requireNonNull;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Neow3jCompilePluginIntegrationTest {

    @TempDir
    public Path projectRootDir;

    @Test
    public void testTaskCompilationCache() throws IOException {
        String buildFileContent = "" +
              "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
              "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
              "\n" +
              "neow3jCompiler {" + "\n" +
              "    className=" + "\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
              "    cacheable=false\n" +
              "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
              .withDefaultDependencies()
              .appendToBuildFile(buildFileContent)
              .withContractName("ContractTest")
              .withContractSourceFileName("ContractTest.java")
              .runBuild();

        BuildResult firstBuildResult = testCase.getGradleBuildResult();

        assertEquals(SUCCESS, firstBuildResult.task(":" + TASK_NAME).getOutcome());

        // run for the second time to assert cache was not used
        testCase.runBuild();
        BuildResult buildResult = testCase.getGradleBuildResult();
        assertNotEquals(FROM_CACHE, buildResult.task(":" + TASK_NAME).getOutcome());
        assertEquals(UP_TO_DATE, buildResult.task(":" + TASK_NAME).getOutcome());
    }

    @Test
    public void testTaskHappyPath() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=" + "\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .withContractSourceFileName("ContractTest.java")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // build success/failure check
        assertEquals(SUCCESS, buildResult.task(":" + TASK_NAME).getOutcome());
        assertTrue(testCase.getBuildNeow3jOutputDir().exists());

        // check whether NEF file was generated
        assertTrue(testCase.getExpectedNefFile().exists());
        assertThat(testCase.getExpectedNefFile().length(), greaterThan(0L));

        // check whether manifest file was generated
        assertTrue(testCase.getExpectedManifestFile().exists());
        assertThat(testCase.getExpectedManifestFile().length(), greaterThan(0L));

        // check whether debug file was generated
        assertTrue(testCase.getExpectedDebugFile().exists());
        assertThat(testCase.getExpectedDebugFile().length(), greaterThan(0L));
    }

    @Test
    public void testTaskHappyPath_wrongJavaTargetCompatibility_lower() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_7" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_7" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=" + "\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .withContractSourceFileName("ContractTest.java")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // build success/failure check
        assertEquals(FAILED, buildResult.task(":" + TASK_NAME).getOutcome());
        assertTrue(testCase.getBuildNeow3jOutputDir().exists());
        assertEquals(0, requireNonNull(testCase.getBuildNeow3jOutputDir().listFiles()).length);
    }

    @Test
    public void testTaskHappyPath_wrongJavaTargetCompatibility_higher() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_10" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_10" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=" + "\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .withContractSourceFileName("ContractTest.java")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // build success/failure check

        // The particularity of this test is that it fails on the compilation of the Java code
        // if the targetCompatibility is higher than the Java version used to compile the code.
        // Then, in this case we need to first test if the compilation succeeded and then if the
        // neow3jCompile task failed, consequently.

        if (buildResult.task(":" + "compileJava").getOutcome() == SUCCESS) {
            assertEquals(FAILED, buildResult.task(":" + TASK_NAME).getOutcome());
            assertTrue(testCase.getBuildNeow3jOutputDir().exists());
            assertEquals(0, requireNonNull(testCase.getBuildNeow3jOutputDir().listFiles()).length);
        } else {
            assertEquals(FAILED, buildResult.task(":" + "compileJava").getOutcome());
        }
    }

    @Test
    public void testTaskWithDebugSetToFalse() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=" + "\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
                "    debug=false" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .withContractSourceFileName("ContractTest.java")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // build success/failure check
        assertEquals(SUCCESS, buildResult.task(":" + TASK_NAME).getOutcome());
        assertTrue(testCase.getBuildNeow3jOutputDir().exists());

        // check whether NEF file was generated
        assertTrue(testCase.getExpectedNefFile().exists());
        assertThat(testCase.getExpectedNefFile().length(), greaterThan(0L));

        // check whether manifest file was generated
        assertTrue(testCase.getExpectedManifestFile().exists());
        assertThat(testCase.getExpectedManifestFile().length(), greaterThan(0L));

        // check whether debug file was generated
        assertFalse(testCase.getExpectedDebugFile().exists());
    }

    @Test
    public void testTaskWithoutClassName() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .withContractSourceFileName("ContractTest.java")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // check whether the compilation miserably failed :-)
        assertEquals(FAILED, buildResult.task(":" + TASK_NAME).getOutcome());
        // Here the output dir doesn't exist yet because of a missing option.
        assertFalse(testCase.getBuildNeow3jOutputDir().exists());
    }

    @Test
    public void testTaskCompilationError_className_notFound() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=" + "\"io.neow3j.devpack.gradle.ShouldNeverExist\"" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // check whether the compilation miserably failed :-)
        assertEquals(FAILED, buildResult.task(":" + TASK_NAME).getOutcome());
        assertTrue(testCase.getBuildNeow3jOutputDir().exists());
        assertEquals(0, requireNonNull(testCase.getBuildNeow3jOutputDir().listFiles()).length);
    }

    @Test
    public void testTaskHappyPath_DisplayName() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=" + "\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("Contract-Test")
                .withContractSourceFileName("ContractTestWithDisplayName.java")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // build success/failure check
        assertEquals(SUCCESS, buildResult.task(":" + TASK_NAME).getOutcome());
        assertTrue(testCase.getBuildNeow3jOutputDir().exists());

        // check whether NEF file was generated
        assertTrue(testCase.getExpectedNefFile().exists());
        assertThat(testCase.getExpectedNefFile().length(), greaterThan(0L));

        // check whether manifest file was generated
        assertTrue(testCase.getExpectedManifestFile().exists());
        assertThat(testCase.getExpectedManifestFile().length(), greaterThan(0L));

        // check whether debug file was generated
        assertTrue(testCase.getExpectedDebugFile().exists());
        assertThat(testCase.getExpectedDebugFile().length(), greaterThan(0L));
    }

    @Test
    public void testTaskCompilationError_SmartContract_NotValid() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=" + "\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .withContractSourceFileName("ContractTestWithError.java")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // check whether the compilation miserably failed :-)
        assertEquals(FAILED, buildResult.task(":" + TASK_NAME).getOutcome());
        assertTrue(testCase.getBuildNeow3jOutputDir().exists());
        assertEquals(0, requireNonNull(testCase.getBuildNeow3jOutputDir().listFiles()).length);
        assertThat(
                buildResult.getOutput(),
                not(
                        containsString("Caused by: io.neow3j.compiler.CompilerException")
                )
        );
    }

    @Test
    public void testTaskCompilationError_SmartContract_NotValid_With_StackTrace()
            throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=" + "\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .withContractSourceFileName("ContractTestWithError.java")
                .withGradleArgument("--stacktrace")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // check whether the compilation miserably failed :-)
        assertEquals(FAILED, buildResult.task(":" + TASK_NAME).getOutcome());
        assertTrue(testCase.getBuildNeow3jOutputDir().exists());
        assertEquals(0, requireNonNull(testCase.getBuildNeow3jOutputDir().listFiles()).length);
        assertThat(
                buildResult.getOutput(),
                containsString(
                        "Caused by: io.neow3j.compiler.CompilerException"
                )
        );
    }

    @Test
    public void testTaskHappyPath_BuildOutputDir()
            throws IOException {

        File newOutputDir = Files.createDirectory(this.projectRootDir.resolve("someDir")).toFile();

        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_8" + "\n" +
                "\n" +
                "neow3jCompiler {" + "\n" +
                "    className=\"io.neow3j.devpack.gradle.ContractTest\"" + "\n" +
                "    outputDir=file(\"" + newOutputDir.getAbsolutePath() + "\")" + "\n" +
                "}" + "\n";

        GradleProjectTestCase testCase = new GradleProjectTestCase(this.projectRootDir)
                .withExpectedNeow3jBuildOutputDir(newOutputDir)
                .withDefaultDependencies()
                .appendToBuildFile(buildFileContent)
                .withContractName("ContractTest")
                .withContractSourceFileName("ContractTest.java")
                .withGradleArgument("--stacktrace")
                .runBuild();

        BuildResult buildResult = testCase.getGradleBuildResult();

        // build success/failure check
        assertEquals(SUCCESS, buildResult.task(":" + TASK_NAME).getOutcome());
        assertTrue(testCase.getBuildNeow3jOutputDir().exists());
        assertEquals(testCase.getBuildNeow3jOutputDir(), newOutputDir);

        // check whether NEF file was generated
        assertTrue(testCase.getExpectedNefFile().exists());
        assertThat(testCase.getExpectedNefFile().length(), greaterThan(0L));

        // check whether manifest file was generated
        assertTrue(testCase.getExpectedManifestFile().exists());
        assertThat(testCase.getExpectedManifestFile().length(), greaterThan(0L));

        // check whether debug file was generated
        assertTrue(testCase.getExpectedDebugFile().exists());
        assertThat(testCase.getExpectedDebugFile().length(), greaterThan(0L));
    }

}
