package io.neow3j.devpack.compiler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Compiler {

    public static void main(String[] args) throws Exception {
        ClassReader reader = new ClassReader("io.neow3j.devpack.template.HelloWorldContract");
        ClassNode n = new ClassNode();
        reader.accept(n, 0);
    }

}
