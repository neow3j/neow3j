package io.neow3j.devpack.gradle;

import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getBuildDirURL;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getCompileOutputFileName;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getSourceSetsDirsURL;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.writeToFile;
import static java.nio.file.Files.createDirectories;
import static java.util.Optional.ofNullable;

import io.neow3j.compiler.Compiler;
import io.neow3j.compiler.Compiler.CompilationResult;
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

        System.out.println("Smart Contract class: "
                + canonicalClassName);

        ofNullable(canonicalClassName)
                .orElseThrow(() ->
                        new IllegalArgumentException("The parameter '"
                                + Neow3jPluginOptions.CLASSNAME_NAME
                                + "' should be set."));

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
            CompilationResult compilationResult = n.compileClass(canonicalClassName);
            byte[] nefBytes = compilationResult.getNef().toArray();

            // output the result to the output file
            String outDir = createDirectories(neow3jPluginCompile.getCompilerOutputDir())
                    .toString();
            String outFileName = getCompileOutputFileName(canonicalClassName);
            Path outputFile = Paths.get(outDir, outFileName);
            writeToFile(outputFile.toFile(), nefBytes);

            // if everything goes fine, print info
            System.out.println("Compilation succeeded!");
            System.out.println("NEF output file: " + outputFile.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Compilation failed. Reason: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

}
