package io.neow3j.compiler;


import java.io.IOException;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;

public interface Converter {

    AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException;

}
