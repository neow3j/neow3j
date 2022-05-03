package io.neow3j.devpack.gradle;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.compiler.sourcelookup.DirectorySourceContainer;
import io.neow3j.compiler.sourcelookup.ISourceContainer;
import org.gradle.api.Action;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.neow3j.contract.ContractUtils.writeContractManifestFile;
import static io.neow3j.contract.ContractUtils.writeNefFile;
import static io.neow3j.devpack.gradle.Neow3jCompileTask.CLASSNAME_NAME;
import static io.neow3j.devpack.gradle.Neow3jPlugin.EXTENSION_NAME;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.getOutputDirs;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;

public class Neow3jCompileAction implements Action<Neow3jCompileTask> {

    @Override
    public void execute(Neow3jCompileTask neow3jPluginCompile) {
        if (!neow3jPluginCompile.getClassName().isPresent()) {
            throw new IllegalArgumentException(format("The parameter '%s' needs to be set in the '%s' declaration in " +
                    "your build.gradle file.", CLASSNAME_NAME, EXTENSION_NAME));
        }
        String canonicalClassName = neow3jPluginCompile.getClassName().get();
        File projectBuildDir = neow3jPluginCompile.getProject().getBuildDir();
        Boolean debugSymbols = neow3jPluginCompile.getDebug().get();
        File outputDir = neow3jPluginCompile.getOutputDir().getAsFile().get();

        URLClassLoader classLoader = constructClassLoader(neow3jPluginCompile, projectBuildDir);
        CompilationUnit compilationUnit = compile(neow3jPluginCompile, canonicalClassName, debugSymbols, classLoader);

        try {
            Path outDir = createDirectories(outputDir.toPath());
            String contractName = compilationUnit.getManifest().getName();
            if (contractName == null || contractName.length() == 0) {
                throw new IllegalStateException("No contract name is set in the contract's manifest.");
            }

            String nefFileName = writeNefFile(compilationUnit.getNefFile(), contractName, outDir);
            String manifestFileName = writeContractManifestFile(compilationUnit.getManifest(), outDir);

            // if everything goes fine, print info
            System.out.println("Compilation succeeded!");
            System.out.println("NEF file: " + nefFileName);
            System.out.println("Manifest file: " + manifestFileName);

            if (debugSymbols) {
                // Pack the debug info into a ZIP archive.
                String debugInfoZipFileName =
                        Neow3jPluginUtils.writeDebugInfoZip(compilationUnit.getDebugInfo(), contractName, outDir);
                System.out.println("Debug info zip file: " + debugInfoZipFileName);
            }

        } catch (Exception e) {
            System.out.println("Smart contract compilation failed.");
            ShowStacktrace showStacktrace =
                    neow3jPluginCompile.getProject().getGradle().getStartParameter().getShowStacktrace();
            if (showStacktrace.equals(ShowStacktrace.ALWAYS)) {
                throw new RuntimeException(e);
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private CompilationUnit compile(Neow3jCompileTask neow3jPluginCompile, String canonicalClassName,
            Boolean debugSymbols, URLClassLoader classLoader) {

        Compiler n = new Compiler(classLoader);
        CompilationUnit compilationUnit;
        try {
            if (debugSymbols) {
                List<ISourceContainer> containers = constructSourceContainers(neow3jPluginCompile);
                compilationUnit = n.compile(canonicalClassName, containers);
            } else {
                compilationUnit = n.compile(canonicalClassName);
            }
        } catch (Exception e) {
            System.out.println("Smart contract compilation failed.");
            ShowStacktrace showStacktrace =
                    neow3jPluginCompile.getProject().getGradle().getStartParameter().getShowStacktrace();
            if (showStacktrace.equals(ShowStacktrace.ALWAYS)) {
                throw new RuntimeException(e);
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
        return compilationUnit;
    }

    private List<ISourceContainer> constructSourceContainers(Neow3jCompileTask neow3jPluginCompile) {
        SourceSetContainer sourceSets =
                neow3jPluginCompile.getProject().getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        Set<File> sourceDirs = new HashSet<>();
        sourceSets.forEach(sc -> sourceDirs.addAll(sc.getAllJava().getSrcDirs()));
        return sourceDirs.stream()
                .map(sd -> new DirectorySourceContainer(sd, false))
                .collect(Collectors.toList());
    }

    private URLClassLoader constructClassLoader(Neow3jCompileTask neow3jPluginCompile, File projectBuildDir) {
        List<File> classDirs = getOutputDirs(neow3jPluginCompile.getProject());
        classDirs.add(projectBuildDir);
        URL[] classDirURLs = classDirs.stream().map(f -> {
            try {
                return f.toURI().toURL();
            } catch (MalformedURLException e) {
                System.out.printf("Error converting '%s' to URL: %s", f.getAbsolutePath(), e);
            }
            return null;
        }).toArray(URL[]::new);

        return new URLClassLoader(classDirURLs, this.getClass().getClassLoader());
    }

}
