package io.neow3j.compiler;

public class CompilerException extends RuntimeException {

    public CompilerException(String s) {
        super(s);
    }

    public CompilerException(ClassNotFoundException e) {
        super(e);
    }
}
