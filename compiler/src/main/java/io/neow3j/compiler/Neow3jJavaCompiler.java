package io.neow3j.compiler;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neow3jJavaCompiler {

    private static final Logger log = LoggerFactory.getLogger(Neow3jJavaCompiler.class);

    private final JavaCompiler javac;
    private final List<Neow3jJavaFileObject> sourceFiles = new ArrayList<>();

    public Neow3jJavaCompiler() {
        this.javac = ToolProvider.getSystemJavaCompiler();
    }

    public File compileAll() throws Exception {
        if (sourceFiles.size() == 0) {
            throw new CompilerException("Nothing to compile because no source files were set.");
        }
        // Create a temporary directory for storing the compiled class files.
        File classesDir = Files.createTempDirectory("neow3j-compiler").toFile();
        classesDir.deleteOnExit();
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        List<String> options = Arrays.asList("-d", classesDir.getAbsolutePath(), "-g");
        boolean result = javac.getTask(null, null, collector, options, null, sourceFiles)
                .call();

        if (!result || collector.getDiagnostics().size() > 0) {
            StringBuilder exceptionMsg = new StringBuilder();
            boolean hasWarnings = false;
            boolean hasErrors = false;
            for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
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
        return classesDir;
    }

    public Neow3jJavaCompiler addSources(List<File> sourceFiles) throws Exception {
        for (File sourceFile : sourceFiles) {
            String sourceCode = new String(Files.readAllBytes(sourceFile.toPath()));
            this.sourceFiles.add(new Neow3jJavaFileObject(
                    sourceFile.getAbsolutePath(), sourceCode));
        }
        return this;
    }

    public static class Neow3jJavaFileObject extends SimpleJavaFileObject {

        private final String contents;
        private final String absoluteFileName;

        public Neow3jJavaFileObject(String absoluteFileName, String contents) {
            super(URI.create("string:///" + absoluteFileName), Kind.SOURCE);
            this.contents = contents;
            this.absoluteFileName = absoluteFileName;
        }

        public String getAbsoluteFileName() {
            return absoluteFileName;
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
