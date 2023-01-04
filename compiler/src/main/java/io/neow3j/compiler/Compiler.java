package io.neow3j.compiler;

import io.neow3j.compiler.converters.Converter;
import io.neow3j.compiler.converters.ConverterMap;
import io.neow3j.compiler.sourcelookup.ISourceContainer;
import io.neow3j.contract.NefFile;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.InteropInterface;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.annotations.ContractSourceCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Instruction.Instructions;
import io.neow3j.devpack.events.EventInterface;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.ClassUtils;
import io.neow3j.utils.Numeric;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.neow3j.compiler.AsmHelper.getAnnotationNode;
import static io.neow3j.compiler.AsmHelper.getAnnotations;
import static io.neow3j.compiler.AsmHelper.getAsmClass;
import static io.neow3j.compiler.AsmHelper.getByteArrayAnnotationProperty;
import static io.neow3j.compiler.AsmHelper.getStringAnnotationProperty;
import static io.neow3j.compiler.DebugInfo.buildDebugInfo;
import static io.neow3j.utils.ClassUtils.getFullyQualifiedNameForInternalName;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Compiler {

    public static final String COMPILER_NAME = "neow3j-3.19.4";

    // Check the following table for a complete version list:
    // https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html#jvms-4.1-200-B.2
    // 52 = Java 1.8
    public static final int CLASS_VERSION_SUPPORTED = 52;

    public static final int MAX_PARAMS_COUNT = 255;
    public static final int MAX_LOCAL_VARIABLES = 255;
    public static final int MAX_STATIC_FIELDS = 255;

    public static final String INSTANCE_CTOR = "<init>";
    private static final String CLASS_CTOR = "<clinit>";
    public static final String THIS_KEYWORD = "this";

    public static final String INSN_ANNOTATION_OPCODE = "opcode";
    public static final String INSN_ANNOTATION_OPERAND = "operand";
    public static final String INSN_ANNOTATION_OPERAND_PREFIX = "operandPrefix";
    public static final String INSN_ANNOTATION_INTEROPSERVICE = "interopService";

    private static final String ASSERTIONS_DISABLED = "$assertionsDisabled";

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
                || typeName.equals(byte[].class.getTypeName())
                || typeName.equals(ByteString.class.getTypeName())) {
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
        if (typeName.equals(io.neow3j.devpack.List.class.getTypeName())
                || typeName.equals(io.neow3j.devpack.Iterator.Struct.class.getTypeName())) {
            return ContractParameterType.ARRAY;
        }
        try {
            typeName = getFullyQualifiedNameForInternalName(type.getInternalName());
            Class<?> clazz = Class.forName(typeName);
            if (Arrays.asList(clazz.getInterfaces()).contains(InteropInterface.class)) {
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

    /**
     * Maps the given Java type to the corresponding neo-vm stack item type.
     * <p>
     * Not every Java type has a specific matching stack item type. For those the stack item type
     * {@link StackItemType#ANY} is returned.
     *
     * @param type The Java type.
     * @return the corresponding stack item type.
     */
    public static StackItemType mapTypeToStackItemType(Type type) {
        String typeName = type.getClassName();
        if (typeName.equals(String.class.getTypeName()) ||
                typeName.equals(Hash160.class.getTypeName()) ||
                typeName.equals(Hash256.class.getTypeName()) ||
                typeName.equals(ECPoint.class.getTypeName()) ||
                typeName.equals(ByteString.class.getTypeName())) {
            return StackItemType.BYTE_STRING;
        }
        if (typeName.equals(Integer.class.getTypeName()) ||
                typeName.equals(int.class.getTypeName()) ||
                typeName.equals(Long.class.getTypeName()) ||
                typeName.equals(long.class.getTypeName()) ||
                typeName.equals(Byte.class.getTypeName()) ||
                typeName.equals(byte.class.getTypeName()) ||
                typeName.equals(Short.class.getTypeName()) ||
                typeName.equals(short.class.getTypeName()) ||
                typeName.equals(Character.class.getTypeName()) ||
                typeName.equals(char.class.getTypeName())) {
            return StackItemType.INTEGER;
        }
        if (typeName.equals(Boolean.class.getTypeName()) || typeName.equals(boolean.class.getTypeName())) {
            return StackItemType.BOOLEAN;
        }
        if (typeName.equals(Byte[].class.getTypeName()) || typeName.equals(byte[].class.getTypeName())) {
            return StackItemType.BUFFER;
        }
        if (typeName.equals(Map.class.getTypeName())) {
            return StackItemType.MAP;
        }
        if (typeName.equals(io.neow3j.devpack.List.class.getTypeName())) {
            // The io.neow3j.devpack.List type is simply an array-abstraction.
            return StackItemType.ARRAY;
        }
        if (typeName.equals(InteropInterface.class.getTypeName())) {
            return StackItemType.INTEROP_INTERFACE;
        }
        if (typeName.equals(io.neow3j.devpack.Iterator.Struct.class.getTypeName())) {
            return StackItemType.STRUCT;
        }
        try {
            typeName = type.getDescriptor().replace("/", ".");
            Class<?> clazz = Class.forName(typeName);
            if (clazz.isArray()) {
                return StackItemType.ARRAY;
            }
        } catch (ClassNotFoundException ignore) {
        }
        // If the type is Object or any other class.
        return StackItemType.ANY;
    }


    /**
     * Compiles the given contract class to neo-vm code and generates debug information with the help of the given
     * source containers.
     * <p>
     * Make sure that the {@code Classloader} used to initialize this {@code Compiler} includes the paths to the
     * given class files.
     *
     * @param contractClass    The fully qualified name of the contract class.
     * @param sourceContainers A list of source containers used for generating debugging information.
     * @return the compilation results.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public CompilationUnit compile(String contractClass, List<ISourceContainer> sourceContainers) throws IOException {
        compUnit.addSourceContainers(sourceContainers);
        return compile(contractClass);
    }

    /**
     * Replaces placeholder strings in the contract class according to {@code replaceMap} and compiles the contract
     * to neo-vm code and generates debug information with the help of the given source containers.
     * <p>
     * Make sure that the {@code Classloader} used to initialize this {@code Compiler} includes the paths to the
     * given class files.
     *
     * @param contractClass    the fully qualified name of the contract class.
     * @param sourceContainers a list of source containers used for generating debugging information.
     * @param replaceMap       the {@link java.util.Map} mapping placeholder strings to the desired values.
     * @return the compilation results.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public CompilationUnit compile(String contractClass, List<ISourceContainer> sourceContainers,
            java.util.Map<String, String> replaceMap) throws IOException {
        compUnit.addSourceContainers(sourceContainers);
        return compile(contractClass, replaceMap);
    }

    /**
     * Compiles the given contract class to neo-vm code.
     *
     * @param contractClass the fully qualified name of the contract class.
     * @return the compilation unit holding the NEF and contract manifest.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public CompilationUnit compile(String contractClass) throws IOException {
        return compile(getAsmClass(contractClass, compUnit.getClassLoader()));
    }

    /**
     * Replaces placeholder strings in the contract class according to {@code replaceMap} and compiles the contract
     * to neo-vm code.
     *
     * @param contractClass the fully qualified name of the contract class.
     * @param replaceMap    the {@link java.util.Map} mapping placeholder strings to the desired values.
     * @return the compilation unit holding the NEF and contract manifest.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public CompilationUnit compile(String contractClass, java.util.Map<String, String> replaceMap) throws IOException {
        return compile(getAsmClass(contractClass, compUnit.getClassLoader()), replaceMap);
    }

    /**
     * Compiles the given contract class to neo-vm code.
     *
     * @param classStream the {@link InputStream} pointing to a contract class file.
     * @return the compilation unit holding the NEF and contract manifest.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public CompilationUnit compile(InputStream classStream) throws IOException {
        return compile(getAsmClass(classStream));
    }

    /**
     * Replaces placeholder strings in the contract class according to {@code replaceMap} and compiles the contract
     * to neo-vm code.
     *
     * @param classStream the {@link InputStream} pointing to a contract class file.
     * @param replaceMap  the {@link java.util.Map} mapping placeholder strings to the desired values.
     * @return the compilation unit holding the NEF and contract manifest.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public CompilationUnit compile(InputStream classStream, java.util.Map<String, String> replaceMap)
            throws IOException {
        return compile(getAsmClass(classStream), replaceMap);
    }

    /**
     * Replaces placeholder strings in the contract class according to {@code replaceMap} and compiles the contract
     * to neo-vm code.
     *
     * @param classNode  the {@link ClassNode} representing a contract class.
     * @param replaceMap the {@link java.util.Map} mapping placeholder strings to the desired values.
     * @return the compilation unit holding the NEF and contract manifest.
     * @throws IOException if an error occurs when trying to read class files.
     */
    protected CompilationUnit compile(ClassNode classNode, java.util.Map<String, String> replaceMap)
            throws IOException {
        substitutePlaceholdersInMethodBodies(classNode, replaceMap);
        substitutePlaceholdersInAnnotations(classNode, replaceMap);
        return compile(classNode);
    }

    private static void substitutePlaceholdersInMethodBodies(ClassNode classNode,
            java.util.Map<String, String> replaceMap) {

        classNode.methods.forEach((methodNode) -> {
            for (AbstractInsnNode insnNode : methodNode.instructions) {
                if (insnNode.getType() == AbstractInsnNode.LDC_INSN) {
                    LdcInsnNode node = (LdcInsnNode) insnNode;
                    if (node.cst instanceof String && replaceMap.containsKey(node.cst)) {
                        node.cst = replaceMap.get(node.cst);
                    }
                }
            }
        });
    }

    private static void substitutePlaceholdersInAnnotations(ClassNode classNode,
            java.util.Map<String, String> replaceMap) {

        List<AnnotationNode> annotations = new ArrayList<>();
        if (classNode.invisibleAnnotations != null) {
            annotations.addAll(classNode.invisibleAnnotations);
        }
        annotations.addAll(classNode.fields.stream().filter(f -> f.invisibleAnnotations != null)
                .flatMap(f -> f.invisibleAnnotations.stream()).collect(Collectors.toList()));
        annotations.addAll(classNode.methods.stream().filter(m -> m.invisibleAnnotations != null)
                .flatMap(m -> m.invisibleAnnotations.stream()).collect(Collectors.toList()));

        annotations.forEach((it) -> processAnnotationNode(it, replaceMap));
    }

    @SuppressWarnings("unchecked")
    private static void processAnnotationNode(AnnotationNode annotationNode, java.util.Map<String, String> replaceMap) {
        // safety check
        if (annotationNode.values == null || annotationNode.values.size() % 2 != 0) {
            return;
        }

        // for each name-value pair
        for (int i = 0; i < annotationNode.values.size(); i += 2) {
            // The value might be different types
            Object value = annotationNode.values.get(i + 1);
            if (value == null) {
                continue;
            }

            // We only focused on String, AnnotationNode, List<String> and List<AnnotationNode>
            if (value instanceof String) {
                // do the modification
                if (replaceMap.containsKey(value)) {
                    annotationNode.values.set(i + 1, replaceMap.get(value));
                }
            } else if (value instanceof AnnotationNode) {
                processAnnotationNode((AnnotationNode) value, replaceMap);
            } else if (value instanceof List) {
                List<Object> casted = (List<Object>) value;
                for (int j = 0; j < casted.size(); j++) {
                    Object elem = casted.get(j);
                    if (elem instanceof String) {
                        // do the modification
                        if (replaceMap.containsKey(elem)) {
                            casted.set(j, replaceMap.get(elem));
                        }
                    } else if (elem instanceof AnnotationNode) {
                        processAnnotationNode((AnnotationNode) elem, replaceMap);
                    }
                }
            }
        }
    }

    /**
     * Compiles the given contract class to neo-vm code.
     *
     * @param classNode the {@link ClassNode} representing a contract class.
     * @return the compilation unit holding the NEF and contract manifest.
     * @throws IOException if an error occurs when trying to read class files.
     */
    protected CompilationUnit compile(ClassNode classNode) throws IOException {
        checkForClassCompatibility(classNode);
        checkForUsageOfInstanceConstructor(classNode);
        checkForNonStaticVariablesOnContractClass(classNode);

        compUnit.setContractClass(classNode);
        collectSmartContractEvents(classNode);
        List<NeoMethod> initializedNeoMethods = initializeContractMethods(classNode);
        compUnit.getNeoModule().addMethods(initializedNeoMethods);
        // Need to create a new list from the methods that have been added to the NeoModule so far because we are
        // potentially adding new methods to the module in the compilation, which leads to concurrency errors.
        ArrayList<NeoMethod> neoMethods = new ArrayList<>(compUnit.getNeoModule().getSortedMethods());
        for (NeoMethod neoMethod : neoMethods) {
            neoMethod.convert(compUnit);
        }
        compileInitsslotMethod();
        finalizeCompilation();
        return compUnit;
    }

    public static boolean isAssertionDisabledStaticField(AbstractInsnNode insn) {
        if (insn.getType() != AbstractInsnNode.FIELD_INSN) {
            return false;
        }
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        return fieldInsn.name.equals(ASSERTIONS_DISABLED);
    }

    private void checkForNonStaticVariablesOnContractClass(ClassNode contractClass) {
        if (contractClass.fields.stream().anyMatch(f -> (f.access & Opcodes.ACC_STATIC) == 0)) {
            throw new CompilerException(format("Contract class %s has non-static fields but only static fields are " +
                            "supported in smart contract classes.",
                    getFullyQualifiedNameForInternalName(contractClass.name)));
        }
    }

    /**
     * Creates the method {@code INITSSLOT} that initialzes static variables in the NeoVM script. Currently, the
     * compiler only considers static variables from the main contract class. Static variables in other classes lead
     * to a compiler exception if they are non-final or final but not of constant value (e.g., set via method call).
     *
     * @throws IOException if an error occurs when trying to read class files.
     */
    private void compileInitsslotMethod() throws IOException {
        Optional<MethodNode> classCtorOpt = compUnit.getContractClass().methods.stream()
                .filter(m -> m.name.equals(CLASS_CTOR))
                .findFirst();
        if (!classCtorOpt.isPresent()) {
            return;
        }
        InitsslotNeoMethod m = new InitsslotNeoMethod(classCtorOpt.get(), compUnit.getContractClass(), compUnit);
        if (m.containsOnlyAssertionRelatedInstructions()) {
            return;
        }
        compUnit.getNeoModule().addMethod(m);
        m.convert(compUnit);
    }

    private void finalizeCompilation() {
        compUnit.getNeoModule().finalizeModule();
        String sourceUrl = getSourceUrl(compUnit.getContractClass());
        NefFile nef = new NefFile(COMPILER_NAME, sourceUrl, compUnit.getNeoModule().getMethodTokens(),
                compUnit.getNeoModule().toByteArray());
        ContractManifest manifest = ManifestBuilder.buildManifest(compUnit);
        compUnit.setNef(nef);
        compUnit.setManifest(manifest);
        compUnit.setDebugInfo(buildDebugInfo(compUnit));
    }

    private String getSourceUrl(ClassNode contractClass) {
        Optional<AnnotationNode> a = getAnnotationNode(contractClass, ContractSourceCode.class);
        return a.map(annotationNode -> (String) annotationNode.values.get(1)).orElse(null);
    }

    private void collectSmartContractEvents(ClassNode asmClass) {
        if (asmClass.fields == null || asmClass.fields.size() == 0) {
            return;
        }
        List<FieldNode> eventFields = asmClass.fields
                .stream()
                .filter(field -> isEvent(field.desc, this.compUnit))
                .collect(Collectors.toList());

        if (eventFields.size() == 0) {
            return;
        }
        eventFields.forEach(f -> compUnit.getNeoModule().addEvent(new NeoEvent(f, asmClass)));
    }

    /**
     * Checks if there are any instructions in the {@code <init>} method of the given class (the instance initializer)
     * besides the call to the {@code Object} constructor (i.e., only line, label, frame, and return instructions are
     * allowed).
     * <p>
     * If instructions are found, an exception is thrown because the compiler does not support instance constructors.
     *
     * @param asmClass the asm class.
     */
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
                    throw new CompilerException(format("Class %s has an explicit instance constructor, which is not " +
                                    "supported.",
                            getFullyQualifiedNameForInternalName(asmClass.name)));
                }
                insn = insn.getNext();
            }
        }
    }

    /**
     * Checks the minimum version of class compatibility. Currently, only 'target compatibility' for 1.8 is supported.
     *
     * @param asmClass the asm class.
     */
    private void checkForClassCompatibility(ClassNode asmClass) {
        if (asmClass.version != CLASS_VERSION_SUPPORTED) {
            throw new CompilerException(format("Class %s was compiled with JVM version %d, which is not supported. " +
                            "Please, change your environment to compile the class to version %d.",
                    getFullyQualifiedNameForInternalName(asmClass.name),
                    asmClass.version,
                    CLASS_VERSION_SUPPORTED
            )
            );
        }
    }

    /**
     * Collects all static method and initializes them, e.e.g, sets the parameters and local variables.
     *
     * @param asmClass the asm class.
     * @return a list of all initialized Neo methods.
     */
    private List<NeoMethod> initializeContractMethods(ClassNode asmClass) {
        List<NeoMethod> methods = new ArrayList<>();
        for (MethodNode asmMethod : asmClass.methods) {
            if (asmMethod.name.equals(INSTANCE_CTOR) || asmMethod.name.equals(CLASS_CTOR)) {
                continue; // Handled in method `compileInitsslotMethod()`.
            }
            if ((asmMethod.access & Opcodes.ACC_STATIC) == 0) {
                throw new CompilerException(asmClass, format("Method '%s' of class %s is non-static but only static " +
                                "methods are allowed in smart contracts.",
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

    /**
     * Handles and/or converts the given instructions. The provided {@code NeoMethod} is the method that the
     * instruction belongs to and the converted instruction will be added to that method.
     *
     * @param insn      the instruction to start from.
     * @param neoMethod the Neo method.
     * @param compUnit  the compilation unit.
     * @return the last instruction node that was processed, i.e., the returned instruction can be used to obtain the
     * next instruction that should be processed.
     * @throws IOException if an error occurs when trying to read class files.
     */
    public static AbstractInsnNode handleInsn(AbstractInsnNode insn, NeoMethod neoMethod, CompilationUnit compUnit)
            throws IOException {

        if (insn.getType() == AbstractInsnNode.LINE) {
            neoMethod.setCurrentLine(((LineNumberNode) insn).line);
        }
        if (insn.getType() == AbstractInsnNode.LABEL) {
            neoMethod.setCurrentLabel(((LabelNode) insn).getLabel());
        }
        if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
            throwIfObjectIsOwner((MethodInsnNode) insn);
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

    private static void throwIfObjectIsOwner(MethodInsnNode insn) {
        if (getFullyQualifiedNameForInternalName(insn.owner)
                .equals(Object.class.getCanonicalName())) {
            throw new CompilerException("Inherited methods that are not specifically implemented are not supported. " +
                    "Implement the method '" + insn.name + "' without a 'super' call to the class Object to use it.");
        }
    }

    /**
     * Checks if the {@code callee} has the {@link Instruction} or {@link Instructions} annotation and, if yes,
     * processes it.
     *
     * @param callee the method being called by {@code caller}.
     * @param caller the calling method.
     */
    public static void processInstructionAnnotations(MethodNode callee, NeoMethod caller) {
        List<AnnotationNode> nodes = getAnnotations(callee, Instruction.class, Instructions.class);
        if (isSingleSyscallInstruction(nodes)) {
            // Needs special treatment because syscall arguments have to be reversed.
            int paramsCount = Type.getMethodType(callee.desc).getArgumentTypes().length;
            if ((callee.access & Opcodes.ACC_STATIC) == 0 && !callee.name.equals(INSTANCE_CTOR)) {
                // The called method has a `this` parameter and is not a constructor.
                paramsCount++;
            }
            addReverseArguments(caller, paramsCount);
        }
        // Otherwise, it's a mix of any kind of instructions. Even if a syscall is included, the
        // reversal of its arguments is up to the developer of the annotated method.
        nodes.forEach(a -> addInstruction(a, caller));
    }

    /**
     * Checks if the list of annotations only contains one annotation that represents a {@link OpCode#SYSCALL}
     * instruction.
     *
     * @param annotations the list to check.
     * @return true if the annotations only contain one syscall instruction. False, otherwise.
     */
    private static boolean isSingleSyscallInstruction(List<AnnotationNode> annotations) {
        if (annotations.size() != 1) {
            return false;
        }
        String name = getStringAnnotationProperty(annotations.get(0), INSN_ANNOTATION_INTEROPSERVICE);
        return name != null && !InteropService.valueOf(name).equals(InteropService.DUMMY);
    }

    /**
     * Adds an instruction that reverses the ordering of the parameters on the evaluation stack according to the
     * number of parameters the called method takes.
     *
     * @param calledAsmMethod  the method that is being called.
     * @param callingNeoMethod the calling method that will be extended with the instruction.
     */
    public static void addReverseArguments(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
        int paramsCount = Type.getMethodType(calledAsmMethod.desc).getArgumentTypes().length;
        if ((calledAsmMethod.access & Opcodes.ACC_STATIC) == 0) {
            // The called method is an instance method, i.e., the instance itself ("this") is also an argument.
            paramsCount++;
        }
        addReverseArguments(callingNeoMethod, paramsCount);
    }

    /**
     * Adds an instruction that reverses the ordering of the parameters on the evaluation stack according to the
     * given number of parameters.
     *
     * @param callingNeoMethod the calling method that will be extended with the instruction.
     * @param paramsCount      the number of parameters passed to the called method.
     */
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

    private static void addInstruction(AnnotationNode annotation, NeoMethod neoMethod) {
        // If the Instruction annotation was used without setting any of its properties the annotations values will
        // be null. This can be treated as no operation.
        if (annotation.values == null) {
            return;
        }

        // First check if the `interopService` property was used and if yes set the SYSCALL.
        String name = getStringAnnotationProperty(annotation, INSN_ANNOTATION_INTEROPSERVICE);
        if (name != null) {
            InteropService interopService = InteropService.valueOf(name);
            if (!interopService.equals(InteropService.DUMMY)) {
                byte[] hash = Numeric.hexStringToByteArray(interopService.getHash());
                neoMethod.addInstruction(new NeoInstruction(OpCode.SYSCALL, hash));
                return;
            }
        }

        String opcodeName = getStringAnnotationProperty(annotation, INSN_ANNOTATION_OPCODE);
        OpCode opcode = OpCode.valueOf(opcodeName);
        if (opcode.equals(OpCode.NOP)) {
            return;
        }

        byte[] operandPrefix = new byte[]{};
        if (annotation.values.contains(INSN_ANNOTATION_OPERAND_PREFIX)) {
            operandPrefix = getByteArrayAnnotationProperty(annotation,
                    INSN_ANNOTATION_OPERAND_PREFIX);
        }
        byte[] operand = new byte[]{};
        if (annotation.values.contains(INSN_ANNOTATION_OPERAND)) {
            operand = getByteArrayAnnotationProperty(annotation, INSN_ANNOTATION_OPERAND);
        }
        // Correctness of operand prefix and operand are checked in the NeoInstruction.
        neoMethod.addInstruction(new NeoInstruction(opcode, operandPrefix, operand));
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
            throw new CompilerException(neoMethod, "Found use of float number but the compiler does not support " +
                    "floats.");
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
     * <p>
     * There should always be a super call even if it was not explicitly specified by the developer.
     *
     * @param constructor the constructor method.
     * @param owner       the class of the constructor method.
     * @return the instruction that calls the super constructor.
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
        assert insn != null && insn.getType() == AbstractInsnNode.METHOD_INSN : "Expected call to constructor but " +
                "couldn't find it.";
        return (MethodInsnNode) insn;
    }

    /**
     * Skips instructions, starting at the given one, until the constructor call to the given class is reached.
     *
     * @param insn  the instruction from which to start looking for the constructor.
     * @param owner the class that owns the constructor.
     * @return the instruction that calls the constructor.
     */
    public static MethodInsnNode skipToCtorCall(AbstractInsnNode insn, ClassNode owner) {
        while (insn != null) {
            insn = insn.getNext();
            if (isCallToCtor(insn, owner.name)) {
                break;
            }
        }
        assert insn != null && insn.getType() == AbstractInsnNode.METHOD_INSN : "Expected call to constructor but " +
                "couldn't find it.";
        return (MethodInsnNode) insn;
    }

    /**
     * Checks if the given instruction is a call to the given class' constructor (i.e., {@code <init>}).
     *
     * @param insn              the instruction.
     * @param ownerInternalName the owner's internal name.
     * @return true if the given instruction is a call to the given class' constructor. False, otherwise.
     */
    public static boolean isCallToCtor(AbstractInsnNode insn, String ownerInternalName) {
        return insn.getType() == AbstractInsnNode.METHOD_INSN &&
                ((MethodInsnNode) insn).owner.equals(ownerInternalName) &&
                ((MethodInsnNode) insn).name.equals(INSTANCE_CTOR);
    }

    /**
     * Adds an instruction to push the given number on the stack.
     *
     * @param number    the number to push on the stack.
     * @param neoMethod the Neo method to add the instruction to.
     */
    public static void addPushNumber(long number, NeoMethod neoMethod) {
        neoMethod.addInstruction(buildPushNumberInstruction(BigInteger.valueOf(number)));
    }

    /**
     * Builds an instruction that pushes the given number on the stack.
     *
     * @param number the number to push on the stack.
     * @return the {@link NeoInstruction} that pushes the given number on the stack.
     */
    public static NeoInstruction buildPushNumberInstruction(BigInteger number) {
        byte[] insnBytes = new ScriptBuilder().pushInteger(number).toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        return new NeoInstruction(OpCode.get(insnBytes[0]), operand);
    }

    /**
     * Checks if the given class descriptor belongs to an event class.
     *
     * @param classDesc the descriptor.
     * @param compUnit  the compilation unit required for the classloader.
     * @return true if the class descriptor is from an event class. False otherwise.
     */
    public static boolean isEvent(String classDesc, CompilationUnit compUnit) {
        char firstChar = classDesc.charAt(0);
        if (AsmHelper.PRIMITIVE_TYPE_NAMES.contains(firstChar) || firstChar == '[') {
            return false;
        }
        try {
            return AsmHelper.getAsmClassForDescriptor(classDesc, compUnit.getClassLoader()).interfaces.stream()
                    .map(ClassUtils::getFullyQualifiedNameForInternalName)
                    .anyMatch(i -> i.equals(EventInterface.class.getName()));
        } catch (IOException e) {
            throw new RuntimeException("Failed fetching class " + classDesc, e);
        }
    }

}
