package io.neow3j.compiler.converters;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.compiler.CompilerException;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoEvent;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.compiler.SuperNeoMethod;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.devpack.contracts.ContractInterface;
import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Numeric;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.io.IOException;
import java.util.List;

import static io.neow3j.compiler.AsmHelper.getAsmClass;
import static io.neow3j.compiler.AsmHelper.getAsmClassForInternalName;
import static io.neow3j.compiler.AsmHelper.getMethodNode;
import static io.neow3j.compiler.AsmHelper.hasAnnotations;
import static io.neow3j.compiler.Compiler.addPushNumber;
import static io.neow3j.compiler.Compiler.addReverseArguments;
import static io.neow3j.compiler.Compiler.buildPushDataInsn;
import static io.neow3j.compiler.Compiler.handleInsn;
import static io.neow3j.compiler.Compiler.isAssertionDisabledStaticField;
import static io.neow3j.compiler.Compiler.isCallToCtor;
import static io.neow3j.compiler.Compiler.isEvent;
import static io.neow3j.compiler.Compiler.processInstructionAnnotations;
import static io.neow3j.compiler.Compiler.skipToCtorCall;
import static io.neow3j.compiler.Compiler.skipToSuperCtorCall;
import static io.neow3j.compiler.LocalVariableHelper.buildStoreOrLoadVariableInsn;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.ClassUtils.getClassNameForInternalName;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.objectweb.asm.Type.getInternalName;

public class ObjectsConverter implements Converter {

