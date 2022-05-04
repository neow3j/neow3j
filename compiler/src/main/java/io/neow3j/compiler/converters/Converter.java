package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.NeoMethod;
import java.io.IOException;
import org.objectweb.asm.tree.AbstractInsnNode;

public interface Converter {

    AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit) throws IOException;

}
