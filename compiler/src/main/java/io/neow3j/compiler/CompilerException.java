package io.neow3j.compiler;

import org.objectweb.asm.tree.ClassNode;

public class CompilerException extends RuntimeException {

    public CompilerException(String s) {
        super(s);
    }

    public CompilerException(NeoMethod neoMethod, String errorMessage) {
        super(neoMethod.getOwnerClass().sourceFile + ".java: " + neoMethod.getCurrentLine() + ": "
                + "error:\n" + errorMessage);
    }

    public CompilerException(ClassNode owner, String errorMessage) {
        super(owner.sourceFile + ".java: error:\n" + errorMessage);
    }

    public CompilerException(Exception e) {
        super(e);
    }

}
