package io.neow3j.compiler;

import static io.neow3j.compiler.AsmHelper.getClassNodeForInternalName;
import static io.neow3j.compiler.AsmHelper.getMethodNode;
import static io.neow3j.compiler.AsmHelper.hasAnnotations;
import static io.neow3j.compiler.Compiler.addInstruction;
import static io.neow3j.compiler.Compiler.addLoadConstant;
import static io.neow3j.compiler.Compiler.addPushDataArray;
import static io.neow3j.compiler.Compiler.addPushNumber;
import static io.neow3j.compiler.Compiler.addReverseArguments;
import static io.neow3j.compiler.Compiler.addSyscall;
import static io.neow3j.compiler.Compiler.compileMethod;
import static io.neow3j.compiler.LocalVariableHelper.addLoadLocalVariable;
import static io.neow3j.compiler.MethodInitializer.initializeMethod;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CALL;
import static io.neow3j.constants.OpCode.getOperandSize;
import static io.neow3j.utils.ClassUtils.getClassNameForInternalName;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.objectweb.asm.Type.getInternalName;

import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.devpack.StaticVariableHelper;
import io.neow3j.devpack.annotations.Contract;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Instruction.Instructions;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.annotations.Syscall.Syscalls;
import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MethodsConverter implements Converter {

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case RETURN:
            case IRETURN:
            case ARETURN:
            case LRETURN:
                neoMethod.addInstruction(new NeoInstruction(OpCode.RET));
                break;
            case INVOKESTATIC:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
                insn = MethodsConverter.handleInvoke(insn, neoMethod, compUnit);
                break;
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
        }
        return insn;
    }

    private static final List<String> PRIMITIVE_TYPE_CAST_METHODS = Arrays.asList(
            "intValue", "longValue", "byteValue", "shortValue", "booleanValue", "charValue");
    private static final List<String> PRIMITIVE_TYPE_WRAPPER_CLASSES = Arrays.asList(
            "java/lang/Integer", "java/lang/Long", "java/lang/Byte", "java/lang/Short",
            "java/lang/Boolean", "java/lang/Character");

    private static final String VALUEOF_METHOD_NAME = "valueOf";
    private static final String HASH_CODE_METHOD_NAME = "hashCode";
    private static final String ADDRESS_TO_SCRIPTHASH_METHOD_NAME = "addressToScriptHash";
    private static final String HEX_TO_BYTES_METHOD_NAME = "hexToBytes";
    private static final String STRING_TO_INT_METHOD_NAME = "stringToInt";
    private static final String INITSSLOT_METHOD_NAME = "_initialize";
    private static final String EQUALS_METHOD_NAME = "hashCode";

    /**
     * Handles all INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC instructions. Note that constructor
     * calls (INVOKESPECIAL) are handled in the {@link ObjectsConverter}
     */
    public static AbstractInsnNode handleInvoke(AbstractInsnNode insn,
            NeoMethod callingNeoMethod, CompilationUnit compUnit) throws IOException {

        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        ClassNode owner = getClassNodeForInternalName(methodInsn.owner, compUnit.getClassLoader());
        Optional<MethodNode> calledAsmMethod = getMethodNode(methodInsn, owner);
        // If the called method cannot be found on the owner type, we look through the super
        // types until we find the method.
        while (!calledAsmMethod.isPresent()) {
            if (owner.superName == null) {
                throw new CompilerException("Couldn't find method " + methodInsn.name + " on "
                        + "owning type and its super types.");
            }
            owner = getClassNodeForInternalName(owner.superName, compUnit.getClassLoader());
            calledAsmMethod = getMethodNode(methodInsn, owner);
        }
        if (hasSyscallAnnotation(calledAsmMethod.get())) {
            addSyscall(calledAsmMethod.get(), callingNeoMethod);
        } else if (hasInstructionAnnotation(calledAsmMethod.get())) {
            addInstruction(calledAsmMethod.get(), callingNeoMethod);
        } else if (isContractCall(owner)) {
            addContractCall(calledAsmMethod.get(), callingNeoMethod, owner);
        } else if (isStaticFieldConverter(calledAsmMethod.get(), owner)) {
            handleStaticFieldConverter(calledAsmMethod.get(), callingNeoMethod, compUnit);
        } else {
            return handleMethodCall(callingNeoMethod, owner, calledAsmMethod.get(), methodInsn,
                    compUnit);
        }
        return insn;
    }

    private static AbstractInsnNode handleMethodCall(NeoMethod callingNeoMethod, ClassNode owner,
            MethodNode calledAsmMethod, MethodInsnNode methodInsn, CompilationUnit compUnit)
            throws IOException {

        String calledMethodId = NeoMethod.getMethodId(calledAsmMethod, owner);
        if (compUnit.getNeoModule().methods.containsKey(calledMethodId)) {
            // If the module already compiled the method simply add a CALL instruction.
            NeoMethod calledNeoMethod = compUnit.getNeoModule().methods.get(calledMethodId);
            addReverseArguments(calledAsmMethod, callingNeoMethod);
            // The actual address offset for the method call is set at a later point in compilation.
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.CALL_L, new byte[4])
                    .setExtra(calledNeoMethod));
        } else {
            return handleUncachedMethodCall(callingNeoMethod, owner, calledAsmMethod, methodInsn,
                    compUnit);
        }
        return methodInsn;
    }

    // Handles method calls that the compiler sees for the first time (in this compilation unit)
    // or have special behavior that will not be cached in a reusable NeoMethod object.
    private static AbstractInsnNode handleUncachedMethodCall(NeoMethod callingNeoMethod,
            ClassNode owner, MethodNode calledAsmMethod, MethodInsnNode methodInsn,
            CompilationUnit compUnit) throws IOException {

        if (isPrimitiveTypeCast(calledAsmMethod, owner)) {
            // Nothing to do if Java casts between primitive type and wrapper classes.
            return methodInsn;
        }
        if (calledAsmMethod.name.equals(HASH_CODE_METHOD_NAME)
                && owner.name.equals(getInternalName(String.class))
                && methodInsn.getNext() instanceof LookupSwitchInsnNode) {
            return handleStringSwitch(callingNeoMethod, methodInsn);
        }
        NeoMethod calledNeoMethod = new NeoMethod(calledAsmMethod, owner);
        compUnit.getNeoModule().addMethod(calledNeoMethod);
        initializeMethod(calledNeoMethod, compUnit);
        compileMethod(calledNeoMethod, compUnit);
        addReverseArguments(calledAsmMethod, callingNeoMethod);
        // The actual address offset for the method call is set at a later point in compilation.
        callingNeoMethod.addInstruction(
                new NeoInstruction(OpCode.CALL_L, new byte[4]).setExtra(calledNeoMethod));
        return methodInsn;
    }

    private static boolean isPrimitiveTypeCast(MethodNode calledAsmMethod, ClassNode owner) {
        boolean isConversionFromPrimitiveType = calledAsmMethod.name.equals(
                VALUEOF_METHOD_NAME);
        boolean isConversionToPrimitiveType =
                PRIMITIVE_TYPE_CAST_METHODS.contains(calledAsmMethod.name);
        boolean isOwnerPrimitiveTypeWrapper = PRIMITIVE_TYPE_WRAPPER_CLASSES.contains(
                owner.name);
        return (isConversionFromPrimitiveType || isConversionToPrimitiveType)
                && isOwnerPrimitiveTypeWrapper;
    }

    public static boolean isStaticFieldConverter(MethodNode methodNode, ClassNode owner) {
        return owner.name.equals(Type.getInternalName(StaticVariableHelper.class))
                && (methodNode.name.equals(ADDRESS_TO_SCRIPTHASH_METHOD_NAME)
                || methodNode.name.equals(HEX_TO_BYTES_METHOD_NAME)
                || methodNode.name.equals(STRING_TO_INT_METHOD_NAME));
    }

    private static void handleStaticFieldConverter(MethodNode methodNode,
            NeoMethod callingNeoMethod, CompilationUnit compUnit) {
        if (!callingNeoMethod.name.equals(INITSSLOT_METHOD_NAME)) {
            throw new CompilerException(compUnit, callingNeoMethod, format("The static field "
                    + "converter method %s was used outside of the static variable "
                    + "initialization scope.", methodNode.name));
        }

        NeoInstruction lastNeoInsn = callingNeoMethod.getLastInstruction();
        if (!lastNeoInsn.opcode.equals(OpCode.PUSHDATA1)
                && !lastNeoInsn.opcode.equals(OpCode.PUSHDATA2)
                && !lastNeoInsn.opcode.equals(OpCode.PUSHDATA4)) {
            throw new CompilerException(compUnit, callingNeoMethod, "Static field converter "
                    + "methods can only be applied to constant string literals.");
        }
        int pushDataPrefixSize = getOperandSize(lastNeoInsn.opcode).prefixSize();
        String stringLiteral = new String(ArrayUtils.getLastNBytes(lastNeoInsn.operand,
                lastNeoInsn.operand.length - pushDataPrefixSize), UTF_8);
        byte[] newInsnBytes = null;

        if (methodNode.name.equals(ADDRESS_TO_SCRIPTHASH_METHOD_NAME)) {
            if (!AddressUtils.isValidAddress(stringLiteral)) {
                throw new CompilerException(compUnit, callingNeoMethod, format("Invalid address "
                        + "(\"%s\") used in static field initialization.", stringLiteral));
            }
            byte[] scriptHash = AddressUtils.addressToScriptHash(stringLiteral);
            newInsnBytes = new ScriptBuilder().pushData(scriptHash).toArray();

        } else if (methodNode.name.equals(HEX_TO_BYTES_METHOD_NAME)) {
            if (!Numeric.isValidHexString(stringLiteral)) {
                throw new CompilerException(compUnit, callingNeoMethod, format("Invalid hex "
                                + "string (\"%s\") used in static field initialization.",
                        stringLiteral));
            }
            byte[] bytes = Numeric.hexStringToByteArray(stringLiteral);
            newInsnBytes = new ScriptBuilder().pushData(bytes).toArray();

        } else if (methodNode.name.equals(STRING_TO_INT_METHOD_NAME)) {
            try {
                newInsnBytes = new ScriptBuilder().pushInteger(new BigInteger(stringLiteral))
                        .toArray();
            } catch (NumberFormatException e) {
                throw new CompilerException(compUnit, callingNeoMethod, format("Invalid number "
                                + "string (\"%s\") used in static field initialization.",
                        stringLiteral));
            }
        }
        byte[] newOperand = Arrays.copyOfRange(newInsnBytes, 1, newInsnBytes.length);
        lastNeoInsn.setOpcode(OpCode.get(newInsnBytes[0]));
        lastNeoInsn.setOperand(newOperand);
    }

    public static boolean hasSyscallAnnotation(MethodNode asmMethod) {
        return hasAnnotations(asmMethod, Syscalls.class, Syscall.class);
    }

    /**
     * Checks if the given class node carries the {@link Contract} annotation.
     */
    private static boolean isContractCall(ClassNode owner) {
        return owner.invisibleAnnotations != null && owner.invisibleAnnotations.stream().
                anyMatch(a -> a.desc.equals(Type.getDescriptor(Contract.class)));
    }

    private static boolean hasInstructionAnnotation(MethodNode asmMethod) {
        return hasAnnotations(asmMethod, Instructions.class, Instruction.class);
    }

    // Converts the instructions of a string switch from JVM bytecode to NeoVM code. The
    // `MethodInsnNode` is expected represent a call to the `String.hashCode()` method.
    private static AbstractInsnNode handleStringSwitch(NeoMethod callingNeoMethod,
            MethodInsnNode methodInsn) {
        // Before the call to `hashCode()` there the opcodes ICONST_M1, ISTORE,
        // and ALOAD occured. The compiler already converted them and added them to the
        // `NeoMethod` at this point. But they are not needed when converting the switch to
        // NeoVM code. Thus, they must to be removed again.
        callingNeoMethod.removeLastInstruction();
        callingNeoMethod.removeLastInstruction();
        callingNeoMethod.removeLastInstruction();
        // TODO: Besides removing the last few instructions we could also clean up the variable
        //  initialization (in INTISLOT).
        LookupSwitchInsnNode lookupSwitchInsn = (LookupSwitchInsnNode) methodInsn.getNext();
        AbstractInsnNode insn = lookupSwitchInsn.getNext();
        // The TABLESWITCH instruction will be needed several times in the following.
        TableSwitchInsnNode tableSwitchInsn = (TableSwitchInsnNode)
                skipToInstructionType(insn, AbstractInsnNode.TABLESWITCH_INSN);
        for (int i = 0; i < lookupSwitchInsn.labels.size(); i++) {
            // First, instruction in each `case` loads the string var that is evaluated.
            insn = skipToInstructionType(insn, AbstractInsnNode.VAR_INSN);
            addLoadLocalVariable(((VarInsnNode) insn).var, callingNeoMethod);
            // Next, the constant string to compare with is loaded.
            insn = insn.getNext();
            addLoadConstant(insn, callingNeoMethod);
            // Next, the method call to String.equals() follows.
            insn = insn.getNext();
            assert isStringEqualsMethodCall(insn);
            // Next is the jump instruction for the inequality case. We ignore this because
            // it will be the last instruction after all `cases` have been passed.
            JumpInsnNode jumpInsn = (JumpInsnNode) insn.getNext();
            assert jumpInsn.getOpcode() == JVMOpcode.IFEQ.getOpcode();
            // Next, follow instructions for the equality case. We retrieve the number that
            // points us to the correct case in the TABLESWITCH instruction following later.
            InsnNode branchNumberInsn = (InsnNode) jumpInsn.getNext();
            int branchNr = branchNumberInsn.getOpcode() - 3;
            // The branch number gets stored to a local variable in the next opcode. But we
            // can ignore that.
            insn = branchNumberInsn.getNext();
            assert insn.getType() == AbstractInsnNode.VAR_INSN;
            // The next instruction opcode jumps to the TABLESWITCH instruction but we need
            // to replace this with a jump directly to the correct branch after the TABLESWITCH.
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.EQUAL));
            callingNeoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMPIF_L,
                    tableSwitchInsn.labels.get(branchNr).getLabel()));
        }
        callingNeoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMP_L,
                tableSwitchInsn.dflt.getLabel()));
        return tableSwitchInsn;
    }

    // Checks if the given instruction is a method call to the `String.equals()` method.
    private static boolean isStringEqualsMethodCall(AbstractInsnNode insn) {
        if (insn.getType() == AbstractInsnNode.METHOD_INSN
                && insn.getOpcode() == JVMOpcode.INVOKEVIRTUAL.getOpcode()) {

            MethodInsnNode equalsCallInsn = (MethodInsnNode) insn;
            return equalsCallInsn.name.equals(EQUALS_METHOD_NAME)
                    && equalsCallInsn.owner.equals(getInternalName(String.class));
        }
        return false;
    }

    private static void addContractCall(MethodNode calledAsmMethod, NeoMethod callingNeoMethod,
            ClassNode owner) {

        AnnotationNode annotation = owner.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Contract.class))).findFirst().get();
        byte[] scriptHash = Numeric.hexStringToByteArray((String) annotation.values.get(1));
        if (scriptHash.length != NeoConstants.SCRIPTHASH_SIZE) {
            throw new CompilerException("Script hash on contract class '"
                    + getClassNameForInternalName(owner.name) + "' does not have the correct "
                    + "length.");
        }

        int nrOfParams = Type.getType(calledAsmMethod.desc).getArgumentTypes().length;
        addPushNumber(nrOfParams, callingNeoMethod);
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.PACK));
        addPushDataArray(calledAsmMethod.name, callingNeoMethod);
        addPushDataArray(ArrayUtils.reverseArray(scriptHash), callingNeoMethod);
        byte[] contractSyscall = Numeric.hexStringToByteArray(SYSTEM_CONTRACT_CALL.getHash());
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.SYSCALL, contractSyscall));

        // If the return type is void, insert a DROP.
        String returnType = Type.getMethodType(calledAsmMethod.desc).getReturnType().getClassName();
        if (returnType.equals(void.class.getTypeName())
                || returnType.equals(Void.class.getTypeName())) {
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.DROP));
        }
    }

    private static AbstractInsnNode skipToInstructionType(AbstractInsnNode insn, int type) {
        while (insn.getNext() != null) {
            insn = insn.getNext();
            if (insn.getType() == type) {
                return insn;
            }
        }
        throw new CompilerException("Couldn't find node of type " + type);
    }
}
