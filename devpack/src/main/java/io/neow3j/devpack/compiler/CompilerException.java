package io.neow3j.devpack.compiler;

public class CompilerException extends RuntimeException {

    public CompilerException(String s) {
        super(s);
    }

    public CompilerException(ClassNotFoundException e) {
        super(e);
    }
}
