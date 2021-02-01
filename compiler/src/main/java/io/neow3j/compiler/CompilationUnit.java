package io.neow3j.compiler;

import io.neow3j.contract.NefFile;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.tree.ClassNode;

/**
 * Acts as the central object that is passed around in the compilation process and holds the class
 * loader to use for loading classes required for the compilation, the {@link NeoModule} that is
 * built in the compilation, and the Java smart contract class that is compiled.
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

    // Maps fully qualified class names to their source files (aboslute file paths).
    private final Map<String, String> sourceFileMap = new HashMap<>();

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
     * Adds the given {@code ClassNode} to the list of classes that form the smart contract. I.e.,
     * only classes that are added here can contribute to a contract's public interface.
     *
     * @param classNode The class to add.
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
     * Gets the absolute path of the source file corresponding to the given class.
     *
     * @param fullyQualifiedClassName The name of the class.
     * @return the absolute path of the source file.
     */
    protected String getSourceFile(String fullyQualifiedClassName) {
        return sourceFileMap.get(fullyQualifiedClassName);
    }

    /**
     * Adds the given mapping between the compiled class and the source file.
     *
     * @param className  The fully qualified name of the class.
     * @param sourceFile The absolute path to the source file that the class was compiled from.
     */
    protected void addClassToSourceMapping(String className, String sourceFile) {
        sourceFileMap.put(className, sourceFile);
    }

}