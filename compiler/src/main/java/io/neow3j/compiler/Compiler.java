package io.neow3j.compiler;

import static io.neow3j.compiler.AsmHelper.getAsmClass;
import static io.neow3j.compiler.DebugInfo.buildDebugInfo;
import static io.neow3j.constants.OpCode.getOperandSize;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.objectweb.asm.Type.getInternalName;

import io.neow3j.compiler.converters.Converter;
import io.neow3j.compiler.converters.ConverterMap;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.NefFile.Version;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.devpack.ScriptContainer;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Instruction.Instructions;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.annotations.Syscall.Syscalls;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.utils.ClassUtils;
import io.neow3j.utils.Numeric;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
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

    private CompilationUnit compUnit;

    public Compiler() {
        compUnit = new CompilationUnit(this.getClass().getClassLoader());
    }

    public Compiler(ClassLoader classLoader) {
        compUnit = new CompilationUnit(classLoader);
    }

    /**
     * Compiles the Java files in the given directory and subdirectories to neo-vm code and produces
     * debugging information for usage with the Neo Debugger.
     *
     * @param sourceFileDir The directory to look for Java smart contract files.
     * @return the compilation results.
     * @throws IOException if something goes wrong when reading Java and class files from disk.
     */
    public CompilationUnit compileJavaFiles(String sourceFileDir) throws IOException {
        Neow3jJavaCompiler javac = new Neow3jJavaCompiler();
        List<File> sourceFiles = new ArrayList<>();
        // Collect source files in the source file directory.
        Files.walkFileTree(new File(sourceFileDir).toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().endsWith(".java")) {
                    sourceFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        // Compile all source files.
        StandardJavaFileManager fileManager = javac.addSources(sourceFiles).compileAll();

        // Add the class output directory to the class loader.
        List<URL> classDirs = new ArrayList<>();
        for (File f : fileManager.getLocation(StandardLocation.CLASS_OUTPUT)) {
            classDirs.add(f.toURI().toURL());
        }
        compUnit.setClassLoader(new URLClassLoader(
                classDirs.toArray(new URL[]{}), compUnit.getClassLoader()));

        // Get all compiled class files and
        Iterable<JavaFileObject> classFiles = fileManager.list(StandardLocation.CLASS_OUTPUT, "",
                Collections.singleton(Kind.CLASS), true);
        List<ClassNode> asmClasses = new ArrayList<>();
        for (JavaFileObject classFile : classFiles) {
            ClassNode asmClass = getAsmClass(classFile.openInputStream());
            asmClasses.add(asmClass);
            String sourceFileName = asmClass.sourceFile.substring(0,
                    asmClass.sourceFile.indexOf("."));
            JavaFileObject sourceFile = fileManager.getJavaFileForInput(
                    StandardLocation.SOURCE_PATH, sourceFileName, Kind.SOURCE);
            compUnit.addClassToSourceMapping(
                    ClassUtils.getFullyQualifiedNameForInternalName(asmClass.name),
                    sourceFile.getName());
        }

        for (ClassNode asmClass : asmClasses) {
            compileClass(asmClass);
        }
        return compUnit;
    }

    /**
     * Converts the JVM class files in the given directory and subdirectories to a neo-vm script. No
     * debugging information is created because the source files are unknown.
     *
     * @param classFileDir The directory to look for class files.
     * @return The compilation result.
     * @throws IOException if something goes wrong when reading Java and class files from disk.
     */
    public CompilationUnit compileClassFiles(String classFileDir) throws IOException {
        // Add the classes dir to the class loader.
        compUnit.setClassLoader(new URLClassLoader(new URL[]{new File(classFileDir)
                .toURI().toURL()},
                compUnit.getClassLoader()));
        // Add mappings from class names to source files for later use in the debug information.
        List<File> classFiles = new ArrayList<>();
        Files.walkFileTree(new File(classFileDir).toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().endsWith(".class")) {
                    classFiles.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        for (File classFile : classFiles) {
            compileClass(new FileInputStream(classFile));
        }
        return compUnit;
    }

    /**
     * Compiles the given class to NeoVM code.
     *
     * @param fullyQualifiedClassName the fully qualified name of the class.
     * @return the compilation unit holding the NEF and contract manifest.
     */
    public CompilationUnit compileClass(String fullyQualifiedClassName) throws IOException {
        return compileClass(getAsmClass(fullyQualifiedClassName, compUnit.getClassLoader()));
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
     * @param classNode the {@link ClassNode} representing a class file.
     * @return the compilation unit holding the NEF and contract manifest.
     */
    private CompilationUnit compileClass(ClassNode classNode) throws IOException {
        compUnit.addContractClass(classNode);
        collectAndInitializeStaticFields(classNode);
        collectAndInitializeMethods(classNode);
        // Need to create a new list from the methods that have been added to the NeoModule so
        // far because we are potentially adding new methods to the module in the compilation,
        // which leads to concurrency errors.
        for (NeoMethod neoMethod : new ArrayList<>(compUnit.getNeoModule().getSortedMethods())) {
            compileMethod(neoMethod, compUnit);
        }
        compUnit.getNeoModule().finalizeModule();
        NefFile nef = new NefFile(COMPILER_NAME, COMPILER_VERSION,
                compUnit.getNeoModule().toByteArray());
        ContractManifest manifest = ManifestBuilder.buildManifest(compUnit,
                nef.getScriptHash());
        compUnit.setNef(nef);
        compUnit.setManifest(manifest);
        compUnit.setDebugInfo(buildDebugInfo(compUnit));
        return compUnit;
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
        checkForUsageOfInstanceConstructor(asmClass);
        NeoMethod neoMethod = createInitsslotMethod(asmClass);
        compUnit.getNeoModule().addMethod(neoMethod);
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
            AbstractInsnNode insn = findSuperCallToObjectCtor(instanceCtor.get());
            insn = insn.getNext();
            while (insn != null) {
                if (insn.getType() != AbstractInsnNode.LINE &&
                        insn.getType() != AbstractInsnNode.LABEL &&
                        insn.getType() != AbstractInsnNode.FRAME &&
                        insn.getOpcode() != JVMOpcode.RETURN.getOpcode()) {
                    throw new CompilerException(format("Class %s has an explicit instance "
                                    + "constructor, which is supported by the compiler.",
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
        neoMethod.setName(INITSSLOT_METHOD_NAME);
        neoMethod.setIsAbiMethod(true);
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
            if (!compUnit.getNeoModule().hasMethod(NeoMethod.getMethodId(asmMethod, asmClass))) {
                NeoMethod neoMethod = new NeoMethod(asmMethod, asmClass);
                MethodInitializer.initializeMethod(neoMethod, compUnit);
                compUnit.getNeoModule().addMethod(neoMethod);
            }
        }
    }

    public static void compileMethod(NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {
        AbstractInsnNode insn = neoMethod.getAsmMethod().instructions.get(0);
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
            throw new CompilerException(compUnit, neoMethod,
                    format("Unsupported instruction %s.", opcode.toString()));
        }
        return converter.convert(insn, neoMethod, compUnit);
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
        if (typeName.equals(ScriptContainer.class.getTypeName())) {
            return ContractParameterType.INTEROP_INTERFACE;
        }
        try {
            typeName = type.getDescriptor().replace("/", ".");
            Class<?> clazz = Class.forName(typeName);
            if (clazz.isArray()) {
                return ContractParameterType.ARRAY;
            }
        } catch (ClassNotFoundException e) {
            throw new CompilerException(e);
        }
        throw new CompilerException("Unsupported type: " + type.getClassName());
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
