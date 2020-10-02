package io.neow3j.compiler;

import static io.neow3j.compiler.AsmHelper.getAsmClass;
import static io.neow3j.constants.OpCode.getOperandSize;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.objectweb.asm.Type.getInternalName;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.NefFile.Version;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Instruction.Instructions;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.annotations.Syscall.Syscalls;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Compiler {

    public static final String COMPILER_NAME = "neow3j";
    public static final Version COMPILER_VERSION = new Version(0, 1, 0, 0);

    public static final int MAX_PARAMS_COUNT = 255;
    public static final int MAX_LOCAL_VARIABLES_COUNT = 255;
    public static final int MAX_STATIC_FIELDS_COUNT = 255;

    public static final String INSTANCE_CTOR = "<init>";
    private static final String CLASS_CTOR = "<clinit>";
    private static final String INITSSLOT_METHOD_NAME = "_initialize";
    public static final String THIS_KEYWORD = "this";

    private CompilationUnit compilationUnit;

    public Compiler() {
        compilationUnit = new CompilationUnit(this.getClass().getClassLoader());
    }

    public Compiler(ClassLoader classLoader) {
        compilationUnit = new CompilationUnit(classLoader);
    }

    /**
     * Compiles the given class to NeoVM code.
     *
     * @param fullyQualifiedClassName the fully qualified name of the class.
     * @return the compilation unit holding the NEF and contract manifest.
     */
    public CompilationUnit compileClass(String fullyQualifiedClassName) throws IOException {
        return compileClass(getAsmClass(fullyQualifiedClassName, compilationUnit.getClassLoader()));
    }

    /**
     * Compiles the given class to NeoVM code.
     *
     * @param classStream the {@link InputStream} pointing to a class file.
     * @return the compilation unit holding the NEF and contract manifest.
     */
    public CompilationUnit compileClass(InputStream classStream) throws IOException {
        return compileClass(getAsmClass(classStream));
    }

    /**
     * Compiles the given class to NeoVM code.
     *
     * @param contractClassNode the {@link ClassNode} representing a class file.
     * @return the compilation unit holding the NEF and contract manifest.
     */
    private CompilationUnit compileClass(ClassNode contractClassNode) throws IOException {
        compilationUnit.setAsmClass(contractClassNode);
        collectAndInitializeStaticFields(contractClassNode);
        collectAndInitializeMethods(contractClassNode);
        // Need to create a new list from the methods that have been added to the NeoModule so
        // far because we are potentially adding new methods to the module in the compilation,
        // which leads to concurrency errors.
        for (NeoMethod neoMethod : new ArrayList<>(
                compilationUnit.getNeoModule().methods.values())) {
            compileMethod(neoMethod, compilationUnit);
        }
        compilationUnit.getNeoModule().finalizeModule();
        NefFile nef = new NefFile(COMPILER_NAME, COMPILER_VERSION,
                compilationUnit.getNeoModule().toByteArray());
        ContractManifest manifest = ManifestBuilder.buildManifest(compilationUnit,
                nef.getScriptHash());
        compilationUnit.setNef(nef);
        compilationUnit.setManifest(manifest);
        return compilationUnit;
    }

    private void collectAndInitializeStaticFields(ClassNode asmClass) {
        if (asmClass.fields == null || asmClass.fields.size() == 0) {
            return;
        }
        if (asmClass.fields.size() > MAX_STATIC_FIELDS_COUNT) {
            throw new CompilerException("The method has more than the max number of static field "
                    + "variables.");
        }

        if (asmClass.fields.stream().anyMatch(f -> (f.access & Opcodes.ACC_STATIC) == 0)) {
            throw new CompilerException("Class " + asmClass.name + " has non-static fields but only"
                    + " static fields are supported in smart contracts.");
        }
        checkForUsageOfStaticConstructor(asmClass);
        NeoMethod neoMethod = createInitsslotMethod(asmClass);
        compilationUnit.getNeoModule().addMethod(neoMethod);
    }

    // Checks if there are any instructions in the given classes <init> method besides the call to
    // the `Object` constructor. I.e., only line, label, frame, and return instructions are allowed.
    // THe <init> method is checked because in case of a static constructor, its instructions
    // are placed into the <init> method and not into the <clinit> as one would expect.
    // If other instructions are found an exception is thrown because the compiler currently does
    // not support static constructors, but only initialization of static variables directly at
    // their definition.
    private void checkForUsageOfStaticConstructor(ClassNode asmClass) {
        Optional<MethodNode> instanceCtor = asmClass.methods.stream()
                .filter(m -> m.name.equals(INSTANCE_CTOR)).findFirst();
        if (instanceCtor.isPresent()) {
            AbstractInsnNode insn = findSuperCallToObjectCtor(instanceCtor.get());
            insn = insn.getNext();
            while (insn != null) {
                if (insn.getType() != AbstractInsnNode.LINE &&
                        insn.getType() != AbstractInsnNode.LABEL &&
                        insn.getType() != AbstractInsnNode.FRAME &&
                        insn.getOpcode() != JVMOpcode.RETURN.getOpcode()) {
                    throw new CompilerException(format("Class %s has an explicit instance "
                                    + "constructor or static constructor, but, neither is "
                                    + "supported.",
                            getFullyQualifiedNameForInternalName(asmClass.name)));
                }
                insn = insn.getNext();
            }
        }
    }

    // Creates the method (beginning with INITSSLOT) that initializes static variables in the NeoVM
    // script. This only looks at the <clinit> method of the class. The <clinit> method
    // contains static variable initialization instructions that happen right at the definition
    // of the variables and not in a static constructor. Static constructors are not supported at
    // the moment.
    private NeoMethod createInitsslotMethod(ClassNode asmClass) {
        MethodNode initsslotMethod = null;
        Optional<MethodNode> classCtorOpt = asmClass.methods.stream()
                .filter(m -> m.name.equals(CLASS_CTOR))
                .findFirst();
        if (classCtorOpt.isPresent()) {
            initsslotMethod = classCtorOpt.get();
        } else {
            // Static variables are not initialized but we still need to add the INITSSLOT method.
            // Therefore, we create a "fake" ASM method for it here.
            // TODO: Determine if this is even necessary.
            initsslotMethod = new MethodNode();
            initsslotMethod.instructions.add(new InsnNode(JVMOpcode.RETURN.getOpcode()));
            initsslotMethod.name = CLASS_CTOR;
            initsslotMethod.desc = "()V";
            initsslotMethod.access = Opcodes.ACC_STATIC;
        }
        NeoMethod neoMethod = new NeoMethod(initsslotMethod, asmClass);
        neoMethod.name = INITSSLOT_METHOD_NAME;
        neoMethod.isAbiMethod = true;
        byte[] operand = new byte[]{(byte) asmClass.fields.size()};
        neoMethod.addInstruction(new NeoInstruction(OpCode.INITSSLOT, operand));
        return neoMethod;
    }

    // Collects all static methods and initializes them, e.g., sets the parameters and local
    // variables.
    private void collectAndInitializeMethods(ClassNode asmClass) {
        for (MethodNode asmMethod : asmClass.methods) {
            if (asmMethod.name.equals(INSTANCE_CTOR) || asmMethod.name.equals(CLASS_CTOR)) {
                continue; // Handled in method `collectAndInitializeStaticFields()`.
            }
            if ((asmMethod.access & Opcodes.ACC_STATIC) == 0) {
                throw new CompilerException(asmClass, format("Method '%s' of class %s is non-static"
                                + " but only static methods are allowed in smart contracts.",
                        asmMethod.name, getFullyQualifiedNameForInternalName(asmClass.name)));
            }
            NeoMethod neoMethod = new NeoMethod(asmMethod, asmClass);
            MethodInitializer.initializeMethod(neoMethod, compilationUnit);
            compilationUnit.getNeoModule().addMethod(neoMethod);
        }
    }

    public static void compileMethod(NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {
        AbstractInsnNode insn = neoMethod.asmMethod.instructions.get(0);
        while (insn != null) {
            insn = handleInsn(insn, neoMethod, compUnit);
            insn = insn.getNext();
        }
    }

    // Handles/converts the given instruction. The given NeoMethod is the method that the
    // instruction belongs to and the converted instruction will be added to that method. Returns
    // the last instruction node that was processed, i.e., the returned instruction can be used
    // to obtain the next instruction that should be processed.
    public static AbstractInsnNode handleInsn(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        if (insn.getType() == AbstractInsnNode.LINE) {
            neoMethod.currentLine = ((LineNumberNode) insn).line;
        }
        if (insn.getType() == AbstractInsnNode.LABEL) {
            neoMethod.currentLabel = ((LabelNode) insn).getLabel();
        }
        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        if (opcode == null) {
            return insn;
        }
        Converter converter = ConverterMap.get(opcode);
        if (converter == null) {
            throw new CompilerException(compUnit, neoMethod,
                    format("Unsupported instruction %s.", opcode.toString()));
        }
        return converter.convert(insn, neoMethod, compUnit);
    }

    public static int getFieldIndex(FieldInsnNode fieldInsn, ClassNode owner) {
        int idx = 0;
        for (FieldNode field : owner.fields) {
            if (field.name.equals(fieldInsn.name)) {
                break;
            }
            idx++;
        }
        return idx;
    }

    public static void addSyscall(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
        // Before doing the syscall the arguments have to be reversed.
        addReverseArguments(calledAsmMethod, callingNeoMethod);
        // Annotation has to be either Syscalls or Syscall.
        AnnotationNode syscallAnnotation = calledAsmMethod.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Syscalls.class))
                        || a.desc.equals(Type.getDescriptor(Syscall.class)))
                .findFirst().get();
        if (syscallAnnotation.desc.equals(Type.getDescriptor(Syscalls.class))) {
            for (Object a : (List<?>) syscallAnnotation.values.get(1)) {
                addSingleSyscall((AnnotationNode) a, callingNeoMethod);
            }
        } else {
            addSingleSyscall(syscallAnnotation, callingNeoMethod);
        }
    }

    public static void addLoadConstant(AbstractInsnNode insn, NeoMethod neoMethod) {
        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
        if (ldcInsn.cst instanceof String) {
            addPushDataArray(((String) ldcInsn.cst).getBytes(UTF_8), neoMethod);
        } else if (ldcInsn.cst instanceof Integer) {
            addPushNumber(((Integer) ldcInsn.cst), neoMethod);
        } else if (ldcInsn.cst instanceof Long) {
            addPushNumber(((Long) ldcInsn.cst), neoMethod);
        } else if (ldcInsn.cst instanceof Float || ldcInsn.cst instanceof Double) {
            throw new CompilerException("Compiler does not support floating point numbers.");
        }
        // TODO: Handle `org.objectweb.asm.Type`.
    }

    public static void addPushDataArray(byte[] data, NeoMethod neoMethod) {
        addInstructionFromBytes(new ScriptBuilder().pushData(data).toArray(), neoMethod);
    }

    public static void addPushDataArray(String data, NeoMethod neoMethod) {
        addInstructionFromBytes(new ScriptBuilder().pushData(data).toArray(), neoMethod);
    }

    private static void addInstructionFromBytes(byte[] insnBytes, NeoMethod neoMethod) {
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        neoMethod.addInstruction(new NeoInstruction(OpCode.get(insnBytes[0]), operand));
    }

    // Goes through the instructions of the given method and looks for the call to the `Object`
    // constructor. I.e., the given method should be a constructor. Super calls to other classes
    // are not allowed.
    public static MethodInsnNode findSuperCallToObjectCtor(MethodNode constructor) {
        Iterator<AbstractInsnNode> it = constructor.instructions.iterator();
        AbstractInsnNode insn = null;
        while (it.hasNext()) {
            insn = it.next();
            if (insn.getType() == AbstractInsnNode.METHOD_INSN
                    && ((MethodInsnNode) insn).name.equals(INSTANCE_CTOR)) {
                if (((MethodInsnNode) insn).owner.equals(getInternalName(Object.class))) {
                    break;
                } else {
                    throw new CompilerException(format("Found call to super constructor of %s "
                                    + "but inheritance is not supported, i.e., only super calls "
                                    + "to the Object constructor are allowed.",
                            getFullyQualifiedNameForInternalName(((MethodInsnNode) insn).owner)));
                }
            }
        }
        assert insn != null && insn instanceof MethodInsnNode : "Expected call to Object super "
                + "constructor but couldn't find it.";
        return (MethodInsnNode) insn;
    }


    // Adds an opcode that reverses the ordering of the arguments on the evaluation stack
    // according to the number of arguments the called method takes.
    public static void addReverseArguments(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
        int paramsCount = Type.getMethodType(calledAsmMethod.desc).getArgumentTypes().length;
        if (calledAsmMethod.localVariables != null
                && calledAsmMethod.localVariables.size() > 0
                && calledAsmMethod.localVariables.get(0).name.equals(THIS_KEYWORD)) {
            // The called method is an instance method, i.e., the instance itself ("this") is
            // also an argument.
            paramsCount++;
        }
        addReverseArguments(callingNeoMethod, paramsCount);
    }

    public static void addReverseArguments(NeoMethod callingNeoMethod, int paramsCount) {
        if (paramsCount == 2) {
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.SWAP));
        } else if (paramsCount == 3) {
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.REVERSE3));
        } else if (paramsCount == 4) {
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.REVERSE4));
        } else if (paramsCount > 4) {
            addPushNumber(paramsCount, callingNeoMethod);
            callingNeoMethod.addInstruction(new NeoInstruction(OpCode.REVERSEN));
        }
    }

    private static void addSingleSyscall(AnnotationNode syscallAnnotation, NeoMethod neoMethod) {
        String syscallName = ((String[]) syscallAnnotation.values.get(1))[1];
        InteropServiceCode syscall = InteropServiceCode.valueOf(syscallName);
        byte[] hash = Numeric.hexStringToByteArray(syscall.getHash());
        neoMethod.addInstruction(new NeoInstruction(OpCode.SYSCALL, hash));
    }

    public static void addInstruction(MethodNode asmMethod, NeoMethod neoMethod) {
        AnnotationNode insnAnnotation = asmMethod.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Instructions.class))
                        || a.desc.equals(Type.getDescriptor(Instruction.class)))
                .findFirst().get();
        if (insnAnnotation.desc.equals(Type.getDescriptor(Instructions.class))) {
            for (Object a : (List<?>) insnAnnotation.values.get(1)) {
                addSingleInstruction((AnnotationNode) a, neoMethod);
            }
        } else {
            addSingleInstruction(insnAnnotation, neoMethod);
        }
    }

    private static void addSingleInstruction(AnnotationNode insnAnnotation, NeoMethod neoMethod) {
        // Setting a default value on the Instruction annotation does not have an effect on ASM.
        // The default value does not show up in the ASM annotation node. I.e. the annotation
        // values can be null if the default values were used.
        if (insnAnnotation.values == null) {
            // In this case the default value OpCode.NOP was used, so there is nothing to do.
            return;
        }
        String insnName = ((String[]) insnAnnotation.values.get(1))[1];
        OpCode opcode = OpCode.valueOf(insnName);
        if (opcode.equals(OpCode.NOP)) {
            // The default value OpCode.NOP was set explicitly. Nothing to do.
            return;
        }
        if (insnAnnotation.values.size() == 4) {
            byte[] operand = getOperand(insnAnnotation, opcode);
            neoMethod.addInstruction(new NeoInstruction(opcode, operand));
        } else {
            neoMethod.addInstruction(new NeoInstruction(opcode));
        }
    }

    private static byte[] getOperand(AnnotationNode insnAnnotation, OpCode opcode) {
        byte[] operand = new byte[]{};
        if (insnAnnotation.values.get(3) instanceof byte[]) {
            operand = (byte[]) insnAnnotation.values.get(3);
        } else if (insnAnnotation.values.get(3) instanceof List) {
            List<?> operandAsList = (List<?>) insnAnnotation.values.get(3);
            operand = new byte[operandAsList.size()];
            int i = 0;
            for (Object element : operandAsList) {
                operand[i++] = (byte) element;
            }
        }
        if (operand.length != getOperandSize(opcode).size()) {
            throw new CompilerException("Opcode " + opcode.name() + " was used with a wrong number "
                    + "of operand bytes.");
        }
        return operand;
    }

    public static void addPushNumber(long number, NeoMethod neoMethod) {
        byte[] insnBytes = new ScriptBuilder().pushInteger(BigInteger.valueOf(number))
                .toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        neoMethod.addInstruction(new NeoInstruction(OpCode.get(insnBytes[0]), operand));
    }
}
