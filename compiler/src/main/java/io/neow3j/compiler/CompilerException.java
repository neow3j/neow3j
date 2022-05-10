package io.neow3j.compiler;

import org.objectweb.asm.tree.ClassNode;

import static java.lang.String.format;

public class CompilerException extends RuntimeException {

    public CompilerException(String s) {
        super(s);
    }

    public CompilerException(NeoMethod neoMethod, String errorMessage) {
        super(format("%s: line %s\n Error: \"%s\"", neoMethod.getOwnerClass().sourceFile, neoMethod.getCurrentLine(),
                errorMessage));
    }

    public CompilerException(ClassNode owner, String errorMessage) {
        super(format(owner.sourceFile + "\n Error: \"%s\"", errorMessage));
    }

    public CompilerException(Exception e) {
        super(e);
    }

}
