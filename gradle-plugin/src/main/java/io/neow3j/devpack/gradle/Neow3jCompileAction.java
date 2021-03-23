package io.neow3j.devpack.gradle;

import static io.neow3j.contract.ContractUtils.writeContractManifestFile;
import static io.neow3j.contract.ContractUtils.writeNefFile;
import static io.neow3j.devpack.gradle.Neow3jCompileTask.NEOW3J_COMPILER_OPTIONS_NAME;
import static io.neow3j.devpack.gradle.Neow3jCompileTask.NEOW3J_DEFAULT_OUTPUT_DIR;
import static io.neow3j.devpack.gradle.Neow3jPluginOptions.CLASSNAME_NAME;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getBuildDirURL;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getSourceSetsDirsURL;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getSourceSetsFilesURL;
import static java.nio.file.Files.createDirectories;
import static java.util.Optional.ofNullable;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.logging.configuration.ShowStacktrace;

public class Neow3jCompileAction implements Action<Neow3jCompileTask> {

    @Override
    public void execute(Neow3jCompileTask neow3jPluginCompile) {
        String canonicalClassName = ofNullable(neow3jPluginCompile.getClassName().getOrNull())
                .orElseThrow(() ->
                        new IllegalArgumentException("The parameter "
                                + "'" + CLASSNAME_NAME + "' needs to be set in the "
                                + "'" + NEOW3J_COMPILER_OPTIONS_NAME + "' "
                                + "declaration in your build.gradle file.")
                );

        // default value is 'true' for generating debug symbols if not specified
        Boolean debugSymbols = neow3jPluginCompile.getDebug().getOrElse(true);

        File projectBuildDir = ofNullable(
                neow3jPluginCompile.getProjectBuildDir()
                        .map(Path::toFile).getOrNull())
                .orElseThrow(() ->
                        new IllegalArgumentException("The project build directory needs "
                                + "to be set to the task."));

        Path outputDir = neow3jPluginCompile.getOutputDir().getOrElse(
                Paths.get(projectBuildDir.getAbsolutePath(), NEOW3J_DEFAULT_OUTPUT_DIR));

        List<URL> clDirs = new ArrayList<>();

        // adding the source sets dir of the project
        List<URL> sourceSetDirsURL = getSourceSetsDirsURL(neow3jPluginCompile.getProject());
        clDirs.addAll(sourceSetDirsURL);

        // adding the build dir of the project
        URL buildDirsURL = getBuildDirURL(projectBuildDir);
        clDirs.add(buildDirsURL);

        URL[] clDirsArray = clDirs.stream().toArray(URL[]::new);
        URLClassLoader compilerClassLoader = new URLClassLoader(clDirsArray,
                this.getClass().getClassLoader());

        // Get the absolute path of the source file
        String setOfSourceSetFilesPath = getSourceSetsFilesURL(
                neow3jPluginCompile.getProject()).stream()
                .map(URL::getPath)
                .filter(s -> s.contains(canonicalClassName.replace(".", "/")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Couldn't find the source file "
                        + "belonging to " + canonicalClassName));

        Compiler n = new Compiler(compilerClassLoader);

        try {
            // compile
            CompilationUnit compilationUnit;
            if (debugSymbols) {
                compilationUnit = n.compileClass(canonicalClassName, setOfSourceSetFilesPath);
            } else {
                compilationUnit = n.compileClass(canonicalClassName);
            }

            Path outDir = createDirectories(outputDir);
            String contractName = compilationUnit.getManifest().getName();
            if (contractName == null || contractName.length() == 0) {
                throw new IllegalStateException("No contract name is set in the contract's "
                        + "manifest.");
            }

            String nefFileName = writeNefFile(compilationUnit.getNefFile(), contractName, outDir);
            String manifestFileName = writeContractManifestFile(compilationUnit.getManifest(),
                    outDir);

            // if everything goes fine, print info
            System.out.println("Compilation succeeded!");
            System.out.println("NEF file: " + nefFileName);
            System.out.println("Manifest file: " + manifestFileName);

            if (debugSymbols) {
                // Pack the debug info into a ZIP archive.
                String debugInfoZipFileName = Neow3jPluginUtils.writeDebugInfoZip(
                        compilationUnit.getDebugInfo(), contractName, outDir);
                System.out.println("Debug info zip file: " + debugInfoZipFileName);
            }

        } catch (Exception e) {
            System.out.println("Compilation failed.");
            ShowStacktrace showStacktrace = neow3jPluginCompile.getProject().getGradle()
                    .getStartParameter().getShowStacktrace();
            RuntimeException r;
            if (showStacktrace.equals(ShowStacktrace.ALWAYS)) {
                r = new RuntimeException(e);
            } else {
                r = new RuntimeException(e.getMessage());
            }
            throw r;
        }
    }

}
