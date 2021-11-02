package io.neow3j.compiler.converters;

import io.neow3j.compiler.AsmHelper;
import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.CompilerException;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoJumpInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.contract.NefFile.MethodToken;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Instruction.Instructions;
import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.script.OpCode;
import io.neow3j.types.CallFlags;
import io.neow3j.types.Hash160;
import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.ClassUtils;
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

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.neow3j.compiler.AsmHelper.getAsmClassForInternalName;
import static io.neow3j.compiler.AsmHelper.getMethodNode;
import static io.neow3j.compiler.AsmHelper.hasAnnotations;
import static io.neow3j.compiler.Compiler.addLoadConstant;
import static io.neow3j.compiler.Compiler.addReverseArguments;
import static io.neow3j.compiler.Compiler.buildPushDataInsn;
import static io.neow3j.compiler.Compiler.buildPushNumberInstruction;
import static io.neow3j.compiler.Compiler.processInstructionAnnotations;
import static io.neow3j.compiler.LocalVariableHelper.addLoadLocalVariable;
import static io.neow3j.script.OpCode.CALLT;
import static io.neow3j.utils.AddressUtils.addressToScriptHash;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.ClassUtils.getClassNameForInternalName;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.isValidHexString;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.objectweb.asm.Type.getInternalName;

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
                insn = handleInvoke(insn, neoMethod, compUnit);
                break;
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC:
                throw new CompilerException(neoMethod, format("JVM opcode %s is not supported.",
                        opcode.name()));
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
    private static final String EQUALS_METHOD_NAME = "equals";
    private static final String LENGTH_METHOD_NAME = "length";
    private static final String GET_CONTRACT_HASH_METHOD_NAME = "getHash";
    private static final String THROWABLE_GET_MESSAGE = "getMessage";


    /**
     * Handles all INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC instructions. Note that constructor
     * calls (INVOKESPECIAL) are handled in the {@link ObjectsConverter}
     *
     * @param insn             The instruction to handle.
     * @param callingNeoMethod The method in which the invoke happens.
     * @param compUnit         The {@code CompilationUnit}.
     * @return the instruction that should be processed next.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public static AbstractInsnNode handleInvoke(AbstractInsnNode insn,
            NeoMethod callingNeoMethod, CompilationUnit compUnit) throws IOException {

        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        ClassNode ownerClass = getAsmClassForInternalName(methodInsn.owner,
                compUnit.getClassLoader());
        Optional<MethodNode> calledAsmMethod = getMethodNode(methodInsn, ownerClass);
        // If the called method cannot be found on the owner type, we look through the super types
        // until we find the method.
        ClassNode topLevelOwnerClass = ownerClass;
        while (!calledAsmMethod.isPresent()) {
            if (ownerClass.superName == null) {
                throw new CompilerException(callingNeoMethod, format("Couldn't find method '%s' "
                                + "on its owner class %s and its super classes.", methodInsn.name,
                        getFullyQualifiedNameForInternalName(ownerClass.name)));
            }
            ownerClass = getAsmClassForInternalName(ownerClass.superName,
                    compUnit.getClassLoader());
            calledAsmMethod = getMethodNode(methodInsn, ownerClass);
        }
        if (hasAnnotations(calledAsmMethod.get(), Instruction.class, Instructions.class)) {
            processInstructionAnnotations(calledAsmMethod.get(), callingNeoMethod);
        } else if (isContractCall(topLevelOwnerClass, compUnit)) {
            addContractCall(calledAsmMethod.get(), callingNeoMethod, topLevelOwnerClass, compUnit);
        } else if (isStringLiteralConverter(calledAsmMethod.get(), ownerClass)) {
            handleStringLiteralsConverter(calledAsmMethod.get(), callingNeoMethod);
        } else if (isStringEqualsMethodCall(methodInsn)) {
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.EQUAL));
        } else {
            return handleMethodCall(callingNeoMethod, ownerClass, calledAsmMethod.get(), methodInsn,
                    compUnit);
        }
        return insn;
    }

    private static AbstractInsnNode handleMethodCall(NeoMethod callingNeoMethod, ClassNode owner,
            MethodNode calledAsmMethod, MethodInsnNode methodInsn, CompilationUnit compUnit)
            throws IOException {

        String calledMethodId = NeoMethod.getMethodId(calledAsmMethod, owner);
        if (compUnit.getNeoModule().hasMethod(calledMethodId)) {
            // If the module already compiled the method simply add a CALL instruction.
            NeoMethod calledNeoMethod = compUnit.getNeoModule().getMethod(calledMethodId);
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
        if (isStringSwitch(owner, calledAsmMethod, methodInsn)) {
            return handleStringSwitch(callingNeoMethod, methodInsn);
        }
        if (isStringLengthCall(methodInsn)) {
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.SIZE));
            return methodInsn;
        }
        if (isThrowableGetMessage(owner, methodInsn)) {
            return methodInsn;
        }
        NeoMethod calledNeoMethod = new NeoMethod(calledAsmMethod, owner);
        compUnit.getNeoModule().addMethod(calledNeoMethod);
        calledNeoMethod.initialize(compUnit);
        calledNeoMethod.convert(compUnit);
        addReverseArguments(calledAsmMethod, callingNeoMethod);
        // The actual address offset for the method call is set at a later point in compilation.
        callingNeoMethod.addInstruction(
                new NeoInstruction(OpCode.CALL_L, new byte[4]).setExtra(calledNeoMethod));
        return methodInsn;
    }

    private static boolean isThrowableGetMessage(ClassNode owner, MethodInsnNode methodInsn) {
        return getFullyQualifiedNameForInternalName(owner.name).equals(
                Throwable.class.getCanonicalName()) &&
                methodInsn.name.equals(THROWABLE_GET_MESSAGE);
    }

    private static boolean isStringLengthCall(MethodInsnNode methodInsn) {
        return methodInsn.owner.equals(Type.getInternalName(String.class))
                && methodInsn.name.equals(LENGTH_METHOD_NAME);
    }

    private static boolean isStringSwitch(ClassNode owner, MethodNode calledAsmMethod,
            MethodInsnNode methodInsn) {
        return calledAsmMethod.name.equals(HASH_CODE_METHOD_NAME)
                && owner.name.equals(getInternalName(String.class))
                && methodInsn.getNext() instanceof LookupSwitchInsnNode;
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

    public static boolean isStringLiteralConverter(MethodNode methodNode, ClassNode owner) {
        return owner.name.equals(Type.getInternalName(StringLiteralHelper.class))
                && (methodNode.name.equals(ADDRESS_TO_SCRIPTHASH_METHOD_NAME)
                || methodNode.name.equals(HEX_TO_BYTES_METHOD_NAME)
                || methodNode.name.equals(STRING_TO_INT_METHOD_NAME));
    }

    private static void handleStringLiteralsConverter(MethodNode methodNode,
            NeoMethod callingNeoMethod) {

        NeoInstruction lastNeoInsn = callingNeoMethod.getLastInstruction();
        if (!lastNeoInsn.getOpcode().equals(OpCode.PUSHDATA1)
                && !lastNeoInsn.getOpcode().equals(OpCode.PUSHDATA2)
                && !lastNeoInsn.getOpcode().equals(OpCode.PUSHDATA4)) {
            throw new CompilerException(callingNeoMethod, "Static field converter "
                    + "methods can only be applied to constant string literals.");
        }
        String stringLiteral = new String(lastNeoInsn.getOperand(), UTF_8);
        NeoInstruction newInsn = null;
        if (methodNode.name.equals(ADDRESS_TO_SCRIPTHASH_METHOD_NAME)) {
            if (!AddressUtils.isValidAddress(stringLiteral)) {
                throw new CompilerException(callingNeoMethod, format("Invalid address "
                        + "'%s' used in static field initialization.", stringLiteral));
            }
            byte[] scriptHash = addressToScriptHash(stringLiteral);
            newInsn = buildPushDataInsn(reverseArray(scriptHash));
        } else if (methodNode.name.equals(HEX_TO_BYTES_METHOD_NAME)) {
            if (!isValidHexString(stringLiteral)) {
                throw new CompilerException(callingNeoMethod, format("Invalid hex string ('%s') "
                        + "used in static field initialization.", stringLiteral));
            }
            byte[] bytes = hexStringToByteArray(stringLiteral);
            newInsn = buildPushDataInsn(bytes);
        } else if (methodNode.name.equals(STRING_TO_INT_METHOD_NAME)) {
            try {
                newInsn = buildPushNumberInstruction(new BigInteger(stringLiteral));
            } catch (NumberFormatException e) {
                throw new CompilerException(callingNeoMethod, format("Invalid number string ('%s') "
                        + "used in static field initialization.", stringLiteral));
            }
        }
        callingNeoMethod.replaceLastInstruction(newInsn);
    }

    /**
     * Checks if the given class node is a ContractInterface.
     */
    private static boolean isContractCall(ClassNode owner, CompilationUnit compUnit)
            throws IOException {

        boolean hasContractHash = hasAnnotations(owner, ContractHash.class);
        boolean isContractInterface = false;
        ClassNode clazz = owner;
        while (clazz.superName != null) {
            if (clazz.superName.equals(Type.getType(ContractInterface.class).getInternalName())) {
                isContractInterface = true;
                break;
            }
            clazz = getAsmClassForInternalName(clazz.superName, compUnit.getClassLoader());
        }
        if (hasContractHash && !isContractInterface) {
            throw new CompilerException(format("The class '%s' annotated with '%s' needs to " +
                            "extend '%s' to be usable.", owner.name,
                    ContractHash.class.getSimpleName(), ContractInterface.class.getSimpleName()));
        }
        if (isContractInterface && !hasContractHash) {
            throw new CompilerException(format("Contract interface '%s' needs to be annotated " +
                            "with the '%s' annotation to be usable.",
                    ClassUtils.getClassNameForInternalName(owner.name),
                    ContractHash.class.getSimpleName()));
        }
        return hasContractHash;
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
        // NeoVM code. Thus, they must be removed again.
        callingNeoMethod.removeLastInstruction();
        callingNeoMethod.removeLastInstruction();
        callingNeoMethod.removeLastInstruction();
        // TODO: Besides removing the last few instructions we could also clean up the variable
        //  initialization (in INTISLOT).
        LookupSwitchInsnNode lookupSwitchInsn = (LookupSwitchInsnNode) methodInsn.getNext();
        AbstractInsnNode insn = lookupSwitchInsn.getNext();

        // The compiler inserts a second switch instruction after the first lookup switch. This
        // second switch instruction is a tableswitch if there are three or more cases, and a
        // lookupswitch if there are less than three cases.
        TableSwitchInsnNode tableSwitchInsn = null;
        LookupSwitchInsnNode secondLookupSwitchInsn = null;
        try {
            tableSwitchInsn = (TableSwitchInsnNode) skipToInstructionType(insn,
                    AbstractInsnNode.TABLESWITCH_INSN, callingNeoMethod);
        } catch (CompilerException e) {
            // The string switch case has only two or less cases.
            try {
                secondLookupSwitchInsn = (LookupSwitchInsnNode) skipToInstructionType(insn,
                        AbstractInsnNode.LOOKUPSWITCH_INSN, callingNeoMethod);
            } catch (CompilerException i) {
                throw new CompilerException(callingNeoMethod, "Error converting a string " +
                        "switch-case statement.");
            }
        }
        for (int i = 0; i < lookupSwitchInsn.labels.size(); i++) {
            // First, instruction in each `case` loads the string var that is evaluated.
            insn = skipToInstructionType(insn, AbstractInsnNode.VAR_INSN, callingNeoMethod);
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
            if (tableSwitchInsn != null) {
                callingNeoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMPIF_L,
                        tableSwitchInsn.labels.get(branchNr).getLabel()));
            } else {
                callingNeoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMPIF_L,
                        secondLookupSwitchInsn.labels.get(branchNr).getLabel()));
            }
        }
        if (tableSwitchInsn != null) {
            callingNeoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMP_L,
                    tableSwitchInsn.dflt.getLabel()));
            return tableSwitchInsn;
        } else {
            callingNeoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMP_L,
                    secondLookupSwitchInsn.dflt.getLabel()));
            return secondLookupSwitchInsn;
        }
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
            ClassNode owner, CompilationUnit compUnit) {

        AnnotationNode annotation = AsmHelper.getAnnotationNode(owner, ContractHash.class).get();
        Hash160 scriptHash;
        try {
            scriptHash = new Hash160((String) annotation.values.get(1));
        } catch (IllegalArgumentException e) {
            throw new CompilerException(owner, format("Script hash '%s' of the contract "
                            + "class '%s' does not have the length of a correct script hash.",
                    annotation.values.get(1), getClassNameForInternalName(owner.name)));
        }

        // If its a call to the `getHash()` method, simply add PUSHDATA <scriptHash>.
        if (calledAsmMethod.name.equals(GET_CONTRACT_HASH_METHOD_NAME)) {
            callingNeoMethod.addInstruction(
                    // The contract hash is pushed in little-endian ordering because the NeoVM
                    // also returns contract hashes in little-endian ordering.
                    buildPushDataInsn(ArrayUtils.reverseArray(scriptHash.toArray())));
            return;
        }

        int nrOfParams = Type.getType(calledAsmMethod.desc).getArgumentTypes().length;
        boolean hasReturnValue = !Type.getMethodType(calledAsmMethod.desc)
                .getReturnType().getClassName().equals(void.class.getTypeName());
        MethodToken token = new MethodToken(scriptHash, calledAsmMethod.name, nrOfParams,
                hasReturnValue, CallFlags.ALL);
        int idx = compUnit.getNeoModule().addMethodToken(token);
        byte[] idxBytes = ArrayUtils.getFirstNBytes(ByteBuffer.allocate(4).order(
                ByteOrder.LITTLE_ENDIAN).putInt(idx).array(), 2);
        addReverseArguments(callingNeoMethod, nrOfParams);
        callingNeoMethod.addInstruction(new NeoInstruction(CALLT, idxBytes));
    }

    private static AbstractInsnNode skipToInstructionType(AbstractInsnNode insn, int type,
            NeoMethod neoMethod) {
        while (insn.getNext() != null) {
            insn = insn.getNext();
            if (insn.getType() == type) {
                return insn;
            }
        }
        throw new CompilerException(neoMethod, format("Tried to skip to an instruction of type %d "
                + "but reached the end of the method.", type));
    }
}
