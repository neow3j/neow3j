package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.CompilerException;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.script.OpCode;

import java.io.IOException;

import org.objectweb.asm.tree.AbstractInsnNode;

import static java.lang.String.format;

public class MiscConverter implements Converter {

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            // region ### CONVERSION ###
            case I2B:
            case L2I:
            case I2L:
            case I2C:
            case I2S:
                // Nothing to do because the neo-vm treats these types all the same.
                break;
            // endregion ### CONVERSION ###

            // region ### FLOATING POINT (unsupported) ###
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
            case FRETURN:
            case DRETURN:
            case F2I:
            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
            case I2F:
            case I2D:
            case L2F:
            case L2D:
            case FNEG:
            case DNEG:
            case FDIV:
            case DDIV:
            case FREM:
            case DREM:
            case FMUL:
            case DMUL:
            case FSUB:
            case DSUB:
            case FADD:
            case DADD:
            case FASTORE:
            case DASTORE:
            case FALOAD:
            case DALOAD:
            case FSTORE:
            case DSTORE:
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case DCONST_0:
            case DCONST_1:
            case FSTORE_0:
            case FSTORE_1:
            case FSTORE_2:
            case FSTORE_3:
            case DSTORE_0:
            case DSTORE_1:
            case DSTORE_2:
            case DSTORE_3:
            case FLOAD_0:
            case FLOAD_1:
            case FLOAD_2:
            case FLOAD_3:
            case DLOAD_0:
            case DLOAD_1:
            case DLOAD_2:
            case DLOAD_3:
            case FLOAD:
            case DLOAD:
                throw new CompilerException(neoMethod, "Floating point numbers are not supported.");
                // endregion ### FLOATING POINT (unsupported) ###

                // region ### MISCELLANEOUS ###
            case ATHROW:
                neoMethod.addInstruction(new NeoInstruction(OpCode.THROW));
                break;
            case MONITORENTER:
            case MONITOREXIT:
            case WIDE:
                // This should never happen for variable loading or storing because the compiler restricts the number
                // of variables to 256, meaning that all local variables can be indexed with one byte. Also, the Java
                // compiler seems not to use the WIDE opcode for integer increments, even with numbers larger than a
                // byte. See https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.wide
                throw new CompilerException(neoMethod, format("JVM opcode %s is not supported.", opcode.name()));
                // endregion ### MISCELLANEOUS ###
        }
        return insn;
    }

}
