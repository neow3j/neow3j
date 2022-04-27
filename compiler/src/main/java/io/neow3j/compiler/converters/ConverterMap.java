package io.neow3j.compiler.converters;

import io.neow3j.compiler.JVMOpcode;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps JVM opcodes to converter classes that convert those opcodes to neo-vm instructions.
 */
public class ConverterMap {

    private static final Map<JVMOpcode, Converter> converterMap = new HashMap<>();

    static {
        Converter conv = new ObjectsConverter();
        converterMap.put(JVMOpcode.PUTSTATIC, conv);
        converterMap.put(JVMOpcode.GETSTATIC, conv);
        converterMap.put(JVMOpcode.CHECKCAST, conv);
        converterMap.put(JVMOpcode.NEW, conv);
        converterMap.put(JVMOpcode.ARRAYLENGTH, conv);
        converterMap.put(JVMOpcode.INSTANCEOF, conv);

        conv = new ConstantsConverter();
        converterMap.put(JVMOpcode.ICONST_M1, conv);
        converterMap.put(JVMOpcode.ICONST_0, conv);
        converterMap.put(JVMOpcode.ICONST_1, conv);
        converterMap.put(JVMOpcode.ICONST_2, conv);
        converterMap.put(JVMOpcode.ICONST_3, conv);
        converterMap.put(JVMOpcode.ICONST_4, conv);
        converterMap.put(JVMOpcode.ICONST_5, conv);
        converterMap.put(JVMOpcode.LCONST_0, conv);
        converterMap.put(JVMOpcode.LCONST_1, conv);
        converterMap.put(JVMOpcode.LDC, conv);
        converterMap.put(JVMOpcode.LDC_W, conv);
        converterMap.put(JVMOpcode.LDC2_W, conv);
        converterMap.put(JVMOpcode.ACONST_NULL, conv);
        converterMap.put(JVMOpcode.BIPUSH, conv);
        converterMap.put(JVMOpcode.SIPUSH, conv);

        conv = new MethodsConverter();
        converterMap.put(JVMOpcode.RETURN, conv);
        converterMap.put(JVMOpcode.IRETURN, conv);
        converterMap.put(JVMOpcode.ARETURN, conv);
        converterMap.put(JVMOpcode.LRETURN, conv);
        converterMap.put(JVMOpcode.INVOKESTATIC, conv);
        converterMap.put(JVMOpcode.INVOKEVIRTUAL, conv);
        converterMap.put(JVMOpcode.INVOKESPECIAL, conv);
        converterMap.put(JVMOpcode.INVOKEINTERFACE, conv);
        converterMap.put(JVMOpcode.INVOKEDYNAMIC, conv);

        conv = new LocalVariablesConverter();
        converterMap.put(JVMOpcode.ASTORE, conv);
        converterMap.put(JVMOpcode.ASTORE_0, conv);
        converterMap.put(JVMOpcode.ASTORE_1, conv);
        converterMap.put(JVMOpcode.ASTORE_2, conv);
        converterMap.put(JVMOpcode.ASTORE_3, conv);
        converterMap.put(JVMOpcode.ISTORE, conv);
        converterMap.put(JVMOpcode.ISTORE_0, conv);
        converterMap.put(JVMOpcode.ISTORE_1, conv);
        converterMap.put(JVMOpcode.ISTORE_2, conv);
        converterMap.put(JVMOpcode.ISTORE_3, conv);
        converterMap.put(JVMOpcode.LSTORE, conv);
        converterMap.put(JVMOpcode.LSTORE_0, conv);
        converterMap.put(JVMOpcode.LSTORE_1, conv);
        converterMap.put(JVMOpcode.LSTORE_2, conv);
        converterMap.put(JVMOpcode.LSTORE_3, conv);
        converterMap.put(JVMOpcode.ALOAD, conv);
        converterMap.put(JVMOpcode.ALOAD_0, conv);
        converterMap.put(JVMOpcode.ALOAD_1, conv);
        converterMap.put(JVMOpcode.ALOAD_2, conv);
        converterMap.put(JVMOpcode.ALOAD_3, conv);
        converterMap.put(JVMOpcode.ILOAD, conv);
        converterMap.put(JVMOpcode.ILOAD_0, conv);
        converterMap.put(JVMOpcode.ILOAD_1, conv);
        converterMap.put(JVMOpcode.ILOAD_2, conv);
        converterMap.put(JVMOpcode.ILOAD_3, conv);
        converterMap.put(JVMOpcode.LLOAD, conv);
        converterMap.put(JVMOpcode.LLOAD_0, conv);
        converterMap.put(JVMOpcode.LLOAD_1, conv);
        converterMap.put(JVMOpcode.LLOAD_2, conv);
        converterMap.put(JVMOpcode.LLOAD_3, conv);

        conv = new ArraysConverter();
        converterMap.put(JVMOpcode.NEWARRAY, conv);
        converterMap.put(JVMOpcode.ANEWARRAY, conv);
        converterMap.put(JVMOpcode.BASTORE, conv);
        converterMap.put(JVMOpcode.IASTORE, conv);
        converterMap.put(JVMOpcode.AASTORE, conv);
        converterMap.put(JVMOpcode.CASTORE, conv);
        converterMap.put(JVMOpcode.LASTORE, conv);
        converterMap.put(JVMOpcode.SASTORE, conv);
        converterMap.put(JVMOpcode.AALOAD, conv);
        converterMap.put(JVMOpcode.BALOAD, conv);
        converterMap.put(JVMOpcode.CALOAD, conv);
        converterMap.put(JVMOpcode.IALOAD, conv);
        converterMap.put(JVMOpcode.LALOAD, conv);
        converterMap.put(JVMOpcode.SALOAD, conv);
        converterMap.put(JVMOpcode.PUTFIELD, conv);
        converterMap.put(JVMOpcode.GETFIELD, conv);
        converterMap.put(JVMOpcode.MULTIANEWARRAY, conv);

        conv = new StackManipulationConverter();
        converterMap.put(JVMOpcode.NOP, conv);
        converterMap.put(JVMOpcode.DUP, conv);
        converterMap.put(JVMOpcode.DUP2, conv);
        converterMap.put(JVMOpcode.POP, conv);
        converterMap.put(JVMOpcode.POP2, conv);
        converterMap.put(JVMOpcode.SWAP, conv);
        converterMap.put(JVMOpcode.DUP_X1, conv);
        converterMap.put(JVMOpcode.DUP_X2, conv);
        converterMap.put(JVMOpcode.DUP2_X1, conv);
        converterMap.put(JVMOpcode.DUP2_X2, conv);

        conv = new JumpsConverter();
        converterMap.put(JVMOpcode.IF_ACMPEQ, conv);
        converterMap.put(JVMOpcode.IF_ACMPNE, conv);
        converterMap.put(JVMOpcode.IF_ICMPEQ, conv);
        converterMap.put(JVMOpcode.IF_ICMPNE, conv);
        converterMap.put(JVMOpcode.IF_ICMPLT, conv);
        converterMap.put(JVMOpcode.IF_ICMPGT, conv);
        converterMap.put(JVMOpcode.IF_ICMPLE, conv);
        converterMap.put(JVMOpcode.IF_ICMPGE, conv);
        converterMap.put(JVMOpcode.IFEQ, conv);
        converterMap.put(JVMOpcode.IFNULL, conv);
        converterMap.put(JVMOpcode.IFNE, conv);
        converterMap.put(JVMOpcode.IFNONNULL, conv);
        converterMap.put(JVMOpcode.IFLT, conv);
        converterMap.put(JVMOpcode.IFLE, conv);
        converterMap.put(JVMOpcode.IFGT, conv);
        converterMap.put(JVMOpcode.IFGE, conv);
        converterMap.put(JVMOpcode.LCMP, conv);
        converterMap.put(JVMOpcode.GOTO, conv);
        converterMap.put(JVMOpcode.GOTO_W, conv);
        converterMap.put(JVMOpcode.LOOKUPSWITCH, conv);
        converterMap.put(JVMOpcode.TABLESWITCH, conv);
        converterMap.put(JVMOpcode.JSR, conv);
        converterMap.put(JVMOpcode.RET, conv);
        converterMap.put(JVMOpcode.JSR_W, conv);

        conv = new ArithmeticsConverter();
        converterMap.put(JVMOpcode.IINC, conv);
        converterMap.put(JVMOpcode.IADD, conv);
        converterMap.put(JVMOpcode.LADD, conv);
        converterMap.put(JVMOpcode.ISUB, conv);
        converterMap.put(JVMOpcode.LSUB, conv);
        converterMap.put(JVMOpcode.IMUL, conv);
        converterMap.put(JVMOpcode.LMUL, conv);
        converterMap.put(JVMOpcode.IDIV, conv);
        converterMap.put(JVMOpcode.LDIV, conv);
        converterMap.put(JVMOpcode.IREM, conv);
        converterMap.put(JVMOpcode.LREM, conv);
        converterMap.put(JVMOpcode.INEG, conv);
        converterMap.put(JVMOpcode.LNEG, conv);

        conv = new BitOperationsConverter();
        converterMap.put(JVMOpcode.ISHL, conv);
        converterMap.put(JVMOpcode.LSHL, conv);
        converterMap.put(JVMOpcode.ISHR, conv);
        converterMap.put(JVMOpcode.LSHR, conv);
        converterMap.put(JVMOpcode.IUSHR, conv);
        converterMap.put(JVMOpcode.LUSHR, conv);
        converterMap.put(JVMOpcode.IAND, conv);
        converterMap.put(JVMOpcode.LAND, conv);
        converterMap.put(JVMOpcode.IOR, conv);
        converterMap.put(JVMOpcode.LOR, conv);
        converterMap.put(JVMOpcode.IXOR, conv);
        converterMap.put(JVMOpcode.LXOR, conv);

        conv = new MiscConverter();
        converterMap.put(JVMOpcode.I2B, conv);
        converterMap.put(JVMOpcode.L2I, conv);
        converterMap.put(JVMOpcode.I2L, conv);
        converterMap.put(JVMOpcode.I2C, conv);
        converterMap.put(JVMOpcode.I2S, conv);
        converterMap.put(JVMOpcode.FCMPL, conv);
        converterMap.put(JVMOpcode.FCMPG, conv);
        converterMap.put(JVMOpcode.DCMPL, conv);
        converterMap.put(JVMOpcode.DCMPG, conv);
        converterMap.put(JVMOpcode.FRETURN, conv);
        converterMap.put(JVMOpcode.DRETURN, conv);
        converterMap.put(JVMOpcode.F2I, conv);
        converterMap.put(JVMOpcode.F2L, conv);
        converterMap.put(JVMOpcode.F2D, conv);
        converterMap.put(JVMOpcode.D2I, conv);
        converterMap.put(JVMOpcode.D2L, conv);
        converterMap.put(JVMOpcode.D2F, conv);
        converterMap.put(JVMOpcode.I2F, conv);
        converterMap.put(JVMOpcode.I2D, conv);
        converterMap.put(JVMOpcode.L2F, conv);
        converterMap.put(JVMOpcode.L2D, conv);
        converterMap.put(JVMOpcode.FNEG, conv);
        converterMap.put(JVMOpcode.DNEG, conv);
        converterMap.put(JVMOpcode.FDIV, conv);
        converterMap.put(JVMOpcode.DDIV, conv);
        converterMap.put(JVMOpcode.FREM, conv);
        converterMap.put(JVMOpcode.DREM, conv);
        converterMap.put(JVMOpcode.FMUL, conv);
        converterMap.put(JVMOpcode.DMUL, conv);
        converterMap.put(JVMOpcode.FSUB, conv);
        converterMap.put(JVMOpcode.DSUB, conv);
        converterMap.put(JVMOpcode.FADD, conv);
        converterMap.put(JVMOpcode.DADD, conv);
        converterMap.put(JVMOpcode.FASTORE, conv);
        converterMap.put(JVMOpcode.DASTORE, conv);
        converterMap.put(JVMOpcode.FALOAD, conv);
        converterMap.put(JVMOpcode.DALOAD, conv);
        converterMap.put(JVMOpcode.FSTORE, conv);
        converterMap.put(JVMOpcode.DSTORE, conv);
        converterMap.put(JVMOpcode.FCONST_0, conv);
        converterMap.put(JVMOpcode.FCONST_1, conv);
        converterMap.put(JVMOpcode.FCONST_2, conv);
        converterMap.put(JVMOpcode.DCONST_0, conv);
        converterMap.put(JVMOpcode.DCONST_1, conv);
        converterMap.put(JVMOpcode.FSTORE_0, conv);
        converterMap.put(JVMOpcode.FSTORE_1, conv);
        converterMap.put(JVMOpcode.FSTORE_2, conv);
        converterMap.put(JVMOpcode.FSTORE_3, conv);
        converterMap.put(JVMOpcode.DSTORE_0, conv);
        converterMap.put(JVMOpcode.DSTORE_1, conv);
        converterMap.put(JVMOpcode.DSTORE_2, conv);
        converterMap.put(JVMOpcode.DSTORE_3, conv);
        converterMap.put(JVMOpcode.FLOAD_0, conv);
        converterMap.put(JVMOpcode.FLOAD_1, conv);
        converterMap.put(JVMOpcode.FLOAD_2, conv);
        converterMap.put(JVMOpcode.FLOAD_3, conv);
        converterMap.put(JVMOpcode.DLOAD_0, conv);
        converterMap.put(JVMOpcode.DLOAD_1, conv);
        converterMap.put(JVMOpcode.DLOAD_2, conv);
        converterMap.put(JVMOpcode.DLOAD_3, conv);
        converterMap.put(JVMOpcode.FLOAD, conv);
        converterMap.put(JVMOpcode.DLOAD, conv);
        converterMap.put(JVMOpcode.ATHROW, conv);
        converterMap.put(JVMOpcode.MONITORENTER, conv);
        converterMap.put(JVMOpcode.MONITOREXIT, conv);
        converterMap.put(JVMOpcode.WIDE, conv);

    }

    public static Converter get(JVMOpcode opcode) {
        return converterMap.get(opcode);
    }

}
