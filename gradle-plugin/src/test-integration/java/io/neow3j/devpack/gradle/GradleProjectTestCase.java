package io.neow3j.devpack.gradle;

import static io.neow3j.contract.ContractUtils.getContractManifestFilename;
import static io.neow3j.devpack.gradle.Neow3jPlugin.TASK_NAME;
import static io.neow3j.devpack.gradle.Neow3jPlugin.DEFAULT_OUTPUT_DIR;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.NEFDBGNFO_SUFFIX;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.NEF_SUFFIX;
import static io.neow3j.devpack.gradle.TestCaseUtils.appendFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.rules.TemporaryFolder;

public class GradleProjectTestCase {

    public static final String SETTINGS_FILENAME = "settings.gradle";
    public static final String BUILD_FILENAME = "build.gradle";
    public static final String DEFAULT_BUILD_OUTPUT_DIR_NAME = "build";

    private TemporaryFolder projectRootDir;
    private String contractName;
    private String contractSourceFileName;
    private File settingsFile;
    private File buildFile;
    private File defaultBaseBuildOutputDir;
    private File buildNeow3jOutputDir;
    private File smartContractSourceFile;
    private File smartContractPackageDir;
    private List<String> gradleArguments;
    private BuildResult gradleBuildResult;
    private List<File> pluginClasspath;

    public GradleProjectTestCase(TemporaryFolder projectRootDir) throws IOException {
        this.projectRootDir = projectRootDir;

        this.settingsFile = this.projectRootDir.newFile(SETTINGS_FILENAME);
        this.buildFile = this.projectRootDir.newFile(BUILD_FILENAME);
        this.defaultBaseBuildOutputDir = this.projectRootDir
                .newFolder(DEFAULT_BUILD_OUTPUT_DIR_NAME);
        this.buildNeow3jOutputDir = Paths.get(defaultBaseBuildOutputDir.getAbsolutePath(),
                DEFAULT_OUTPUT_DIR).toFile();
        this.smartContractPackageDir = projectRootDir.newFolder("src", "main",
                "java", "io", "neow3j", "devpack", "gradle");
        this.smartContractPackageDir.mkdirs();

        appendFile(this.settingsFile, "rootProject.name = 'test-smart-contract'");

        InputStream pluginClasspathStream = getClass().getClassLoader()
                .getResourceAsStream("plugin-classpath.txt");
        if (pluginClasspathStream == null) {
            throw new IllegalStateException(
                    "Did not find plugin classpath resource, run `testClasses` build task.");
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(pluginClasspathStream, StandardCharsets.UTF_8));
        this.pluginClasspath = in.lines().map(File::new).collect(Collectors.toList());

        String pluginClasspathString = this.pluginClasspath.stream()
                .map(f -> f.getAbsolutePath().replace("\\", "\\\\"))
                .map(f -> "\"" + f + "\"")
                .collect(Collectors.joining(", "));

        String buildScript = "" +
                "buildscript {\n" +
                "    dependencies {" + "\n" +
                "        classpath files(" + pluginClasspathString + ")" + "\n" +
                "    }" + "\n" +
                "}" + "\n";
        appendFile(buildFile, buildScript);

        String plugins = "" +
                "plugins {" + "\n" +
                "    id 'java'" + "\n" +
                "    id 'io.neow3j.gradle-plugin'" + "\n" +
                "}\n";
        appendFile(buildFile, plugins);
    }

    protected static void addDependencies(File destination, String classPathFilesList)
            throws IOException {
        String content = "" +
                "dependencies {" + "\n" +
                "    compile files(" + classPathFilesList + ")" + "\n" +
                "}" + "\n";
        appendFile(destination, content);
    }

    public GradleProjectTestCase withContractSourceFileName(String contractSourceFileName)
            throws IOException {
        this.contractSourceFileName = contractSourceFileName;
        Path p = Paths.get(this.smartContractPackageDir.toString(),
                this.contractSourceFileName);
        this.smartContractSourceFile = Files.createFile(p).toFile();
        appendFile(this.smartContractSourceFile, getSmartContractSourceInputStream());
        return this;
    }

    public GradleProjectTestCase withContractName(String contractName) {
        this.contractName = contractName;
        return this;
    }

    public GradleProjectTestCase withDefaultDependencies() throws IOException {
        // just filter the devpack dependency jar
        String depClasspathString = this.pluginClasspath.stream()
                .filter(f -> f.getAbsolutePath().contains("devpack-"))
                .map(f -> f.getAbsolutePath().replace("\\", "\\\\"))
                .map(f -> "\"" + f + "\"")
                .collect(Collectors.joining(", "));
        addDependencies(this.buildFile, depClasspathString);
        return this;
    }

    public GradleProjectTestCase appendToBuildFile(String contentString) throws IOException {
        appendFile(this.buildFile, contentString);
        return this;
    }

    public GradleProjectTestCase withGradleArgument(String arg) {
        if (this.gradleArguments == null) {
            this.gradleArguments = new ArrayList<>();
        }
        this.gradleArguments.add(arg);
        return this;
    }

    public GradleProjectTestCase runBuild() {
        this.withGradleArgument(TASK_NAME);
        try {
            this.gradleBuildResult = GradleRunner.create()
                    .withProjectDir(this.projectRootDir.getRoot())
                    .withPluginClasspath()
                    .withArguments(this.gradleArguments)
                    .build();
        } catch (UnexpectedBuildFailure e) {
            this.gradleBuildResult = e.getBuildResult();
        }
        return this;
    }

    public GradleProjectTestCase withExpectedNeow3jBuildOutputDir(File outputDir) {
        this.buildNeow3jOutputDir = outputDir;
        return this;
    }

    public BuildResult getGradleBuildResult() {
        return this.gradleBuildResult;
    }

    public String getExpectedDebugFileName() {
        return this.contractName + NEFDBGNFO_SUFFIX;
    }

    public File getExpectedDebugFile() {
        return Paths.get(this.buildNeow3jOutputDir.getAbsolutePath(),
                this.getExpectedDebugFileName()).toFile();
    }

    public String getExpectedManifestFileName() {
        return getContractManifestFilename(this.contractName);
    }

    public File getExpectedManifestFile() {
        return Paths.get(this.buildNeow3jOutputDir.getAbsolutePath(),
                this.getExpectedManifestFileName()).toFile();
    }

    public String getExpectedNefFileName() {
        return this.contractName + NEF_SUFFIX;
    }

    public File getExpectedNefFile() {
        return Paths.get(this.buildNeow3jOutputDir.getAbsolutePath(),
                this.getExpectedNefFileName()).toFile();
    }

    public String getContractSourceFileName() {
        return this.contractSourceFileName;
    }

    public InputStream getSmartContractSourceInputStream() {
        return GradleProjectTestCase.class
                .getResourceAsStream("/" + this.contractSourceFileName);
    }

    public TemporaryFolder getProjectRootDir() {
        return projectRootDir;
    }

    public String getContractName() {
        return contractName;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public File getBuildFile() {
        return buildFile;
    }

    public File getDefaultBaseBuildOutputDir() {
        return defaultBaseBuildOutputDir;
    }

    public File getBuildNeow3jOutputDir() {
        return buildNeow3jOutputDir;
    }

    public File getSmartContractSourceFile() {
        return smartContractSourceFile;
    }

    public File getSmartContractPackageDir() {
        return smartContractPackageDir;
    }

    public List<File> getPluginClasspath() {
        return pluginClasspath;
    }
}
