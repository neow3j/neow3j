package io.neow3j.compiler;

import static java.lang.String.format;

import io.neow3j.contract.NefFile;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import org.objectweb.asm.tree.ClassNode;

/**
 * Acts as the central object that is passed around in the compilation process and holds the
 * class loader to use for loading classes required for the compilation, the {@link NeoModule}
 * that is built in the compilation, and the Java smart contract class that is compiled.
 */
public class CompilationUnit {

    public CompilationUnit(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.neoModule = new NeoModule();
    }

    /**
     * The class loader used to get and load classes needed in the compilation of a smart contract.
     */
    private ClassLoader classLoader;

    /**
     *
     */
    private NeoModule neoModule;

    /**
     * The smart contract class that this compilation unit is concerned with.
     */
    ClassNode contractClassNode;

    NefFile nef;

    ContractManifest manifest;

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public NeoModule getNeoModule() {
        return neoModule;
    }

    public ClassNode getContractClassNode() {
        return contractClassNode;
    }

    public NefFile getNef() {
        return nef;
    }

    public ContractManifest getManifest() {
        return manifest;
    }
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setNeoModule(NeoModule neoModule) {
        this.neoModule = neoModule;
    }

    public void setAsmClass(ClassNode asmClass) {
        this.contractClassNode = asmClass;
    }

    public void throwErr(NeoMethod neoMethod, String message) {
        throw new CompilerException(this, neoMethod, message);
    }

    public void setNef(NefFile nef) {
        this.nef = nef;
    }

    public void setManifest(ContractManifest manifest) {
        this.manifest = manifest;
    }
}