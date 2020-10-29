package io.neow3j.devpack.gradle;

import static io.neow3j.contract.ContractUtils.generateContractManifestFile;
import static io.neow3j.devpack.gradle.Neow3jCompileTask.NEOW3J_COMPILER_OPTIONS_NAME;
import static io.neow3j.devpack.gradle.Neow3jPluginOptions.CLASSNAMES_NAME;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.generateDebugInfoZip;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getBuildDirURL;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getCompileOutputFileName;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getSourceSetsDirsURL;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getSourceSetsFilesURL;
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
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.Action;

public class Neow3jCompileAction implements Action<Neow3jCompileTask> {

    @Override
    public void execute(Neow3jCompileTask neow3jPluginCompile) {
        Set<String> canonicalClassNames = neow3jPluginCompile.getOptions().getClassNames();
        Boolean debugSymbols = neow3jPluginCompile.getOptions().getDebug();

        ofNullable(canonicalClassNames).orElseThrow(() ->
                new IllegalArgumentException("The parameter "
                        + "'" + CLASSNAMES_NAME + "' needs to be set in the "
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

        // get the source files set URL
        Set<String> setOfSourceSetFilesPath = getSourceSetsFilesURL(neow3jPluginCompile.getProject())
                .stream()
                .map(URL::getPath)
                .collect(Collectors.toSet());

        Compiler n = new Compiler(compilerClassLoader);

        try {
            // compile
            CompilationUnit compilationUnit;
            if (debugSymbols) {
                compilationUnit = n.compileClasses(canonicalClassNames, setOfSourceSetFilesPath);
            } else {
                compilationUnit = n.compileClasses(canonicalClassNames);
            }
            byte[] nefBytes = compilationUnit.getNefFile().toArray();

            // get the output directory
            String outDirString = createDirectories(neow3jPluginCompile.getCompilerOutputDir())
                    .toString();
            Path outDirPath = Paths.get(outDirString);

            // output the result to the output file
            // TODO: when there's multiple class names, which one to use for the final NEF?
            String firstCanonicalClassName = canonicalClassNames
                    .stream().findFirst().orElse(null);

            String nefOutFileName = getCompileOutputFileName(firstCanonicalClassName);
            Path outputFile = Paths.get(outDirString, nefOutFileName);
            writeToFile(outputFile.toFile(), nefBytes);

            // generate the manifest to the output dir
            String manifestOutFileName = generateContractManifestFile(
                    compilationUnit.getManifest(),
                    outDirPath.toFile());

            // if everything goes fine, print info
            System.out.println("Compilation succeeded!");
            System.out.println("NEF file: " + outputFile.toAbsolutePath());
            System.out.println("Manifest file: " + manifestOutFileName);

            if (debugSymbols) {
                // Pack the debug info into a ZIP archive.
                String debugInfoZipFileName = generateDebugInfoZip(compilationUnit.getDebugInfo(),
                        outDirString, ClassUtils.getClassName(firstCanonicalClassName));
                System.out.println("Debug info zip file: " + debugInfoZipFileName);
            }

        } catch (Exception e) {
            System.out.println("Compilation failed. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
