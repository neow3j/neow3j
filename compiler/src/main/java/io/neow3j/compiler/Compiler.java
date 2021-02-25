package io.neow3j.compiler;

import static io.neow3j.compiler.AsmHelper.getAsmClass;
import static io.neow3j.compiler.AsmHelper.getInternalNameForDescriptor;
import static io.neow3j.compiler.DebugInfo.buildDebugInfo;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.objectweb.asm.Type.getInternalName;

import io.neow3j.compiler.converters.Converter;
import io.neow3j.compiler.converters.ConverterMap;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.devpack.ApiInterface;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Instruction.Instructions;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.annotations.Syscall.Syscalls;
import io.neow3j.devpack.events.Event;
import io.neow3j.model.types.ContractParameterType;
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
import java.util.stream.Collectors;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Compiler {

    public static final String COMPILER_NAME = "neow3j-3.6.1";

    public static final int MAX_PARAMS_COUNT = 255;
    public static final int MAX_LOCAL_VARIABLES = 255;
    public static final int MAX_STATIC_FIELDS = 255;

    public static final String INSTANCE_CTOR = "<init>";
    private static final String CLASS_CTOR = "<clinit>";
    public static final String THIS_KEYWORD = "this";

    public static final String INSTRUCTION_ANNOTATION_OPERAND = "operand";
    public static final String INSTRUCTION_ANNOTATION_OPERAND_PREFIX = "operandPrefix";

    private final CompilationUnit compUnit;

    public Compiler() {
        compUnit = new CompilationUnit(this.getClass().getClassLoader());
    }

    public Compiler(ClassLoader classLoader) {
        compUnit = new CompilationUnit(classLoader);
    }

    public static ContractParameterType mapTypeToParameterType(Type type) {
        String typeName = type.getClassName();
        if (typeName.equals(String.class.getTypeName())) {
            return ContractParameterType.STRING;
        }
        if (typeName.equals(Integer.class.getTypeName())
                || typeName.equals(int.class.getTypeName())
                || typeName.equals(Long.class.getTypeName())
                || typeName.equals(long.class.getTypeName())
                || typeName.equals(Byte.class.getTypeName())
                || typeName.equals(byte.class.getTypeName())
                || typeName.equals(Short.class.getTypeName())
                || typeName.equals(short.class.getTypeName())
                || typeName.equals(Character.class.getTypeName())
                || typeName.equals(char.class.getTypeName())) {
            return ContractParameterType.INTEGER;
        }
        if (typeName.equals(Boolean.class.getTypeName())
                || typeName.equals(boolean.class.getTypeName())) {
            return ContractParameterType.BOOLEAN;
        }
        if (typeName.equals(Byte[].class.getTypeName())
                || typeName.equals(byte[].class.getTypeName())) {
            return ContractParameterType.BYTE_ARRAY;
        }
        if (typeName.equals(Void.class.getTypeName())
                || typeName.equals(void.class.getTypeName())) {
            return ContractParameterType.VOID;
        }
        if (typeName.equals(ECPoint.class.getTypeName())) {
            return ContractParameterType.PUBLIC_KEY;
        }
        if (typeName.equals(Map.class.getTypeName())) {
            return ContractParameterType.MAP;
        }
        if (typeName.equals(Hash160.class.getTypeName())) {
            return ContractParameterType.HASH160;
        }
        if (typeName.equals(Hash256.class.getTypeName())) {
            return ContractParameterType.HASH256;
        }
        if (typeName.equals(io.neow3j.devpack.List.class.getTypeName())) {
            // The io.neow3j.devpack.List type is simply an array-abstraction.
            return ContractParameterType.ARRAY;
        }
        try {
            typeName = getFullyQualifiedNameForInternalName(type.getInternalName());
            Class<?> clazz = Class.forName(typeName);
            if (Arrays.asList(clazz.getInterfaces()).contains(ApiInterface.class)) {
                return ContractParameterType.INTEROP_INTERFACE;
            }
        } catch (ClassNotFoundException ignore) {
        }
        try {
            typeName = type.getDescriptor().replace("/", ".");
            Class<?> clazz = Class.forName(typeName);
            if (clazz.isArray()) {
                return ContractParameterType.ARRAY;
            }
        } catch (ClassNotFoundException ignore) {
        }
        // If the type is Object or any other class.
        return ContractParameterType.ANY;
    }

//    /**
//     * Converts the given classes to neo-vm code and generates debug information with the help of
//     * the given source file paths.
//     * <p>
//     * Make sure that the {@code Classloader} used to initialize this {@code Compiler} includes
//     the
//     * paths to the given class files.
//     *
//     * @param classNames      The fully qualified names of the classes
//     * @param sourceFilePaths The absolute paths to the source files of the given classes.
//     * @return the compilation results.
//     * @throws IOException if something goes wrong when reading Java and class files from disk.
//     */
//    public CompilationUnit compileClasses(Set<String> classNames, Set<String> sourceFilePaths)
//            throws IOException {
//
//        List<ClassNode> classes = new ArrayList<>();
//        for (String className : classNames) {
//            ClassNode asmClass = getAsmClass(className, compUnit.getClassLoader());
//            classes.add(asmClass);
//            String relativePath = className.replace(".", "/");
//            String sourceFilePath = sourceFilePaths.stream()
//                    .filter(path -> path.contains(relativePath))
//                    .findFirst().orElseThrow(() -> new CompilerException(
//                            "Could not find source file for class " + className));
//            compUnit.addClassToSourceMapping(className, sourceFilePath);
//        }
//        for (ClassNode asmClass : classes) {
//            compileClass(asmClass);
//        }
//        finalizeCompilation();
//        return compUnit;
//    }

//    /**
//     * Converts the given JVM class files a neo-vm script. No debugging information is created
//     * because the source files are unknown.
//     * <p>
//     * Make sure that the {@code Classloader} used to initialize this {@code Compiler} includes
//     the
//     * paths to the given class files.
//     *
//     * @param classNames The fully qualified names of the classes to convert to neo-vm code.
//     * @return The compilation result.
//     * @throws IOException if something goes wrong when reading Java and class files from disk.
//     */
//    public CompilationUnit compileClasses(Set<String> classNames) throws IOException {
//        for (String className : classNames) {
//            ClassNode asmClass = getAsmClass(className, compUnit.getClassLoader());
//            compileClass(asmClass);
//        }
//        finalizeCompilation();
//        return compUnit;
//    }

    /**
     * Compiles the given class to the corresponding neo-vm script and produces debugging
     * information to be used with the Neo Debugger.
     *
     * @param className      The fully qualified name of the class to compile.
     * @param sourceFilePath The absolute path of the class' source file.
     * @return The compilation unit holding the NEF, contract manifest, and {@code DebugInfo}.
     * @throws IOException If an error occurs when reading the class and source files.
     */
    public CompilationUnit compileClass(String className, String sourceFilePath)
            throws IOException {

        ClassNode asmClass = getAsmClass(className, compUnit.getClassLoader());
        String relativePath = className.replace(".", "/");
        if (!sourceFilePath.contains(relativePath)) {
            throw new IllegalArgumentException("Source file path does not correspond to the fully "
                    + "qualified name of the given class.");
        }
        compUnit.addClassToSourceMapping(className, sourceFilePath);
        compileClass(asmClass);
        return compUnit;
    }

    /**
     * Compiles the given class to NeoVM code.
     *
     * @param fullyQualifiedClassName the fully qualified name of the class.
     * @return the compilation unit holding the NEF and contract manifest.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public CompilationUnit compileClass(String fullyQualifiedClassName) throws IOException {
        return compileClass(getAsmClass(fullyQualifiedClassName, compUnit.getClassLoader()));
    }

    /**
     * Compiles the given class to NeoVM code.
     *
     * @param classStream the {@link InputStream} pointing to a class file.
     * @return the compilation unit holding the NEF and contract manifest.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public CompilationUnit compileClass(InputStream classStream) throws IOException {
        return compileClass(getAsmClass(classStream));
    }

    /**
     * Compiles the given class to NeoVM code.
     *
     * @param classNode the {@link ClassNode} representing a class file.
     * @return the compilation unit holding the NEF and contract manifest.
     */
    private CompilationUnit compileClass(ClassNode classNode) throws IOException {
        compUnit.setContractClass(classNode);
        checkForUsageOfInstanceConstructor(classNode);
        checkFieldVariables(classNode);
        collectSmartContractEvents(classNode);
        compUnit.getNeoModule().addMethod(createInitsslotMethod(classNode));
        compUnit.getNeoModule().addMethods(initializeContractMethods(classNode));
        // Need to create a new list from the methods that have been added to the NeoModule so
        // far because we are potentially adding new methods to the module in the compilation,
        // which leads to concurrency errors.
        for (NeoMethod neoMethod : new ArrayList<>(compUnit.getNeoModule().getSortedMethods())) {
            neoMethod.convert(compUnit);
        }
        finalizeCompilation();
        return compUnit;
    }

    private void checkFieldVariables(ClassNode asmClass) {
        if (asmClass.fields == null) {
            return;
        }
        if (asmClass.fields.size() > MAX_STATIC_FIELDS) {
            throw new CompilerException(format("The class %s has more than the max supported "
                            + "number of static field variables (%d).",
                    getFullyQualifiedNameForInternalName(asmClass.name), MAX_STATIC_FIELDS));
        }
        if (asmClass.fields.stream().anyMatch(f -> (f.access & Opcodes.ACC_STATIC) == 0)) {
            throw new CompilerException(format("Class %s has non-static fields but only static "
                            + "fields are supported in smart contract classes.",
                    getFullyQualifiedNameForInternalName(asmClass.name)));
        }
    }

    private void finalizeCompilation() {
        compUnit.getNeoModule().finalizeModule();
        // TODO: Pass MethodTokens to the NefFile constructor.
        NefFile nef = new NefFile(COMPILER_NAME, compUnit.getNeoModule().toByteArray(),
                compUnit.getNeoModule().getMethodTokens());
        ContractManifest manifest = ManifestBuilder.buildManifest(compUnit);
        compUnit.setNef(nef);
        compUnit.setManifest(manifest);
        compUnit.setDebugInfo(buildDebugInfo(compUnit));
    }


    private void collectSmartContractEvents(ClassNode asmClass) {
        if (asmClass.fields == null || asmClass.fields.size() == 0) {
            return;
        }
        List<FieldNode> eventFields = asmClass.fields
                .stream()
                .filter(field -> isEvent(getInternalNameForDescriptor(field.desc)))
                .collect(Collectors.toList());

        if (eventFields.size() == 0) {
            return;
        }

        eventFields.forEach(field -> {
            compUnit.getNeoModule().addEvent(new NeoEvent(field, asmClass));
        });
    }

    // Checks if there are any instructions in the given classes <init> method (the instance
    // initializer) besides the call to the `Object` constructor. I.e., only line, label, frame, and
    // return instructions are allowed.
    // If instructions are found an exception is thrown because the compiler does not support
    // instance constructors.
    private void checkForUsageOfInstanceConstructor(ClassNode asmClass) {
        Optional<MethodNode> instanceCtor = asmClass.methods.stream()
                .filter(m -> m.name.equals(INSTANCE_CTOR)).findFirst();
        if (instanceCtor.isPresent()) {
            AbstractInsnNode insn = skipToSuperCtorCall(instanceCtor.get(), asmClass);
            insn = insn.getNext();
            while (insn != null) {
                if (insn.getType() != AbstractInsnNode.LINE &&
                        insn.getType() != AbstractInsnNode.LABEL &&
                        insn.getType() != AbstractInsnNode.FRAME &&
                        insn.getOpcode() != JVMOpcode.RETURN.getOpcode()) {
                    throw new CompilerException(format("Class %s has an explicit instance "
                                    + "constructor, which is not supported.",
                            getFullyQualifiedNameForInternalName(asmClass.name)));
                }
                insn = insn.getNext();
            }
        }
    }

    // Creates the method (beginning with INITSSLOT) that initializes static variables in the NeoVM
    // script. This only looks at the <clinit> method of the class. The <clinit> method
    // contains static variable initialization instructions that happen right at the definition
    // of the variables or in the static constructor.
    private InitsslotNeoMethod createInitsslotMethod(ClassNode asmClass) {
        Optional<MethodNode> classCtorOpt = asmClass.methods.stream()
                .filter(m -> m.name.equals(CLASS_CTOR))
                .findFirst();
        return classCtorOpt.map(methodNode -> new InitsslotNeoMethod(methodNode, asmClass))
                .orElse(null);
    }

    // Collects all static methods and initializes them, e.g., sets the parameters and local
    // variables.
    private List<NeoMethod> initializeContractMethods(ClassNode asmClass) {
        List<NeoMethod> methods = new ArrayList<>();
        for (MethodNode asmMethod : asmClass.methods) {
            if (asmMethod.name.equals(INSTANCE_CTOR) || asmMethod.name.equals(CLASS_CTOR)) {
                continue; // Handled in method `collectAndInitializeStaticFields()`.
            }
            if ((asmMethod.access & Opcodes.ACC_STATIC) == 0) {
                throw new CompilerException(asmClass, format("Method '%s' of class %s is non-static"
                                + " but only static methods are allowed in smart contracts.",
                        asmMethod.name, getFullyQualifiedNameForInternalName(asmClass.name)));
            }
            if (!compUnit.getNeoModule().hasMethod(NeoMethod.getMethodId(asmMethod, asmClass))) {
                NeoMethod neoMethod = new NeoMethod(asmMethod, asmClass);
                neoMethod.initialize(compUnit);
                methods.add(neoMethod);
            }
        }
        return methods;
    }

    // Handles/converts the given instruction. The given NeoMethod is the method that the
    // instruction belongs to and the converted instruction will be added to that method. Returns
    // the last instruction node that was processed, i.e., the returned instruction can be used
    // to obtain the next instruction that should be processed.
    public static AbstractInsnNode handleInsn(AbstractInsnNode insn, NeoMethod neoMethod,
            CompilationUnit compUnit) throws IOException {

        if (insn.getType() == AbstractInsnNode.LINE) {
            neoMethod.setCurrentLine(((LineNumberNode) insn).line);
        }
        if (insn.getType() == AbstractInsnNode.LABEL) {
            neoMethod.setCurrentLabel(((LabelNode) insn).getLabel());
        }
        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        if (opcode == null) {
            return insn;
        }
        Converter converter = ConverterMap.get(opcode);
        if (converter == null) {
            throw new CompilerException(neoMethod,
                    format("Unsupported instruction %s in method '%s' of class %s",
                            opcode.toString(), neoMethod.getSourceMethodName(),
                            getFullyQualifiedNameForInternalName(neoMethod.getOwnerClass().name)));
        }
        return converter.convert(insn, neoMethod, compUnit);
    }

    public static void addSyscall(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
        // Before doing the syscall the arguments have to be reversed.
        addReverseArguments(calledAsmMethod, callingNeoMethod);
        addSyscallInternal(calledAsmMethod, callingNeoMethod);
    }

    private static void addSyscallInternal(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
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

    public static void addConstructorSyscall(MethodNode calledAsmMethod,
            NeoMethod callingNeoMethod) {
        addSyscallInternal(calledAsmMethod, callingNeoMethod);
    }

    public static void addLoadConstant(AbstractInsnNode insn, NeoMethod neoMethod) {
        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
        if (ldcInsn.cst instanceof String) {
            byte[] data = ((String) ldcInsn.cst).getBytes(UTF_8);
            neoMethod.addInstruction(buildPushDataInsn(data));
        } else if (ldcInsn.cst instanceof Integer) {
            addPushNumber(((Integer) ldcInsn.cst), neoMethod);
        } else if (ldcInsn.cst instanceof Long) {
            addPushNumber(((Long) ldcInsn.cst), neoMethod);
        } else if (ldcInsn.cst instanceof Float || ldcInsn.cst instanceof Double) {
            throw new CompilerException(neoMethod, "Found use of float number but the compiler "
                    + "does not support floats.");
        }
        // TODO: Handle `org.objectweb.asm.Type`.
    }

    public static NeoInstruction buildPushDataInsn(byte[] data) {
        return buildPushDataInsnFromInsnBytes(new ScriptBuilder().pushData(data).toArray());
    }

    public static NeoInstruction buildPushDataInsn(String data) {
        return buildPushDataInsnFromInsnBytes(new ScriptBuilder().pushData(data).toArray());
    }

    private static NeoInstruction buildPushDataInsnFromInsnBytes(byte[] insnBytes) {
        OpCode opcode = OpCode.get(insnBytes[0]);
        int prefixSize = OpCode.getOperandSize(opcode).prefixSize();
        byte[] operandPrefix = Arrays.copyOfRange(insnBytes, 1, 1 + prefixSize);
        byte[] operand = Arrays.copyOfRange(insnBytes, 1 + prefixSize, insnBytes.length);
        return new NeoInstruction(opcode, operandPrefix, operand);
    }

    /**
     * Skips instructions in the given constructor method up to the super constructor call.
     * There should always be a super call even if it was not explicitly specified by the developer.
     *
     * @param constructor The constructor method.
     * @param owner The class of the constructor method.
     * @return The instruction that calls the super constructor.
     */
    public static MethodInsnNode skipToSuperCtorCall(MethodNode constructor, ClassNode owner) {
        Iterator<AbstractInsnNode> it = constructor.instructions.iterator();
        AbstractInsnNode insn = null;
        while (it.hasNext()) {
            insn = it.next();
            if (isCallToCtor(insn, owner.superName)) {
               break;
            }
        }
        assert insn != null && insn.getType() == AbstractInsnNode.METHOD_INSN : "Expected call " +
                "to constructor but couldn't find it.";
        return (MethodInsnNode) insn;
    }

    /**
     * Skips instructions, starting at the given one, until the constructor call to the given
     * class is reached.
     *
     * @param insn The instruction from which to start looking for the constructor.
     * @param owner The class that owns the constructor.
     * @return The instruction that calls the constructor.
     */
    public static MethodInsnNode skipToCtorCall(AbstractInsnNode insn, ClassNode owner) {
        while (insn != null) {
            insn = insn.getNext();
            if (isCallToCtor(insn, owner.name)) {
                break;
            }
        }
        assert insn != null && insn.getType() == AbstractInsnNode.METHOD_INSN : "Expected call " +
                "to constructor but couldn't find it.";
        return (MethodInsnNode) insn;
    }

    // Checks if the given instruction is a call to the given classes constructor (i.e., <init>).
    public static boolean isCallToCtor(AbstractInsnNode insn, String ownerInternalName) {
        return insn.getType() == AbstractInsnNode.METHOD_INSN
                && ((MethodInsnNode) insn).owner.equals(ownerInternalName)
                && ((MethodInsnNode) insn).name.equals(INSTANCE_CTOR);
    }

    // Adds an opcode that reverses the ordering of the arguments on the evaluation stack
    // according to the number of arguments the called method takes.
    public static void addReverseArguments(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
        int paramsCount = Type.getMethodType(calledAsmMethod.desc).getArgumentTypes().length;
        if ((calledAsmMethod.access & Opcodes.ACC_STATIC) == 0) {
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

    /**
     * Assumes that the given ASM method has one or more {@link Instruction} annotations and adds
     * those instructions to the given {@code NeoMethod}.
     *
     * @param asmMethod The ASM method with the annotation(s).
     * @param neoMethod The {@code NeoMethod} that the instructions will be added to.
     */
    public static void addInstructionsFromAnnotation(MethodNode asmMethod, NeoMethod neoMethod) {
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

        byte[] operandPrefix = new byte[]{};
        if (insnAnnotation.values.contains(INSTRUCTION_ANNOTATION_OPERAND_PREFIX)) {
            operandPrefix = getOperandPrefix(insnAnnotation);
        }
        byte[] operand = new byte[]{};
        if (insnAnnotation.values.contains(INSTRUCTION_ANNOTATION_OPERAND)) {
            operand = getOperand(insnAnnotation);
        }
        // Correctness of operand prefix and operand are checked in the NeoInstruction.
        neoMethod.addInstruction(new NeoInstruction(opcode, operandPrefix, operand));
    }

    private static byte[] getOperandPrefix(AnnotationNode insnAnnotation) {
        return getInstructionOperandBytes(insnAnnotation, INSTRUCTION_ANNOTATION_OPERAND_PREFIX);
    }

    private static byte[] getOperand(AnnotationNode insnAnnotation) {
        return getInstructionOperandBytes(insnAnnotation, INSTRUCTION_ANNOTATION_OPERAND);
    }

    private static byte[] getInstructionOperandBytes(AnnotationNode insnAnnotation,
            String instructionAnnotationOperandPrefix) {
        byte[] operandPrefix = new byte[]{};
        int idx = insnAnnotation.values.indexOf(instructionAnnotationOperandPrefix);
        Object prefixObj = insnAnnotation.values.get(idx+1);
        if (prefixObj instanceof byte[]) {
            operandPrefix = (byte[]) prefixObj;
        } else if (prefixObj instanceof List) {
            List<?> prefixObjAsList = (List<?>) prefixObj;
            operandPrefix = new byte[prefixObjAsList.size()];
            int i = 0;
            for (Object element : prefixObjAsList) {
                operandPrefix[i++] = (byte) element;
            }
        }
        return operandPrefix;
    }

    public static void addPushNumber(long number, NeoMethod neoMethod) {
        neoMethod.addInstruction(buildPushNumberInstruction(BigInteger.valueOf(number)));
    }

    public static NeoInstruction buildPushNumberInstruction(BigInteger number) {
        byte[] insnBytes = new ScriptBuilder().pushInteger(number)
                .toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        return new NeoInstruction(OpCode.get(insnBytes[0]), operand);
    }

    /**
     * Checks if the given class is an event.
     *
     * @param classInternalName instructions under inspection.
     * @return true, if the given class is an event. False, otherwise.
     */
    public static boolean isEvent(String classInternalName) {

        Class<?> clazz;
        try {
            clazz = Class.forName(getFullyQualifiedNameForInternalName(classInternalName));
        } catch (ClassNotFoundException e) {
            return false;
        }
        return clazz.getInterfaces() != null && clazz.getInterfaces().length == 1
                && clazz.getInterfaces()[0].equals(Event.class);
    }

}
