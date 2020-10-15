package io.neow3j.compiler;


import java.util.HashMap;
import java.util.Map;

public class SourceLookupProvider {

    //    private static final String JAVA_FILE_SUFFIX = ".java";
//    List<String> sourcePaths = new ArrayList<>();
    Map<String, String> cache = new HashMap<>();

    /**
     * Gets the fully qualified name of the class that the given line points to in the given source
     * file.
     *
     * @param sourceFile The absolute path of the source file to search in.
     * @param line The line inside of the source.
     * @return The fully qualified name of the class at the location in the given file pointed to
     * by the given line.
     */
    public String getFullyQualifiedName(String sourceFile, int[] line) {
        throw new UnsupportedOperationException();
    }

    /**
     * Given a fully qualified class name, gets the associated source file.
     *
     * @param fullyQualifiedName the fully qualified class name (e.g. io.neow3j.compiler
     *                           .ISourceLookUpProvider).
     * @return the absolute path of the associated source file.
     */
    public String getSourceFilePath(String fullyQualifiedName) {
        return cache.get(fullyQualifiedName);
        // TODO: Find absolute file path on file system;
//        String sourcePath = fullyQualifiedName.replace('.', File.separatorChar) +
//        JAVA_FILE_SUFFIX;
    }

    /**
     * Adds the given mapping from class name to source file.
     * <p>
     * This mapping is held in a cache. I.e., when looking up a class with the given name it will
     * always return the given source file.
     *
     * @param className  The fully qualified class name.
     * @param sourceFile The absolute path of the source file.
     */
    public void addClassToSourceMapping(String className, String sourceFile) {
        cache.put(className, sourceFile);
    }
}
