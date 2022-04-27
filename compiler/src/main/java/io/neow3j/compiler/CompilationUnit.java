package io.neow3j.compiler;

import io.neow3j.compiler.sourcelookup.ISourceContainer;
import io.neow3j.contract.NefFile;
import io.neow3j.protocol.core.response.ContractManifest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

/**
 * Acts as the central object that is passed around in the compilation process and holds the class loader to use for
 * loading classes required for the compilation, the {@link NeoModule} that is built in the compilation, and the Java
 * smart contract class that is compiled.
 */
public class CompilationUnit {

    /**
     * The class loader used to get and load classes needed in the compilation of a smart contract.
     */
    private ClassLoader classLoader;

    /**
     * The module containing the compiled classes and methods.
     */
    private final NeoModule neoModule;

    /**
     * The NEF file containing the compiled script.
     */
    private NefFile nef;

    /**
     * The compiled contract's manifest.
     */
    private ContractManifest manifest;

    /**
     * The debug information to be used for debugging the compiled contact.
     */
    private DebugInfo debugInfo;

    // The main contract class
    private ClassNode contractClass;

    // Maps fully qualified class names to their source files.
    private final Map<String, File> sourceFileMap = new HashMap<>();
    private final List<ISourceContainer> sourceContainers = new ArrayList<>();

    public CompilationUnit(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.neoModule = new NeoModule();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public NeoModule getNeoModule() {
        return neoModule;
    }

    public NefFile getNefFile() {
        return nef;
    }

    public ContractManifest getManifest() {
        return manifest;
    }

    public DebugInfo getDebugInfo() {
        return debugInfo;
    }

    protected void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    protected void setNef(NefFile nef) {
        this.nef = nef;
    }

    protected void setManifest(ContractManifest manifest) {
        this.manifest = manifest;
    }

    protected void setDebugInfo(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    /**
     * Adds the given {@code ClassNode} to the list of classes that form the smart contract. I.e., only classes that
     * are added here can contribute to a contract's public interface.
     *
     * @param classNode the class to add.
     */
    protected void setContractClass(ClassNode classNode) {
        contractClass = classNode;
    }

    /**
     * Gets the main contract class of the smart contract being compiled.
     *
     * @return the contract class.
     */
    protected ClassNode getContractClass() {
        return contractClass;
    }

    /**
     * Gets the source file corresponding to the given class if available in this compilation's source containers.
     * Only returns the first occurrence of a match.
     *
     * @param classNode the class to get the source file for.
     * @return the matching source file found in the source containers, or null if not found.
     */
    protected File getSourceFile(ClassNode classNode) {
        if (sourceFileMap.containsKey(classNode.name)) {
            return sourceFileMap.get(classNode.name);
        }
        String filePath = extractFilePathWithPackage(classNode);
        for (ISourceContainer container : sourceContainers) {
            File sourceFile = container.findSourceFile(filePath);
            if (sourceFile != null) {
                sourceFileMap.put(classNode.name, sourceFile);
                return sourceFile;
            }
        }
        // If we cannot find a source for a class we remember that in the source map as well to save time the next
        // time this class is searched for.
        sourceFileMap.put(classNode.name, null);
        return null;
    }

    // Extracts the path of the source file of the given class node. The source file's name is taken from the node's
    // `sourceFile` variable, a ".java" is appended, and the package name is prepended.
    private String extractFilePathWithPackage(ClassNode classNode) {
        int idx = classNode.name.lastIndexOf('/');
        String packageName = classNode.name.substring(0, idx + 1); // includes last slash.
        String sourceFileName = classNode.sourceFile;
        if (!classNode.sourceFile.contains(".java")) {
            sourceFileName = sourceFileName + ".java";
        }
        return (packageName + sourceFileName).replace('/', File.separatorChar);
    }

    public void addSourceContainers(List<ISourceContainer> sourceContainers) {
        this.sourceContainers.addAll(sourceContainers);
    }

}
