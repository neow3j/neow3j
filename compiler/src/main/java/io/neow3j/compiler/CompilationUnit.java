package io.neow3j.compiler;

import static java.lang.String.format;

import io.neow3j.contract.NefFile;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.tree.ClassNode;

/**
 * Acts as the central object that is passed around in the compilation process and holds the
 * class loader to use for loading classes required for the compilation, the {@link NeoModule}
 * that is built in the compilation, and the Java smart contract class that is compiled.
 */
public class CompilationUnit {

    /**
     * The class loader used to get and load classes needed in the compilation of a smart contract.
     */
    private ClassLoader classLoader;

    /**
     * The module containing the compiled classes and methods.
     */
    private NeoModule neoModule;

    /**
     * The smart contract class that this compilation unit is concerned with.
     */
    private ClassNode contractClassNode;

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

    /**
     * The list of source files from which this unit has been compiled.
     */
    private List<File> sourceFiles;


    public CompilationUnit(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.neoModule = new NeoModule();
        this.sourceFiles = new ArrayList<>();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public NeoModule getNeoModule() {
        return neoModule;
    }

    public ClassNode getContractClassNode() {
        return contractClassNode;
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

    public List<File> getSourceFiles() {
        return sourceFiles;
    }

    protected void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    protected void setNeoModule(NeoModule neoModule) {
        this.neoModule = neoModule;
    }

    protected void setAsmClass(ClassNode asmClass) {
        this.contractClassNode = asmClass;
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

    public void setSourceFiles(List<File> sourceFiles) {
        for (File f : sourceFiles) {
            if (!f.exists()) {
                throw new IllegalArgumentException(
                        format("Source file %s does not exist.", f.getName()));
            }
        }
        this.sourceFiles = sourceFiles;
    }
}