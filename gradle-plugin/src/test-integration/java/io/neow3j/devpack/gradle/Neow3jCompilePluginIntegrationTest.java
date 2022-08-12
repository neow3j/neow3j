package io.neow3j.devpack.gradle;

import java.io.File;
import java.io.IOException;

import org.gradle.testkit.runner.BuildResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static io.neow3j.devpack.gradle.Neow3jPlugin.TASK_NAME;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Neow3jCompilePluginIntegrationTest {

    @Rule
    public final TemporaryFolder projectRootDir = new TemporaryFolder();

    @Test
    public void testTaskHappyPath() throws IOException {
        String buildFileContent = "" +
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
    public void testTaskHappyPath_wrongJavaTargetCompatibility() throws IOException {
        String buildFileContent = "" +
                "sourceCompatibility = JavaVersion.VERSION_1_7" + "\n" +
                "targetCompatibility = JavaVersion.VERSION_1_7" + "\n" +
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
        assertFalse(testCase.getBuildNeow3jOutputDir().exists());
    }

    @Test
    public void testTaskWithDebugSetToFalse() throws IOException {
        String buildFileContent = "" +
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
        assertFalse(testCase.getBuildNeow3jOutputDir().exists());
    }

    @Test
    public void testTaskCompilationError_className_notFound() throws IOException {
        String buildFileContent = "" +
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
        assertFalse(testCase.getBuildNeow3jOutputDir().exists());
    }

    @Test
    public void testTaskHappyPath_DisplayName() throws IOException {
        String buildFileContent = "" +
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
        assertFalse(testCase.getBuildNeow3jOutputDir().exists());
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
        assertFalse(testCase.getBuildNeow3jOutputDir().exists());
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

        File newOutputDir = this.projectRootDir.newFolder("someDir");

        String buildFileContent = "" +
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