    private static final String APPEND_METHOD_NAME = "append";
    private static final String TOSTRING_METHOD_NAME = "toString";
    private static final String VALUEOF_METHOD_NAME = "valueOf";

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (requireNonNull(opcode)) {
            case PUTSTATIC:
                addStoreStaticField((FieldInsnNode) insn, neoMethod, compUnit);
                break;
            case GETSTATIC:
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                if (isEvent(fieldInsn.desc, compUnit)) {
                    insn = convertEvent(fieldInsn, neoMethod, compUnit);
                } else if (isAssertionDisabledStaticField(fieldInsn)) {
                    insn = fieldInsn.getNext();
                } else {
                    addLoadStaticField(fieldInsn, neoMethod, compUnit);
                }
                break;
            case CHECKCAST:
                // Check if the object on the operand stack can be cast to a given type.
                // There is no corresponding NeoVM opcode.
                break;
            case NEW:
                TypeInsnNode typeInsn = (TypeInsnNode) insn;
                if (isStruct(typeInsn.desc, compUnit)) {
                    insn = handleNewStruct(insn, neoMethod, compUnit);
                } else if (isContractInterface(typeInsn.desc, compUnit)) {
                    insn = handleContractInterfaceCtor(insn, neoMethod, compUnit);
                } else {
                    insn = handleNew(insn, neoMethod, compUnit);
                }
                break;
            case ARRAYLENGTH:
                neoMethod.addInstruction(new NeoInstruction(OpCode.SIZE));
                break;
            case INSTANCEOF:
                handleInstanceOf((TypeInsnNode) insn, neoMethod);
                break;
        }
        return insn;
    }

    private AbstractInsnNode handleContractInterfaceCtor(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        TypeInsnNode typeInsn = (TypeInsnNode) insn;
        assert typeInsn.getNext().getOpcode() == JVMOpcode.DUP.getOpcode() :
                "Expected DUP after NEW but got other instructions";

        ClassNode ownerClassNode = getAsmClassForInternalName(typeInsn.desc, compUnit.getClassLoader());
        insn = typeInsn.getNext().getNext();
        while (!isCallToCtor(insn, ownerClassNode.name)) {
            insn = handleInsn(insn, neoMethod, compUnit);
            insn = insn.getNext();
        }
        MethodNode methodNode = getMethodNode((MethodInsnNode) insn, ownerClassNode).get();
        Type[] cTorArgTypes = Type.getType(methodNode.desc).getArgumentTypes();
        int cTorParamLength = cTorArgTypes.length;
        if (cTorParamLength == 0) {
            return handleConstantContractHash(neoMethod, ownerClassNode, insn);
        }
        if (cTorParamLength == 1) {
            if (getInternalName(Hash160.class).equals(cTorArgTypes[0].getInternalName())) {
                // The instructions that lead to the contract hash are already on the stack at this point.
                return insn;
            } else if (getInternalName(String.class).equals(cTorArgTypes[0].getInternalName())) {
                NeoInstruction lastInstruction = neoMethod.getLastInstruction();
                handleConstantScriptHash(neoMethod, lastInstruction);
                return insn;
            }
        }
        throw new CompilerException(
                format("Contract interface classes can only be initialized with a %s type or a constant %s.",
                        Hash160.class.getSimpleName(), String.class.getSimpleName()));
    }

    private void handleConstantScriptHash(NeoMethod neoMethod, NeoInstruction lastInstruction) {
        if (lastInstruction.getOpcode().equals(OpCode.PUSHDATA1)) {
            io.neow3j.types.Hash160 scriptHash = new io.neow3j.types.Hash160(new String(lastInstruction.getOperand()));
            neoMethod.replaceLastInstruction(buildPushDataInsn(reverseArray(scriptHash.toArray())));
        } else {
            throw new CompilerException(
                    format("Contract interface classes can only be initialized with a %s type or a constant %s. " +
                                    "Expected opcode '%s' on the stack but found '%s'.",
                            Hash160.class.getSimpleName(), String.class.getSimpleName(), OpCode.PUSHDATA1,
                            lastInstruction.getOpcode()));
        }
    }

    // Expects the ctor to exactly contain only a super call with a constant string script hash value.
    private AbstractInsnNode handleConstantContractHash(NeoMethod neoMethod, ClassNode ownerClassNode,
            AbstractInsnNode ctorInsn) {

        MethodNode methodNode = getMethodNode((MethodInsnNode) ctorInsn, ownerClassNode).orElseThrow(
                () -> new CompilerException("Could not get method node from ctor instruction."));
        AbstractInsnNode abstractInsnNode = methodNode.instructions.get(3);
        if (abstractInsnNode.getType() != AbstractInsnNode.LDC_INSN) {
            throw new CompilerException(format("Expected a different node instruction type. Expected %s, but was %s.",
                    AbstractInsnNode.LDC_INSN, abstractInsnNode.getType()));
        }
        LdcInsnNode cstNode = (LdcInsnNode) abstractInsnNode;
        io.neow3j.types.Hash160 scriptHash = new io.neow3j.types.Hash160((String) cstNode.cst);
        neoMethod.addInstruction(buildPushDataInsn(reverseArray(scriptHash.toArray())));
        return ctorInsn;
    }

    // Whether the type extends the abstract class ContractInterface.
    private boolean isContractInterface(String desc, CompilationUnit compUnit) throws IOException {
        ClassNode ownerClassNode = getAsmClassForInternalName(desc, compUnit.getClassLoader());
        String superName = getFullyQualifiedNameForInternalName(ownerClassNode.superName);
        while (!superName.equals(Object.class.getCanonicalName())) {
            if (superName.equals(ContractInterface.class.getCanonicalName())) {
                return true;
            }
            ownerClassNode = getAsmClassForInternalName(superName, compUnit.getClassLoader());
            superName = getFullyQualifiedNameForInternalName(ownerClassNode.superName);
        }
        return false;
    }

    private boolean isStruct(String desc, CompilationUnit compUnit) throws IOException {
        ClassNode asmClass = getAsmClass(getFullyQualifiedNameForInternalName(desc), compUnit.getClassLoader());
        return hasAnnotations(asmClass, Struct.class);
    }

    private void handleInstanceOf(TypeInsnNode typeInsn, NeoMethod neoMethod) {
        Type type = Type.getObjectType(typeInsn.desc);
        StackItemType stackItemType = Compiler.mapTypeToStackItemType(type);
        if (stackItemType.equals(StackItemType.ANY)) {
            throw new CompilerException(neoMethod, format("The type '%s' is not supported for the instanceof " +
                    "operation.", getFullyQualifiedNameForInternalName(type.getInternalName())));
        }
        neoMethod.addInstruction(new NeoInstruction(OpCode.ISTYPE, new byte[]{stackItemType.byteValue()}));
    }

    public static void addLoadStaticField(FieldInsnNode fieldInsn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {
        int neoVmIdx = compUnit.getNeoModule().getContractVariable(fieldInsn, compUnit).getNeoIdx();
        neoMethod.addInstruction(buildStoreOrLoadVariableInsn(neoVmIdx, OpCode.LDSFLD));
    }

    public static void addStoreStaticField(FieldInsnNode fieldInsn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {
        int neoVmIdx = compUnit.getNeoModule().getContractVariable(fieldInsn, compUnit).getNeoIdx();
        neoMethod.addInstruction(buildStoreOrLoadVariableInsn(neoVmIdx, OpCode.STSFLD));
    }

    public static AbstractInsnNode handleNewStruct(AbstractInsnNode insn, NeoMethod callingNeoMethod,
            CompilationUnit compUnit) throws IOException {

        TypeInsnNode typeInsn = (TypeInsnNode) insn;
        assert typeInsn.getNext().getOpcode() == JVMOpcode.DUP.getOpcode() :
                "Expected DUP after NEW but got other instructions";

        ClassNode ownerClassNode = getAsmClassForInternalName(typeInsn.desc, compUnit.getClassLoader());
        MethodInsnNode ctorMethodInsn = skipToCtorCall(typeInsn.getNext(), ownerClassNode);
        MethodNode ctorMethod = getMethodNode(ctorMethodInsn, ownerClassNode)
                .orElseThrow(() -> new CompilerException(callingNeoMethod,
                        format("Couldn't find constructor '%s' on class '%s'.", ctorMethodInsn.name,
                                getClassNameForInternalName(ownerClassNode.name))));

        // Annotations in struct constructors are ignored
        if (ctorMethod.invisibleAnnotations == null || ctorMethod.invisibleAnnotations.size() == 0) {
            // It's a generic struct constructor without any Neo-specific annotations.
            return convertStructConstructorCall(typeInsn, ctorMethod, ownerClassNode, callingNeoMethod, compUnit);
        } else {
            // The constructor has some Neo-specific annotation.
            // skip NEW and DUP.
            insn = insn.getNext().getNext();
            // Process possible arguments passed to INVOKESPECIAL.
            while (!isCallToCtor(insn, ownerClassNode.name)) {
                insn = handleInsn(insn, callingNeoMethod, compUnit);
                insn = insn.getNext();
            }
            // Now we're at the INVOKESPECIAL call and can convert the ctor method.
            if (hasAnnotations(ctorMethod, Instruction.class, Instruction.Instructions.class)) {
                processInstructionAnnotations(ctorMethod, callingNeoMethod);
            }
            return insn;
        }
    }

    public static AbstractInsnNode handleNew(AbstractInsnNode insn, NeoMethod callingNeoMethod,
            CompilationUnit compUnit) throws IOException {

        TypeInsnNode typeInsn = (TypeInsnNode) insn;
        assert typeInsn.getNext().getOpcode() == JVMOpcode.DUP.getOpcode() :
                "Expected DUP after NEW but got other instructions";

        if (isNewStringBuilder(typeInsn)) {
            // Java, in the background, performs String concatenation, like `s1 + s2`, with the instantiation of a
            // StringBuilder. This is handled here.
            return handleStringConcatenation(typeInsn, callingNeoMethod, compUnit);
        }

        if (isNewHash160FromString(typeInsn, compUnit)) {
            return handleNewHash160FromString(typeInsn, callingNeoMethod);
        }

        if (isNewHash256FromString(typeInsn, compUnit)) {
            return handleNewHash256FromString(typeInsn, callingNeoMethod);
        }

        if (isNewECPointFromString(typeInsn, compUnit)) {
            return handleNewECPointFromString(typeInsn, callingNeoMethod);
        }

        if (isAssertion(typeInsn, compUnit)) {
            return handleAssertion(typeInsn, callingNeoMethod);
        }

        if (isNewException(typeInsn, compUnit)) {
            return handleNewException(typeInsn, callingNeoMethod, compUnit);
        }

        ClassNode owner = getAsmClassForInternalName(typeInsn.desc, compUnit.getClassLoader());
        MethodInsnNode ctorMethodInsn = skipToCtorCall(typeInsn.getNext(), owner);
        MethodNode ctorMethod = getMethodNode(ctorMethodInsn, owner)
                .orElseThrow(() -> new CompilerException(callingNeoMethod,
                        format("Couldn't find constructor '%s' on class '%s'.", ctorMethodInsn.name,
                                getClassNameForInternalName(owner.name))));

        if (ctorMethod.invisibleAnnotations == null || ctorMethod.invisibleAnnotations.size() == 0) {
            // It's a generic constructor without any Neo-specific annotations.
            return convertConstructorCall(typeInsn, ctorMethod, owner, callingNeoMethod, compUnit);
        } else { // The constructor has some Neo-specific annotation.
            // skip NEW and DUP.
            insn = insn.getNext().getNext();
            // Process possible arguments passed to INVOKESPECIAL.
            while (!isCallToCtor(insn, owner.name)) {
                insn = handleInsn(insn, callingNeoMethod, compUnit);
                insn = insn.getNext();
            }
            // Now we're at the INVOKESPECIAL call and can convert the ctor method.
            if (hasAnnotations(ctorMethod, Instruction.class, Instruction.Instructions.class)) {
                processInstructionAnnotations(ctorMethod, callingNeoMethod);
            }
            return insn;
        }
    }

    private static boolean isNewHash160FromString(TypeInsnNode typeInsn, CompilationUnit compUnit) throws IOException {
        return typeInsn.desc.equals(getInternalName(Hash160.class)) && hasSingleStringArgument(typeInsn, compUnit);
    }

    private static boolean isNewHash256FromString(TypeInsnNode typeInsn, CompilationUnit compUnit) throws IOException {
        return typeInsn.desc.equals(getInternalName(Hash256.class)) && hasSingleStringArgument(typeInsn, compUnit);
    }

    private static boolean isNewECPointFromString(TypeInsnNode typeInsn, CompilationUnit compUnit) throws IOException {
        return typeInsn.desc.equals(getInternalName(ECPoint.class)) && hasSingleStringArgument(typeInsn, compUnit);
    }

    private static boolean hasSingleStringArgument(TypeInsnNode typeInsn, CompilationUnit compUnit) throws IOException {
        ClassNode ownerClassNode = getAsmClassForInternalName(typeInsn.desc, compUnit.getClassLoader());
        AbstractInsnNode insn = typeInsn.getNext();

        while (!isCallToCtor(insn, ownerClassNode.name)) {
            insn = insn.getNext();
        }
        Type[] argTypes = Type.getArgumentTypes(((MethodInsnNode) insn).desc);
        if (argTypes.length != 1) {
            return false;
        }
        return getInternalName(String.class).equals(argTypes[0].getInternalName());
    }

    private static AbstractInsnNode handleNewHash160FromString(TypeInsnNode typeInsn, NeoMethod callingNeoMethod) {
        LdcInsnNode insn = checkForConstantStringArgument(typeInsn);
        io.neow3j.types.Hash160 scriptHash = new io.neow3j.types.Hash160((String) insn.cst);
        callingNeoMethod.addInstruction(buildPushDataInsn(reverseArray(scriptHash.toArray())));
        return insn.getNext();
    }

    private static AbstractInsnNode handleNewHash256FromString(TypeInsnNode typeInsn, NeoMethod callingNeoMethod) {
        LdcInsnNode insn = checkForConstantStringArgument(typeInsn);
        io.neow3j.types.Hash256 hash256 = new io.neow3j.types.Hash256((String) insn.cst);
        callingNeoMethod.addInstruction(buildPushDataInsn(reverseArray(hash256.toArray())));
        return insn.getNext();
    }

    private static AbstractInsnNode handleNewECPointFromString(TypeInsnNode typeInsn, NeoMethod callingNeoMethod) {
        LdcInsnNode insn = checkForConstantStringArgument(typeInsn);
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey((String) insn.cst);
        callingNeoMethod.addInstruction(buildPushDataInsn(pubKey.toArray()));
        return insn.getNext();
    }

    private static LdcInsnNode checkForConstantStringArgument(TypeInsnNode typeInsn) {
        assert typeInsn.getNext().getOpcode() == JVMOpcode.DUP.getOpcode() :
                "Expected DUP after NEW but got other instructions";

        AbstractInsnNode insn = typeInsn.getNext().getNext();
        if (insn.getType() != AbstractInsnNode.LDC_INSN || !(((LdcInsnNode) insn).cst instanceof String)) {
            throw new CompilerException("Hash160, Hash256, and ECPoint constructors with a string argument can only " +
                    "be used with constant string literals.");
        }
        return (LdcInsnNode) insn;
    }

    private static boolean isNewStringBuilder(TypeInsnNode typeInsn) {
        return typeInsn.desc.equals(getInternalName(StringBuilder.class));
    }

    private static boolean isAssertion(TypeInsnNode typeInsn, CompilationUnit compUnit) throws IOException {
        ClassNode type = getAsmClassForInternalName(typeInsn.desc, compUnit.getClassLoader());
        if (getFullyQualifiedNameForInternalName(type.name).equals(AssertionError.class.getCanonicalName())) {
            return true;
        }
        while (type.superName != null) {
            type = getAsmClassForInternalName(type.superName, compUnit.getClassLoader());
            if (getFullyQualifiedNameForInternalName(type.name).equals(AssertionError.class.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNewException(TypeInsnNode typeInsn, CompilationUnit compUnit) throws IOException {
        ClassNode type = getAsmClassForInternalName(typeInsn.desc, compUnit.getClassLoader());
        if (getFullyQualifiedNameForInternalName(type.name).equals(Exception.class.getCanonicalName())) {
            return true;
        }
        while (type.superName != null) {
            type = getAsmClassForInternalName(type.superName, compUnit.getClassLoader());
            if (getFullyQualifiedNameForInternalName(type.name).equals(Exception.class.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    private static AbstractInsnNode handleAssertion(TypeInsnNode typeInsn, NeoMethod callingNeoMethod) {
        convertJumpConditionBeforeAssertion(callingNeoMethod);

        AbstractInsnNode insn = typeInsn.getNext().getNext();
        while (!isCallToCtor(insn, Type.getType(AssertionError.class).getInternalName())) {
            // Instructions between the type instruction and <init> method of the AssertionError can be ignored.
            insn = insn.getNext();
        }

        Type[] argTypes = Type.getType(((MethodInsnNode) insn).desc).getArgumentTypes();
        if (argTypes.length != 0) {
            throw new CompilerException("Passing a message with the 'assert' statement is not supported.");
        }
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.ASSERT));
        return insn.getNext(); // Skip the throw instruction.
    }

    private static void convertJumpConditionBeforeAssertion(NeoMethod neoMethod) {
        // The JVM assert conditions are jump instructions to jump over the <init> instruction of AssertionError and
        // potential additional instructions (e.g., a message). For the NeoVM ASSERT opcode, these jump instructions
        // are in the following transpiled into corresponding NeoVM opcodes that just return 0 or 1.
        if (neoMethod.getInstructions().size() == 0) {
            throw new CompilerException(format("The method '%s' seems to hold a hard coded 'assert false' statement " +
                    "or it throws an 'AssertionError'. The compiler does not support that. Use 'Helper.abort()' " +
                    "instead.", neoMethod.getName()));
        }
        NeoInstruction lastInstruction = neoMethod.getLastInstruction();
        neoMethod.removeLastInstruction();
        switch (lastInstruction.getOpcode()) {
            case JMPEQ:
            case JMPEQ_L:
                neoMethod.addInstruction(new NeoInstruction(OpCode.EQUAL));
                break;
            case JMPNE:
            case JMPNE_L:
                neoMethod.addInstruction(new NeoInstruction(OpCode.NOTEQUAL));
                break;
            case JMPLT:
            case JMPLT_L:
                neoMethod.addInstruction(new NeoInstruction(OpCode.LT));
                break;
            case JMPGT:
            case JMPGT_L:
                neoMethod.addInstruction(new NeoInstruction(OpCode.GT));
                break;
            case JMPLE:
            case JMPLE_L:
                neoMethod.addInstruction(new NeoInstruction(OpCode.LE));
                break;
            case JMPGE:
            case JMPGE_L:
                neoMethod.addInstruction(new NeoInstruction(OpCode.GE));
                break;
            case JMPIFNOT:
            case JMPIFNOT_L:
                neoMethod.addInstruction(new NeoInstruction(OpCode.NOT));
                break;
            case JMPIF:
            case JMPIF_L:
                // JMPIF and JMPIF_L do not require a replacement.
                break;
            default:
                throw new CompilerException("Could not handle jump condition. The compiler does not support hard " +
                        "coded 'assert false' statements nor throwing an 'AssertionError'. Use 'Helper.abort()' " +
                        "instead.");
        }
    }

    private static AbstractInsnNode handleNewException(TypeInsnNode typeInsn, NeoMethod callingNeoMethod,
            CompilationUnit compUnit) throws IOException {

        String fullyQualifiedExceptionName = getFullyQualifiedNameForInternalName(typeInsn.desc);
        if (!fullyQualifiedExceptionName.equals(Exception.class.getCanonicalName())) {
            throw new CompilerException(callingNeoMethod,
                    format("Contract uses exception of type %s but only %s is allowed.", fullyQualifiedExceptionName,
                            Exception.class.getCanonicalName()));
        }
        // Skip to the next instruction after DUP.
        AbstractInsnNode insn = typeInsn.getNext().getNext();
        // Process any instructions that come before the INVOKESPECIAL, e.g., a PUSHDATA insn.
        while (!isCallToCtor(insn, Type.getType(Exception.class).getInternalName())) {
            insn = handleInsn(insn, callingNeoMethod, compUnit);
            insn = insn.getNext();
        }

        Type[] argTypes = Type.getType(((MethodInsnNode) insn).desc).getArgumentTypes();
        if (argTypes.length > 1) {
            throw new CompilerException(callingNeoMethod, format("An exception thrown in a contract can either take " +
                    "no arguments or a String argument. You provided %d arguments.", argTypes.length));
        } else if (argTypes.length == 1) {
            if (!getFullyQualifiedNameForInternalName(argTypes[0].getInternalName())
                    .equals(String.class.getCanonicalName())) {
                // Only string messages are allowed in exceptions.
                throw new CompilerException(callingNeoMethod, "An exception thrown in a contract can either take no " +
                        "arguments or a String argument. You provided a non-string argument.");
            }
        } else {
            // No exception message is given, thus a dummy message is added.
            String dummyMessage = "error";
            callingNeoMethod.addInstruction(buildPushDataInsn(dummyMessage));
        }
        return insn;
    }

    /**
     * Handles the concatenation of strings, as in {@code "hello" + " world"}. Java in the background uses a
     * StringBuilder for this.
     *
     * @param typeInsnNode the NEW instruction concerning the StringBuilder.
     * @return the last processed instruction.
     */
    private static AbstractInsnNode handleStringConcatenation(TypeInsnNode typeInsnNode, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        // Skip to the next instruction after DUP
        AbstractInsnNode insn = typeInsnNode.getNext().getNext();
        // Check whether the ctor StringBuilder() or StringBuilder(String str) is used.
        if (isCallToCtor(insn, getInternalName(StringBuilder.class))) {
            // The empty StringBuilder ctor is used in this concatenation: StringBuilder().
            // Skip to the next instruction after StringBuilder ctor.
            insn = insn.getNext();
            return handleStringBuilderCtorNoArg(insn, neoMethod, compUnit);
        }
        // The StringBuilder ctor with a String argument is used in this concatenation: StringBuilder(String str).
        return handleStringBuilderCtorStringArg(insn, neoMethod, compUnit);
    }

    private static AbstractInsnNode handleStringBuilderCtorStringArg(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        boolean isFirstCall = true;
        boolean isAfterStringBuilderInit = false;
        while (insn != null) {
            // Skip ctor of StringBuilder if not yet skipped.
            if (!isAfterStringBuilderInit && isCallToCtor(insn, getInternalName(StringBuilder.class))) {
                isAfterStringBuilderInit = true;
                insn = insn.getNext();
                continue;
            }
            // The first value to append might be followed by a call to String.valueOf() which should be ignored.
            if (isCallToStringValueOf(insn) && isFirstCall) {
                insn = insn.getNext();
                continue;
            }
            if (isCallToStringBuilderAppend(insn)) {
                throwIfNotStringType(insn.getPrevious());
                neoMethod.addInstruction(new NeoInstruction(OpCode.CAT));
                insn = insn.getNext();
                continue;
            }
            if (isCallToStringBuilderToString(insn)) {
                neoMethod.addInstruction(new NeoInstruction(OpCode.CONVERT,
                        new byte[]{StackItemType.BYTE_STRING.byteValue()}));
                break; // End of string concatenation.
            }
            throwIfIsCallToAnyStringBuilderMethod(insn, neoMethod);
            insn = handleInsn(insn, neoMethod, compUnit);
            insn = insn.getNext();
        }
        if (insn == null) {
            throw new CompilerException(neoMethod, "Expected to find ScriptBuilder.toString() but reached end of " +
                    "method.");
        }
        return insn;
    }

    private static AbstractInsnNode handleStringBuilderCtorNoArg(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        boolean isFirstCall = true;
        while (insn != null) {
            if (isCallToStringBuilderAppend(insn)) {
                throwIfNotStringType(insn.getPrevious());
                if (!isFirstCall) {
                    neoMethod.addInstruction(new NeoInstruction(OpCode.CAT));
                }
                isFirstCall = false;
                insn = insn.getNext();
                continue;
            }
            if (isCallToStringBuilderToString(insn)) {
                neoMethod.addInstruction(new NeoInstruction(OpCode.CONVERT,
                        new byte[]{StackItemType.BYTE_STRING.byteValue()}));
                break; // End of string concatenation.
            }
            throwIfIsCallToAnyStringBuilderMethod(insn, neoMethod);
            insn = handleInsn(insn, neoMethod, compUnit);
            insn = insn.getNext();
        }
        if (insn == null) {
            throw new CompilerException(neoMethod, "Expected to find ScriptBuilder.toString() but reached end of " +
                    "method.");
        }
        return insn;
    }

    // Ensures that the instruction (the one to append in the StringBuilder) is a string. Otherwise, throw an exception.
    private static void throwIfNotStringType(AbstractInsnNode insn) {
        int type = insn.getType();
        if (type == AbstractInsnNode.INT_INSN) {
            throw new CompilerException(
                    "String concatenation with an array or a type other than string or char is not supported.");
        } else if (type == AbstractInsnNode.METHOD_INSN &&
                !isNonArrayStringOrCharType(((MethodInsnNode) insn).desc)) {
            throw new CompilerException(
                    "String concatenation with an array or a type other than string or char is not supported.");
        } else if (type == AbstractInsnNode.FIELD_INSN &&
                !isNonArrayStringOrCharType("()" + ((FieldInsnNode) insn).desc)) {
            // The added "()" is imitating a method to retrieve a return type due to the inability of retrieving a
            // usable internal name from a non-method Type.
            throw new CompilerException(
                    "String concatenation with an array or a type other than string or char is not supported.");
        }
    }

    private static void throwIfIsCallToAnyStringBuilderMethod(AbstractInsnNode insn, NeoMethod neoMethod) {
        if (isCallToAnyStringBuilderMethod(insn)) {
            throw new CompilerException(neoMethod, format("Only 'append()' and 'toString()' are supported for " +
                    "StringBuilder, but '%s' was called", ((MethodInsnNode) insn).name));
        }
    }

    private static boolean isNonArrayStringOrCharType(String desc) {
        Type returnType = Type.getMethodType(desc).getReturnType();
        String internalReturnTypeName = returnType.getInternalName();
        return internalReturnTypeName.equals(getInternalName(String.class)) ||
                internalReturnTypeName.equals(getInternalName(Character.class)) ||
                internalReturnTypeName.equals("C"); // Primitive type char
    }

    private static boolean isCallToStringBuilderAppend(AbstractInsnNode insn) {
        return insn instanceof MethodInsnNode
                && ((MethodInsnNode) insn).owner.equals(Type.getInternalName(StringBuilder.class))
                && ((MethodInsnNode) insn).name.equals(APPEND_METHOD_NAME);
    }

    private static boolean isCallToStringBuilderToString(AbstractInsnNode insn) {
        return insn instanceof MethodInsnNode
                && ((MethodInsnNode) insn).owner.equals(Type.getInternalName(StringBuilder.class))
                && ((MethodInsnNode) insn).name.equals(TOSTRING_METHOD_NAME);
    }

    private static boolean isCallToAnyStringBuilderMethod(AbstractInsnNode insn) {
        return insn instanceof MethodInsnNode
                && ((MethodInsnNode) insn).owner.equals(Type.getInternalName(StringBuilder.class));
    }

    private static boolean isCallToStringValueOf(AbstractInsnNode insn) {
        return insn instanceof MethodInsnNode &&
                ((MethodInsnNode) insn).owner.equals(getInternalName(String.class)) &&
                ((MethodInsnNode) insn).name.equals(VALUEOF_METHOD_NAME);
    }

    private static AbstractInsnNode convertStructConstructorCall(TypeInsnNode typeInsnNode, MethodNode ctorMethod,
            ClassNode structClassNode, NeoMethod callingNeoMethod, CompilationUnit compUnit) throws IOException {

        SuperNeoMethod calledNeoMethod;
        String ctorMethodId = NeoMethod.getMethodId(ctorMethod, structClassNode);
        int fieldSize = calculateFieldSize(structClassNode, compUnit);
        if (compUnit.getNeoModule().hasMethod(ctorMethodId)) {
            // If the module already contains the converted ctor.
            calledNeoMethod = (SuperNeoMethod) compUnit.getNeoModule().getMethod(ctorMethodId);
        } else {
            // Create a new NeoMethod, i.e., convert the constructor to NeoVM code.
            calledNeoMethod = new SuperNeoMethod(ctorMethod, structClassNode);
            compUnit.getNeoModule().addMethod(calledNeoMethod);
            calledNeoMethod.initialize(compUnit);
            calledNeoMethod.convert(compUnit);
        }
        return finalizeConstructorCall(fieldSize, typeInsnNode, ctorMethod, structClassNode, callingNeoMethod,
                calledNeoMethod, compUnit);
    }

    private static AbstractInsnNode finalizeConstructorCall(int fieldSize, TypeInsnNode typeInsnNode,
            MethodNode ctorMethod, ClassNode structClassNode, NeoMethod callingNeoMethod, NeoMethod calledNeoMethod,
            CompilationUnit compUnit) throws IOException {

        addPushNumber(fieldSize, callingNeoMethod);
        callingNeoMethod.addNewArrayInstruction();
        // TODO: Determine when to use NEWSTRUCT.
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.DUP));
        // After the JVM NEW and DUP, arguments that will be given to the INVOKESPECIAL call can
        // follow. Those are handled in the following while.
        AbstractInsnNode insn = typeInsnNode.getNext().getNext();
        while (!isCallToCtor(insn, structClassNode.name)) {
            insn = handleInsn(insn, callingNeoMethod, compUnit);
            insn = insn.getNext();
        }
        // Reverse the arguments that are passed to the constructor call.
        addReverseArguments(ctorMethod, callingNeoMethod);
        // The actual address offset for the method call is set at a later point.
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.CALL_L, new byte[4]).setExtra(calledNeoMethod));
        return insn;
    }

    // Calculates the struct's field size, i.e., including its inherited fields.
    private static int calculateFieldSize(ClassNode structClassNode, CompilationUnit compUnit) throws IOException {
        int fieldSize = structClassNode.fields.size();
        ClassNode currentClassNode = structClassNode;
        while (!getFullyQualifiedNameForInternalName(currentClassNode.superName)
                .equals(Object.class.getCanonicalName())) {
            throwIfSuperIsNotStruct(currentClassNode.superName, compUnit);
            currentClassNode = getAsmClass(currentClassNode.superName, compUnit.getClassLoader());
            fieldSize += currentClassNode.fields.size();
        }
        return fieldSize;
    }

    private static void throwIfSuperIsNotStruct(String superName, CompilationUnit compUnit) throws IOException {
        ClassNode superClassNode = getAsmClass(superName, compUnit.getClassLoader());
        if (!hasAnnotations(superClassNode, Struct.class)) {
            throw new CompilerException(format("Struct classes are not allowed to inherit non-struct classes. %s was " +
                    "inherited by a struct class.", superName));
        }
    }

    private static AbstractInsnNode convertConstructorCall(TypeInsnNode typeInsn, MethodNode ctorMethod,
            ClassNode classNode, NeoMethod callingNeoMethod, CompilationUnit compUnit) throws IOException {

        NeoMethod calledNeoMethod;
        String ctorMethodId = NeoMethod.getMethodId(ctorMethod, classNode);
        if (compUnit.getNeoModule().hasMethod(ctorMethodId)) {
            // If the module already contains the converted ctor.
            calledNeoMethod = compUnit.getNeoModule().getMethod(ctorMethodId);
        } else {
            // Create a new NeoMethod, i.e., convert the constructor to NeoVM code.
            // Skip the call to the Object ctor and continue processing the rest of the ctor.
            calledNeoMethod = new NeoMethod(ctorMethod, classNode);
            compUnit.getNeoModule().addMethod(calledNeoMethod);
            calledNeoMethod.initialize(compUnit);
            AbstractInsnNode insn = skipToSuperCtorCall(ctorMethod, classNode);
            insn = insn.getNext();
            while (insn != null) {
                insn = handleInsn(insn, calledNeoMethod, compUnit);
                insn = insn.getNext();
            }
        }
        int fieldSize = classNode.fields.size();
        return finalizeConstructorCall(fieldSize, typeInsn, ctorMethod, classNode, callingNeoMethod, calledNeoMethod,
                compUnit);
    }


    private static AbstractInsnNode convertEvent(FieldInsnNode eventFieldInsn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        if (neoMethod.isVerifyMethod()) {
            throw new CompilerException(neoMethod, "The verify method is not allowed to fire any event.");
        }
        String eventVariableName = eventFieldInsn.name;
        List<NeoEvent> events = compUnit.getNeoModule().getEvents();
        NeoEvent event = events.stream()
                .filter(e -> eventVariableName.equals(e.getAsmVariable().name))
                .findFirst()
                .orElseThrow(() -> new CompilerException(neoMethod,
                        "Couldn't find triggered event in list of events. Make sure to declare events only in the " +
                                "main contract class."));

        AbstractInsnNode insn = eventFieldInsn.getNext();
        while (!isMethodCallToEventSend(insn, compUnit)) {
            insn = handleInsn(insn, neoMethod, compUnit);
            insn = insn.getNext();
            assert insn != null : "Expected to find call to send() method of an event but reached the end of the " +
                    "instructions.";
        }

        // The current instruction is the method call to Event.send(...). We can pack the arguments and do the
        // syscall instead of actually calling the send(...) method.
        addReverseArguments(neoMethod, event.getNumberOfParams());
        addPushNumber(event.getNumberOfParams(), neoMethod);
        neoMethod.addInstruction(new NeoInstruction(OpCode.PACK));
        neoMethod.addInstruction(buildPushDataInsn(event.getDisplayName()));
        byte[] syscallHash = Numeric.hexStringToByteArray(InteropService.SYSTEM_RUNTIME_NOTIFY.getHash());
        neoMethod.addInstruction(new NeoInstruction(OpCode.SYSCALL, syscallHash));
        return insn;
    }

    private static boolean isMethodCallToEventSend(AbstractInsnNode insn, CompilationUnit compUnit) throws IOException {
        if (!(insn instanceof MethodInsnNode)) {
            return false;
        }
        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        return isEvent(Type.getObjectType(methodInsn.owner).getDescriptor(), compUnit);
    }

}
