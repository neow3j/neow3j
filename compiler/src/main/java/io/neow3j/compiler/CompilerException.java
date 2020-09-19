package io.neow3j.compiler;

import org.objectweb.asm.tree.ClassNode;

public class CompilerException extends RuntimeException {

    public CompilerException(String s) {
        super(s);
    }

    public CompilerException(ClassNode owner, int lineNr, String s) {
        super(owner.sourceFile + ":" + lineNr + ": error:\n" + s);
    }

    public CompilerException(NeoModule module, NeoMethod method, String s) {
        this(module.asmSmartContractClass, method.currentLine, s);
    }

    public CompilerException(ClassNotFoundException e) {
        super(e);
    }
}
