package io.neow3j.devpack.gradle;

import static io.neow3j.contract.ContractUtils.generateContractManifestFile;
import static io.neow3j.devpack.gradle.Neow3jCompileTask.NEOW3J_COMPILER_OPTIONS_NAME;
import static io.neow3j.devpack.gradle.Neow3jPluginOptions.CLASSNAME_NAME;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.generateDebugInfoZip;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getBuildDirURL;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getCompileOutputFileName;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getSourceSetsDirsURL;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.writeToFile;
import static java.nio.file.Files.createDirectories;
import static java.util.Optional.ofNullable;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.utils.ClassUtils;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Action;

public class Neow3jCompileAction implements Action<Neow3jCompileTask> {

    @Override
    public void execute(Neow3jCompileTask neow3jPluginCompile) {
        String canonicalClassName = neow3jPluginCompile.getOptions().getClassName();

        ofNullable(canonicalClassName).orElseThrow(() ->
                new IllegalArgumentException("The parameter "
                        + "'" + CLASSNAME_NAME + "' needs to be set in the "
                        + "'" + NEOW3J_COMPILER_OPTIONS_NAME + "' "
                        + "declaration in your build.gradle file."));

        List<URL> clDirs = new ArrayList<>();

        // adding the source sets dir of the project
        List<URL> sourceSetDirsURL = getSourceSetsDirsURL(neow3jPluginCompile.getProject());
        clDirs.addAll(sourceSetDirsURL);

        // adding the build dir of the project
        URL buildDirsURL = getBuildDirURL(neow3jPluginCompile.getProjectBuildDir());
        clDirs.add(buildDirsURL);

        URL[] clDirsArray = clDirs.stream().toArray(URL[]::new);
        URLClassLoader compilerClassLoader = new URLClassLoader(clDirsArray,
                this.getClass().getClassLoader());

        Compiler n = new Compiler(compilerClassLoader);

        try {
            // compile
            CompilationUnit compilationUnit = n.compileClass(canonicalClassName);
            byte[] nefBytes = compilationUnit.getNefFile().toArray();

            // get the output directory
            String outDirString = createDirectories(neow3jPluginCompile.getCompilerOutputDir())
                    .toString();
            Path outDirPath = Paths.get(outDirString);

            // output the result to the output file
            String nefOutFileName = getCompileOutputFileName(canonicalClassName);
            Path outputFile = Paths.get(outDirString, nefOutFileName);
            writeToFile(outputFile.toFile(), nefBytes);

            // generate the manifest to the output dir
            String manifestOutFileName = generateContractManifestFile(
                    compilationUnit.getManifest(),
                    outDirPath.toFile());

            // Pack the debug info into a ZIP archive.
            String debugInfoZipFileName = generateDebugInfoZip(compilationUnit.getDebugInfo(),
                    outDirString, ClassUtils.getClassName(canonicalClassName));

            // if everything goes fine, print info
            System.out.println("Compilation succeeded!");
            System.out.println("NEF file: " + outputFile.toAbsolutePath());
            System.out.println("Manifest file: " + manifestOutFileName);
            System.out.println("Debug info zip file: " + debugInfoZipFileName);
        } catch (Exception e) {
            System.out.println("Compilation failed. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
