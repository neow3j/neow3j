package io.neow3j.compiler;

import org.objectweb.asm.tree.ClassNode;

public class CompilerException extends RuntimeException {

    public CompilerException(String s) {
        super(s);
    }

    public CompilerException(NeoMethod neoMethod, String s) {
        super(neoMethod.getOwnerClass().sourceFile + ":" + neoMethod.getCurrentLine() + ": "
                + "error:\n" + s);
    }

    public CompilerException(ClassNode owner, String s) {
        super(owner.sourceFile + ": error:\n" + s);
    }

    public CompilerException(CompilationUnit compUnit, NeoMethod method, String s) {
        super(compUnit.getSourceFile(method.getOwnerClassName()) + ":" + method.getCurrentLine()
                + ": error:\n" + s);
    }

    public CompilerException(ClassNotFoundException e) {
        super(e);
    }
}
