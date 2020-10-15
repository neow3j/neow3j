package io.neow3j.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neow3jJavaCompiler {

    private static final Logger log = LoggerFactory.getLogger(Neow3jJavaCompiler.class);

    private final JavaCompiler javac;
    private final List<SourceFileObject> sourceFiles = new ArrayList<>();
    private final StandardJavaFileManager fileManager;
    private final DiagnosticCollector<JavaFileObject> diagnosticCollector;
    private final File classOutputDir;

    public Neow3jJavaCompiler() throws IOException {
        this.javac = ToolProvider.getSystemJavaCompiler();
        classOutputDir = Files.createTempDirectory("neow3j").toFile();
        classOutputDir.deleteOnExit();
        diagnosticCollector = new DiagnosticCollector<>();
        fileManager = javac.getStandardFileManager(diagnosticCollector, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(classOutputDir));
    }

    public StandardJavaFileManager compileAll() {
        if (sourceFiles.size() == 0) {
            throw new CompilerException("Nothing to compile because no source files were set.");
        }
        List<String> options = Arrays.asList("-d", classOutputDir.getAbsolutePath(), "-g");
        boolean result = javac.getTask(null, fileManager, diagnosticCollector, options, null,
                sourceFiles).call();

        if (!result || diagnosticCollector.getDiagnostics().size() > 0) {
            StringBuilder exceptionMsg = new StringBuilder();
            boolean hasWarnings = false;
            boolean hasErrors = false;
            for (Diagnostic<? extends JavaFileObject> d : diagnosticCollector.getDiagnostics()) {
                switch (d.getKind()) {
                    case NOTE:
                    case MANDATORY_WARNING:
                    case WARNING:
                        hasWarnings = true;
                        break;
                    case OTHER:
                    case ERROR:
                    default:
                        hasErrors = true;
                        break;
                }
                exceptionMsg.append(d.getSource().getName()).append(": ")
                        .append("kind=").append(d.getKind())
                        .append(", line=").append(d.getLineNumber())
                        .append(", message=").append(d.getMessage(Locale.US))
                        .append("\n");
            }
            if (hasErrors) {
                throw new CompilerException(exceptionMsg.toString());
            }
            if (hasWarnings) {
                log.warn(exceptionMsg.toString());
            }
        }
        return fileManager;
    }

    public Neow3jJavaCompiler addSources(List<File> sourceFiles) throws IOException {
        Set<File> sourceDirs = new HashSet<>();
        for (File sourceFile : sourceFiles) {
            sourceDirs.add(sourceFile.getParentFile());
            String sourceCode = new String(Files.readAllBytes(sourceFile.toPath()));
            this.sourceFiles.add(new SourceFileObject(sourceFile.getAbsolutePath(), sourceCode));
        }
        // Add all collected source directories to the source path of the file manager.
        if (fileManager.getLocation(StandardLocation.SOURCE_PATH) != null) {
            fileManager.getLocation(StandardLocation.SOURCE_PATH).forEach(sourceDirs::add);
        }
        fileManager.setLocation(StandardLocation.SOURCE_PATH, sourceDirs);
        return this;
    }

    public StandardJavaFileManager getFileManager() {
        return fileManager;
    }

    public static class SourceFileObject extends SimpleJavaFileObject {

        private final String contents;
        private final String absoluteFileName;

        public SourceFileObject(String absoluteFileName, String contents) {
            super(URI.create("file:///" + absoluteFileName), Kind.SOURCE);
            this.contents = contents;
            this.absoluteFileName = absoluteFileName;
        }

        @Override
        public String getCharContent(boolean ignoreEncodingErrors) {
            return contents;
        }

        @Override
        public String getName() {
            return absoluteFileName;
        }

    }
}
