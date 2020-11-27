package io.neow3j.compiler.converters;

import static io.neow3j.compiler.AsmHelper.getAsmClassForInternalName;
import static io.neow3j.compiler.AsmHelper.getFieldIndex;
import static io.neow3j.compiler.AsmHelper.getMethodNode;
import static io.neow3j.compiler.AsmHelper.hasAnnotations;
import static io.neow3j.compiler.Compiler.INSTANCE_CTOR;
import static io.neow3j.compiler.Compiler.addInstruction;
import static io.neow3j.compiler.Compiler.addPushNumber;
import static io.neow3j.compiler.Compiler.addReverseArguments;
import static io.neow3j.compiler.Compiler.addSyscall;
import static io.neow3j.compiler.Compiler.buildPushDataInsn;
import static io.neow3j.compiler.Compiler.findSuperCallToObjectCtor;
import static io.neow3j.compiler.Compiler.handleInsn;
import static io.neow3j.compiler.LocalVariableHelper.buildStoreOrLoadVariableInsn;
import static io.neow3j.utils.ClassUtils.getClassNameForInternalName;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static java.lang.String.format;
import static org.objectweb.asm.Type.getInternalName;

import io.neow3j.compiler.AsmHelper;
import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.CompilerException;
import io.neow3j.compiler.JVMOpcode;
import io.neow3j.compiler.NeoInstruction;
import io.neow3j.compiler.NeoMethod;
import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Instruction.Instructions;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.annotations.Syscall.Syscalls;
import io.neow3j.model.types.StackItemType;
import java.io.IOException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class ObjectsConverter implements Converter {

    private static final String APPEND_METHOD_NAME = "append";
    private static final String TOSTRING_METHOD_NAME = "toString";

    @Override
    public AbstractInsnNode convert(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        switch (opcode) {
            case PUTSTATIC:
                addStoreStaticField(insn, neoMethod);
                break;
            case GETSTATIC:
                addLoadStaticField(insn, neoMethod);
                break;
            case CHECKCAST:
                // Check if the object on the operand stack can be cast to a given type.
                // There is no corresponding NeoVM opcode.
                break;
            case NEW:
                insn = handleNew(insn, neoMethod, compUnit);
                break;
            case ARRAYLENGTH:
                neoMethod.addInstruction(new NeoInstruction(OpCode.SIZE));
                break;
            case INSTANCEOF:
                throw new CompilerException(neoMethod, format("JVM opcode %s is not supported.",
                        opcode.name()));
        }
        return insn;
    }

    public static void addLoadStaticField(AbstractInsnNode insn, NeoMethod neoMethod) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, neoMethod.getOwnerClass());
        neoMethod.addInstruction(buildStoreOrLoadVariableInsn(idx, OpCode.LDSFLD));
    }

    public static void addStoreStaticField(AbstractInsnNode insn, NeoMethod neoMethod) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, neoMethod.getOwnerClass());
        neoMethod.addInstruction(buildStoreOrLoadVariableInsn(idx, OpCode.STSFLD));
    }

    public static AbstractInsnNode handleNew(AbstractInsnNode insn, NeoMethod callingNeoMethod,
            CompilationUnit compUnit) throws IOException {

        TypeInsnNode typeInsn = (TypeInsnNode) insn;
        assert typeInsn.getNext().getOpcode() == JVMOpcode.DUP.getOpcode()
                : "Expected DUP after NEW but got other instructions";

        if (isNewStringBuilder(typeInsn)) {
            // Java, in the background, performs String concatenation, like `s1 + s2`, with the
            // instantiation of a StringBuilder. This is handled here.
            return handleStringConcatenation(typeInsn, callingNeoMethod, compUnit);
        }

        if (isNewThrowable(typeInsn, compUnit)) {
            return handleNewThrowable(typeInsn, callingNeoMethod, compUnit);
        }

        ClassNode owner = getAsmClassForInternalName(typeInsn.desc, compUnit.getClassLoader());
        MethodInsnNode ctorMethodInsn = skipToCtorMethodInstruction(typeInsn.getNext(), owner,
                callingNeoMethod);
        MethodNode ctorMethod = getMethodNode(ctorMethodInsn, owner).orElseThrow(() ->
                new CompilerException(callingNeoMethod, format(
                        "Couldn't find constructor '%s' on class '%s'.",
                        ctorMethodInsn.name, getClassNameForInternalName(owner.name))));

        if (ctorMethod.invisibleAnnotations == null
                || ctorMethod.invisibleAnnotations.size() == 0) {
            // It's a generic constructor without any Neo-specific annotations.
            return convertConstructorCall(typeInsn, ctorMethod, owner, callingNeoMethod, compUnit);
        } else {
            // The constructor has some Neo-specific annotation. No NEWARRAY/NEWSTRUCT or DUP is
            // needed here.
            // After the JVM NEW and DUP, arguments that will be given to the INVOKESPECIAL call can
            // follow. Those are handled in the following while.
            insn = insn.getNext().getNext();
            while (!isCallToCtor(insn, owner.name)) {
                insn = handleInsn(insn, callingNeoMethod, compUnit);
                insn = insn.getNext();
            }
            // Now we're at the INVOKESPECIAL call and can convert the ctor method.
            if (hasAnnotations(ctorMethod, Syscall.class, Syscalls.class)) {
                addSyscall(ctorMethod, callingNeoMethod);
            } else if (hasAnnotations(ctorMethod, Instruction.class, Instructions.class)) {
                addInstructionsFromAnnotation(ctorMethod, callingNeoMethod);
            }
            return insn;
        }
    }

    private static boolean isNewStringBuilder(TypeInsnNode typeInsn) {
        return typeInsn.desc.equals(getInternalName(StringBuilder.class));
    }

    private static boolean isNewThrowable(TypeInsnNode typeInsn,
            CompilationUnit compUnit) throws IOException {

        ClassNode type = AsmHelper.getAsmClassForInternalName(typeInsn.desc,
                compUnit.getClassLoader());

        if (getFullyQualifiedNameForInternalName(type.name).equals(
                Throwable.class.getCanonicalName())) {
            return true;
        }
        while (type.superName != null) {
            type = getAsmClassForInternalName(type.superName, compUnit.getClassLoader());
            if (getFullyQualifiedNameForInternalName(type.name).equals(
                    Throwable.class.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    private static AbstractInsnNode handleNewThrowable(TypeInsnNode typeInsn,
            NeoMethod callingNeoMethod, CompilationUnit compUnit) throws IOException {

        if (!Exception.class.getCanonicalName()
                .equals(getFullyQualifiedNameForInternalName(typeInsn.desc))) {
            throw new CompilerException(callingNeoMethod, format("Contract uses exception of type "
                            + "%s but only %s is allowed.",
                    getFullyQualifiedNameForInternalName(typeInsn.desc),
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
            throw new CompilerException(callingNeoMethod, format("An exception thrown in a contract"
                    + " can either take no arguments or a String argument. You provided %d "
                    + "arguments.", argTypes.length));
        }
        if (argTypes.length == 1 &&
                !getFullyQualifiedNameForInternalName(argTypes[0].getInternalName())
                        .equals(String.class.getCanonicalName())) {
            throw new CompilerException(callingNeoMethod, "An exception thrown in a contract can "
                    + "either take no arguments or a String argument. You provided a non-string "
                    + "argument.");

        }
        if (argTypes.length == 0) {
            // No exception message is given, thus we add a dummy message.
            callingNeoMethod.addInstruction(buildPushDataInsn("error"));
        }
        return insn;
    }

    /**
     * Handles the concatenation of strings, as in {@code "hello" + " world"}. Java in the
     * background uses a StringBuilder for this.
     *
     * @param typeInsnNode The NEW instruction concerning the StringBuilder.
     * @return the last processed instruction.
     */
    private static AbstractInsnNode handleStringConcatenation(TypeInsnNode typeInsnNode,
            NeoMethod neoMethod, CompilationUnit compUnit) throws IOException {

        // Skip to the next instruction after DUP and INVOKESPECIAL.
        AbstractInsnNode insn = typeInsnNode.getNext().getNext().getNext();

        boolean isFirstCall = true;
        while (insn != null) {
            if (isCallToStringBuilderAppend(insn)) {
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
            if (isCallToAnyStringBuilderMethod(insn)) {
                throw new CompilerException(neoMethod, format("Only 'append()' and 'toString()' "
                                + "are supported for StringBuilder, but '%s' was called",
                        ((MethodInsnNode) insn).name));
            }
            insn = handleInsn(insn, neoMethod, compUnit);
            insn = insn.getNext();
        }
        if (insn == null) {
            throw new CompilerException(neoMethod, "Expected to find ScriptBuilder.toString() but "
                    + "reached end of method.");
        }
        return insn;
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

    private static MethodInsnNode skipToCtorMethodInstruction(AbstractInsnNode insn,
            ClassNode owner, NeoMethod neoMethod) {

        while (insn.getNext() != null) {
            insn = insn.getNext();
            if (isCallToCtor(insn, owner.name)) {
                return (MethodInsnNode) insn;
            }
        }
        throw new CompilerException(neoMethod, format("Tried to skip to an instruction calling "
                        + "the constructor of the class %s but reached the end of the method.",
                getFullyQualifiedNameForInternalName(owner.name)));

    }

    private static AbstractInsnNode convertConstructorCall(TypeInsnNode typeInsn,
            MethodNode ctorMethod, ClassNode owner, NeoMethod callingNeoMethod,
            CompilationUnit compUnit) throws IOException {

        NeoMethod calledNeoMethod;
        String ctorMethodId = NeoMethod.getMethodId(ctorMethod, owner);
        if (compUnit.getNeoModule().hasMethod(ctorMethodId)) {
            // If the module already contains the converted ctor.
            calledNeoMethod = compUnit.getNeoModule().getMethod(ctorMethodId);
        } else {
            // Create a new NeoMethod, i.e., convert the constructor to NeoVM code.
            // Skip the call to the Object ctor and continue processing the rest of the ctor.
            calledNeoMethod = new NeoMethod(ctorMethod, owner);
            compUnit.getNeoModule().addMethod(calledNeoMethod);
            calledNeoMethod.initializeMethod(compUnit);
            AbstractInsnNode insn = findSuperCallToObjectCtor(ctorMethod, owner);
            insn = insn.getNext();
            while (insn != null) {
                insn = handleInsn(insn, calledNeoMethod, compUnit);
                insn = insn.getNext();
            }
        }

        addPushNumber(owner.fields.size(), callingNeoMethod);
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.NEWARRAY));
        // TODO: Determine when to use NEWSTRUCT.
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.DUP));
        // After the JVM NEW and DUP, arguments that will be given to the INVOKESPECIAL call can
        // follow. Those are handled in the following while.
        AbstractInsnNode insn = typeInsn.getNext().getNext();
        while (!isCallToCtor(insn, owner.name)) {
            insn = handleInsn(insn, callingNeoMethod, compUnit);
            insn = insn.getNext();
        }
        // Reverse the arguments that are passed to the constructor call.
        addReverseArguments(ctorMethod, callingNeoMethod);
        // The actual address offset for the method call is set at a later point.
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.CALL_L, new byte[4])
                .setExtra(calledNeoMethod));
        return insn;
    }

    // Checks if the given instruction is a call to the given classes constructor (i.e., <init>).
    private static boolean isCallToCtor(AbstractInsnNode insn, String ownerInternalName) {
        return insn.getType() == AbstractInsnNode.METHOD_INSN
                && ((MethodInsnNode) insn).owner.equals(ownerInternalName)
                && ((MethodInsnNode) insn).name.equals(INSTANCE_CTOR);
    }

}
