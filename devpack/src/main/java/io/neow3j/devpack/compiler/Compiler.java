package io.neow3j.devpack.compiler;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.NefFile.Version;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.contract.ScriptHash;
import io.neow3j.devpack.framework.annotations.EntryPoint;
import io.neow3j.devpack.framework.annotations.Instruction;
import io.neow3j.devpack.framework.annotations.Instruction.Instructions;
import io.neow3j.devpack.framework.annotations.ManifestExtra.ManifestExtras;
import io.neow3j.devpack.framework.annotations.ManifestFeature;
import io.neow3j.devpack.framework.annotations.Syscall;
import io.neow3j.devpack.framework.annotations.Syscall.Syscalls;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest.ContractABI;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest.ContractFeatures;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest.ContractGroup;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState.ContractManifest.ContractPermission;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Compiler {

    private static final Logger log = LoggerFactory.getLogger(Compiler.class);

    public static final String COMPILER_NAME = "neow3j";
    public static final Version COMPILER_VERSION = new Version(0, 0, 0, 0);

    public static final int MAX_PARAMS_COUNT = 255;
    public static final int MAX_LOCAL_VARIABLES_COUNT = 255;

    private static final String CONSTRUCTOR_NAME = "<init>";

    private int currentNeoAddr;
    private NeoModule neoModule;

    /**
     * Compiles the class with the given name to NeoVM code.
     *
     * @param name the fully qualified name of the class.
     */
    public CompilationResult compileClass(String name) throws IOException {
        ClassNode asmClass = getAsmClass(name);
        this.neoModule = new NeoModule(asmClass);
        collectContractMethods(asmClass);
        for (NeoMethod neoMethod : this.neoModule.methods.values()) {
            compileMethod(neoMethod);
        }
        byte[] script = neoModule.toByteArray();
        // TODO: Get offsets of methods and set them in the manifest.
        NefFile nef = new NefFile(COMPILER_NAME, COMPILER_VERSION, script);
        return new CompilationResult(nef, buildManifest(asmClass, nef.getScriptHash()));
    }

    private void collectContractMethods(ClassNode asmClass) {
        boolean entryPointFound = false;
        for (MethodNode asmMethod : asmClass.methods) {
            if (CONSTRUCTOR_NAME.equals(asmMethod.name)) {
                // Constructors of the smart contract are ignored.
                continue;
            }
            NeoMethod neoMethod = new NeoMethod(asmMethod, asmClass);
            this.neoModule.addMethod(neoMethod);
            if (isEntryPoint(asmMethod)) {
                if (entryPointFound) {
                    throw new CompilerException("Multiple entry points found. Only one method of "
                            + "a smart contract can be the entry point.");
                }
                neoMethod.isEntryPoint = true;
                entryPointFound = true;
            }
        }
        if (!entryPointFound) {
            throw new CompilerException("No entry point found. Specify one method as the entry "
                    + "point of the smart contract.");
        }
    }

    private ClassNode getAsmClass(String name) throws IOException {
        ClassReader reader = new ClassReader(name);
        ClassNode asmClass = new ClassNode();
        reader.accept(asmClass, 0);
        return asmClass;
    }

    private boolean isEntryPoint(MethodNode asmMethod) {
        return asmMethod.invisibleAnnotations != null && asmMethod.invisibleAnnotations.stream()
                .anyMatch(a -> a.desc.equals(Type.getDescriptor(EntryPoint.class)));
    }

    private void compileMethod(NeoMethod neoMethod) throws IOException {
        initializeMethod(neoMethod.asmMethod, neoMethod);
        for (int insnAddr = 0; insnAddr < neoMethod.asmMethod.instructions.size(); insnAddr++) {
            int skipAddresses = handleInsn(neoMethod, neoMethod.asmMethod, insnAddr);
            insnAddr += skipAddresses;
        }
    }

    // Returns the number additional addresses that where processed when handling the given
    // instruction. This usually means that this number of addresses does not have to processed
    // again in the calling method.
    private int handleInsn(NeoMethod neoMethod, MethodNode asmMethod, int insnAddr)
            throws IOException {

        int skipAddresses = 0;
        AbstractInsnNode insn = asmMethod.instructions.get(insnAddr);
        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        if (opcode == null) {
            return skipAddresses;
        }
        log.info(opcode.toString());
        switch (opcode) {
            case INVOKESTATIC:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
                handleMethodInstruction(insn, neoMethod);
                break;
            case LDC:
                addLoadConstant(insn, neoMethod);
                break;
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                addPushNumber(opcode.getOpcode() - 3, neoMethod);
                break;
            case BIPUSH: // Has an operand with an int value from -128 to 127.
            case SIPUSH: // Has an operand with an int value from -32768 to 32767.
                addPushNumber(((IntInsnNode) insn).operand, neoMethod);
                break;
            case RETURN:
            case IRETURN:
            case ARETURN:
            case FRETURN:
            case DRETURN:
            case LRETURN:
                neoMethod.addInstruction(new NeoInstruction(OpCode.RET, this.currentNeoAddr++));
                break;
            case ASTORE:
            case ASTORE_0:
            case ASTORE_1:
            case ASTORE_2:
            case ASTORE_3:
            case ISTORE:
            case ISTORE_0:
            case ISTORE_1:
            case ISTORE_2:
            case ISTORE_3:
            case LSTORE:
            case LSTORE_0:
            case LSTORE_1:
            case LSTORE_2:
            case LSTORE_3:
                addStoreLocalVariable(insn, neoMethod);
                break;
            case ALOAD:
            case ALOAD_1:
            case ALOAD_2:
            case ALOAD_3:
            case ILOAD:
            case ILOAD_1:
            case ILOAD_2:
            case ILOAD_3:
            case LLOAD:
            case LLOAD_1:
            case LLOAD_2:
            case LLOAD_3:
                // Load a variable from the local variable pool. Such a variable can be a
                // method parameter or a normal variable in the method body. The index of the
                // variable in the pool is given in the instruction and not on the operand
                // stack.
                addLoadLocalVariable(insn, neoMethod);
                break;
            case NEWARRAY:
            case ANEWARRAY:
                neoMethod.addInstruction(
                        new NeoInstruction(OpCode.NEWARRAY, this.currentNeoAddr++));
                break;
            case DUP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.DUP, this.currentNeoAddr++));
                break;
            case BASTORE:
            case IASTORE:
            case AASTORE:
            case CASTORE:
            case LASTORE:
            case SASTORE:
                // Store an element in an array. Before calling this OpCode an array references
                // and an index must have been pushed onto the operand stack. JVM opcodes
                // `DASTORE` and `FASTORE` are not covered because NeoVM does not support
                // doubles and floats.
                neoMethod.addInstruction(
                        new NeoInstruction(OpCode.SETITEM, this.currentNeoAddr++));
                break;
            case PUTFIELD:
                addSetField(insn, neoMethod);
                break;
            case POP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.DROP,
                        this.currentNeoAddr++));
                break;
            case CHECKCAST:
                // Check if the object on the operand stack can be cast to a given type.
                // There is no corresponding NeoVM opcode.
                break;
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case IALOAD:
            case LALOAD:
            case SALOAD:
                // Load an element from an array. Before calling this OpCode an array references
                // and an index must have been pushed onto the operand stack. JVM and NeoVM both
                // place the loaded element onto the operand stack. JVM opcodes `DALOAD` and
                // `FALOAD` are not covered because NeoVM does not support doubles and floats.
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICKITEM,
                        this.currentNeoAddr++));
                break;
            case GETFIELD:
                // Get a field variable from an object. The index of the field inside the
                // object is given with the instruction and the object itself must be on top
                // of the operand stack.
                addGetField(insn, neoMethod);
                break;
            case I2B:
                // Convert an integer to byte by truncating. Because integers and bytes are
                // handled equally by NeoVM there is nothing to do.
                break;
            case NEW:
                handleNew(insn, neoMethod);
                break;
            default:
                throw new CompilerException("Unsupported instruction " + opcode + " in: " +
                        asmMethod.name + ".");
        }
        return skipAddresses;
    }

    private void addSetField(AbstractInsnNode insn, NeoMethod neoMethod) throws IOException {
        // NeoVM sets fields of objects simply by calling SETITEM. The operand stack has to have
        // an index on top that is used by SETITEM. We get this index from the class to which the
        // field belongs to.
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn);
        addPushNumber(idx, neoMethod);
        // SETITEM expects the item to be ontop of the stack (item -> index -> array)
        neoMethod.addInstruction(new NeoInstruction(OpCode.SWAP, this.currentNeoAddr++));
        neoMethod.addInstruction(new NeoInstruction(OpCode.SETITEM, this.currentNeoAddr++));
    }

    private void addGetField(AbstractInsnNode insn, NeoMethod neoMethod) throws IOException {
        // NeoVM gets fields of objects simply by calling PICKITEM. The operand stack has to have
        // an index on top that is used by PICKITEM. We get this index from the class to which the
        // field belongs to.
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn);
        addPushNumber(idx, neoMethod);
        neoMethod.addInstruction(new NeoInstruction(OpCode.PICKITEM, this.currentNeoAddr++));
    }

    private int getFieldIndex(FieldInsnNode fieldInsn) throws IOException {
        ClassNode owner = getAsmClass(Type.getObjectType(fieldInsn.owner).getClassName());
        int idx = 0;
        for (FieldNode field : owner.fields) {
            if (field.name.equals(fieldInsn.name)) {
                break;
            }
            idx++;
        }
        return idx;
    }

    private void handleNew(AbstractInsnNode insn, NeoMethod neoMethod) throws IOException {
        // Get the number of fields in the object that is instantiated with the NEW opcode.
        TypeInsnNode typeInsn = (TypeInsnNode) insn;
        ClassNode type = getAsmClass(Type.getObjectType(typeInsn.desc).getClassName());
        addPushNumber(type.fields.size(), neoMethod);
        neoMethod.addInstruction(new NeoInstruction(OpCode.NEWARRAY, this.currentNeoAddr++));
        // TODO: Clarify when we need to use `NEWSTRUCT`.
//        if (type.DeclaringType.IsValueType) {
//            Insert1(VM.OpCode.NEWSTRUCT, null, to);
//        }
        // The NEW JVMopcode is followed by a DUP JVMopcode which will be handled correctly by
        // `compileMethod()`. After that some other opcodes can follow but at some point a
        // INVOKESPECIAL opcode will follow that calls the type's constructor.
    }

    // Checks for method params and local variables, sets them on the NeoMethod object and adds
    // the INITSLOT opcode to initialize variable slots in the NeoVM.
    private void initializeMethod(MethodNode asmMethod, NeoMethod neoMethod) {
        if (asmMethod.localVariables == null || asmMethod.localVariables.size() == 0) {
            return;
        }
        int paramCount = 0;
        if ("this".equals(asmMethod.localVariables.get(0).name)) {
            paramCount++;
        }
        paramCount += Type.getArgumentTypes(asmMethod.desc).length;
        int localVarCount = asmMethod.localVariables.size() - paramCount;
        if (paramCount > MAX_PARAMS_COUNT) {
            throw new CompilerException("The method has more than the max number of "
                    + "parameters.");
        }
        if (localVarCount > MAX_LOCAL_VARIABLES_COUNT) {
            throw new CompilerException("The method has more than the max number of local "
                    + "variables.");
        }
        for (int i = 0; i < paramCount; i++) {
            LocalVariableNode varNode = asmMethod.localVariables.get(i);
            neoMethod.addParameter(new NeoVariable(i, varNode.index, varNode));
        }
        for (int i = paramCount; i < asmMethod.localVariables.size(); i++) {
            LocalVariableNode varNode = asmMethod.localVariables.get(i);
            neoMethod.addVariable(new NeoVariable(i - paramCount, varNode.index, varNode));
        }
        if (paramCount + localVarCount > 0) {
            NeoInstruction neoInsn = new NeoInstruction(OpCode.INITSLOT,
                    new byte[]{(byte) localVarCount, (byte) paramCount}, currentNeoAddr);
            neoMethod.addInstruction(neoInsn);
            currentNeoAddr++;
        }
    }

    private int extractPushedNumber(NeoInstruction insn) {
        if (insn.opcode.getCode() <= OpCode.PUSHINT256.getCode()) {
            return BigIntegers.fromLittleEndianByteArray(insn.operand).intValue();
        }
        if (insn.opcode.getCode() >= OpCode.PUSHM1.getCode()
                && insn.opcode.getCode() <= OpCode.PUSH16.getCode()) {
            return insn.opcode.getCode() - OpCode.PUSHM1.getCode() - 1;
        }
        throw new CompilerException(
                "Couldn't parse get number from instruction " + insn.toString());
    }

    private void addLoadLocalVariable(AbstractInsnNode insn, NeoMethod neoMethod) {
        VarInsnNode varInsn = (VarInsnNode) insn;
        if (varInsn.var >= MAX_LOCAL_VARIABLES_COUNT) {
            throw new CompilerException("Local variable index to high. Was " + varInsn + " but "
                    + "maximally " + MAX_LOCAL_VARIABLES_COUNT + " local variables are supported.");
        }
        // The local variable can either be a method parameter or a normal variable defined in
        // the method body. The NeoMethod has been initialized with all the local variables.
        // Therefore, we can check here if it is a parameter or a normal variable and treat it
        // accordingly.
        NeoVariable param = neoMethod.getParameterByJVMIndex(varInsn.var);
        if (param != null) {
            neoMethod.addInstruction(buildStoreOrLoadVariableInsn(param.index, OpCode.LDARG));
        } else {
            NeoVariable var = neoMethod.getVariableByJVMIndex(varInsn.var);
            neoMethod.addInstruction(buildStoreOrLoadVariableInsn(var.index, OpCode.LDLOC));
        }
    }

    private void addStoreLocalVariable(AbstractInsnNode insn, NeoMethod neoMethod) {
        VarInsnNode varInsn = (VarInsnNode) insn;
        if (varInsn.var >= MAX_LOCAL_VARIABLES_COUNT) {
            throw new CompilerException("Local variable index to high. Was " + varInsn + " but "
                    + "maximally " + MAX_LOCAL_VARIABLES_COUNT + " local variables are supported.");
        }
        NeoVariable var = neoMethod.getVariableByJVMIndex(varInsn.var);
        neoMethod.addInstruction(buildStoreOrLoadVariableInsn(var.index, OpCode.STLOC));
    }

    private NeoInstruction buildStoreOrLoadVariableInsn(int index, OpCode opcode) {
        NeoInstruction neoInsn;
        if (index <= 6) {
            OpCode storeCode = OpCode.get(opcode.getCode() - 7 + index);
            neoInsn = new NeoInstruction(storeCode, this.currentNeoAddr++);
        } else {
            byte[] operand = new byte[]{(byte) index};
            neoInsn = new NeoInstruction(opcode, operand, this.currentNeoAddr);
            this.currentNeoAddr += 2;
        }
        return neoInsn;
    }

    private void addLoadConstant(AbstractInsnNode insn, NeoMethod neoMethod) {
        assert insn.getType() == AbstractInsnNode.LDC_INSN : "Instruction type doesn't match "
                + "opcode.";
        LdcInsnNode ldcInsn = (LdcInsnNode) insn;
        if (ldcInsn.cst instanceof String) {
            addPushDataArray(((String) ldcInsn.cst).getBytes(UTF_8), neoMethod);
        }
        if (ldcInsn.cst instanceof Integer) {
            addPushNumber(((Integer) ldcInsn.cst), neoMethod);
        }
        if (ldcInsn.cst instanceof Long) {
            addPushNumber(((Long) ldcInsn.cst), neoMethod);
        }
        // TODO: Handle other types.
    }

    private void addPushDataArray(byte[] data, NeoMethod neoMethod) {
        byte[] insnBytes = new ScriptBuilder().pushData(data).toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        neoMethod.addInstruction(new NeoInstruction(
                OpCode.get(insnBytes[0]), operand, this.currentNeoAddr));
        this.currentNeoAddr += insnBytes.length;
    }

    private void handleMethodInstruction(AbstractInsnNode insn, NeoMethod invokingNeoMethod)
            throws IOException {

        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        ClassNode owner = getAsmClass(Type.getObjectType(methodInsn.owner).getClassName());
        MethodNode invokedAsmMethod = owner.methods.stream()
                .filter(m -> m.desc.equals(methodInsn.desc) && m.name.equals(methodInsn.name))
                .findFirst().get();
        if (hasSyscallAnnotation(invokedAsmMethod)) {
            addSyscall(invokedAsmMethod, invokingNeoMethod);
        } else if (hasInstructionAnnotation(invokedAsmMethod)) {
            addInstruction(invokedAsmMethod, invokingNeoMethod);
        } else {
            addCallMethod(invokingNeoMethod, owner, invokedAsmMethod);
        }
    }

    // Checks if the called method is already in the NeoModule. If yes, then simply adds a call
    // opcode with that method. If not, creates a new NeoMethod, adds it to the module, and
    // compiles it to NeoVM code.
    private void addCallMethod(NeoMethod invokingNeoMethod, ClassNode owner,
            MethodNode invokedAsmMethod) throws IOException {
        String invokedMethodId = NeoMethod.getMethodId(invokedAsmMethod, owner);
        if (this.neoModule.methods.containsKey(invokedMethodId)) {
            NeoMethod invokedNeoMethod = this.neoModule.methods.get(invokedMethodId);
            NeoInstruction invokeInsn = new NeoInstruction(OpCode.CALL, this.currentNeoAddr++)
                    .setExtra(invokedNeoMethod);
            invokingNeoMethod.addInstruction(invokeInsn);
        } else {
            NeoMethod invokedNeoMethod = new NeoMethod(invokedAsmMethod, owner);
            this.neoModule.addMethod(invokedNeoMethod);
            if (invokedAsmMethod.name.equals(CONSTRUCTOR_NAME)) {
                handleConstructorCall(invokedNeoMethod);
            }
            compileMethod(invokedNeoMethod);
        }
    }

    // Searches for the call to the Object() super constructor and removes all instructions up
    // to it (including the super call itself).
    private void handleConstructorCall(NeoMethod neoMethod) throws IOException {
        String objectName = Type.getInternalName(Object.class);
        boolean hasSuperCallToObject = false;
        Iterator<AbstractInsnNode> it = neoMethod.asmMethod.instructions.iterator();
        while(it.hasNext()) {
            AbstractInsnNode insn = it.next();
            it.remove();
            if (insn.getType() == AbstractInsnNode.METHOD_INSN
                    && ((MethodInsnNode) insn).name.equals(CONSTRUCTOR_NAME)
                    && ((MethodInsnNode) insn).owner.equals(objectName)) {
                hasSuperCallToObject = true;
                break;
            }
        }
        if (!hasSuperCallToObject) {
            throw new CompilerException("Expected call to super constructor but couldn't "
                    + "find it.");
        }
    }

    private boolean hasSyscallAnnotation(MethodNode asmMethod) {
        return asmMethod.invisibleAnnotations != null && asmMethod.invisibleAnnotations.stream()
                .anyMatch(a -> a.desc.equals(Type.getDescriptor(Syscalls.class))
                        || a.desc.equals(Type.getDescriptor(Syscall.class)));
    }

    private boolean hasInstructionAnnotation(MethodNode asmMethod) {
        return asmMethod.invisibleAnnotations != null && asmMethod.invisibleAnnotations.stream()
                .anyMatch(a -> a.desc.equals(Type.getDescriptor(Instructions.class))
                        || a.desc.equals(Type.getDescriptor(Instruction.class)));
    }

    private void addSyscall(MethodNode asmMethod, NeoMethod neoMethod) {
        // Before doing the syscall the arguments have to be reversed. Additionally, the Opcode
        // NOP is inserted before every Syscall.
        neoMethod.addInstruction(new NeoInstruction(OpCode.NOP, this.currentNeoAddr++));
        int paramsCount = Type.getMethodType(asmMethod.desc).getArgumentTypes().length;
        if (paramsCount == 2) {
            neoMethod.addInstruction(new NeoInstruction(OpCode.SWAP, this.currentNeoAddr++));
        } else if (paramsCount == 3) {
            neoMethod.addInstruction(new NeoInstruction(OpCode.REVERSE3, this.currentNeoAddr++));
        } else if (paramsCount == 4) {
            neoMethod.addInstruction(new NeoInstruction(OpCode.REVERSE4, this.currentNeoAddr++));
        } else if (paramsCount > 4) {
            addPushNumber(paramsCount, neoMethod);
            neoMethod.addInstruction(new NeoInstruction(OpCode.REVERSEN, this.currentNeoAddr++));
        }

        // Annotation has to be either Syscalls or Syscall.
        AnnotationNode syscallAnnotation = asmMethod.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Syscalls.class))
                        || a.desc.equals(Type.getDescriptor(Syscall.class)))
                .findFirst().get();
        if (syscallAnnotation.desc.equals(Type.getDescriptor(Syscalls.class))) {
            // handle multiple syscalls
            assert syscallAnnotation.values.size() == 2;
            for (Object a : (List<?>) syscallAnnotation.values.get(1)) {
                addSingleSyscall((AnnotationNode) a, neoMethod);
            }
        } else {
            // handle single syscall
            addSingleSyscall(syscallAnnotation, neoMethod);
        }
    }

    private void addSingleSyscall(AnnotationNode syscallAnnotation, NeoMethod neoMethod) {
        String syscallName = ((String[]) syscallAnnotation.values.get(1))[1];
        InteropServiceCode syscall = InteropServiceCode.valueOf(syscallName);
        byte[] hash = Numeric.hexStringToByteArray(syscall.getHash());
        neoMethod.addInstruction(new NeoInstruction(OpCode.SYSCALL, hash, this.currentNeoAddr));
        this.currentNeoAddr += 1 + hash.length;
    }

    private void addInstruction(MethodNode asmMethod, NeoMethod neoMethod) {
        AnnotationNode insnAnnotation = asmMethod.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Instructions.class))
                        || a.desc.equals(Type.getDescriptor(Instruction.class)))
                .findFirst().get();
        if (insnAnnotation.desc.equals(Type.getDescriptor(Instructions.class))) {
            // handle multiple instructions
            for (Object a : (List<?>) insnAnnotation.values.get(1)) {
                addSingleInstruction((AnnotationNode) a, neoMethod);
            }
        } else {
            // handle single instruction
            addSingleInstruction(insnAnnotation, neoMethod);
        }

    }

    private void addSingleInstruction(AnnotationNode insnAnnotation, NeoMethod neoMethod) {
        String insnName = ((String[]) insnAnnotation.values.get(1))[1];
        OpCode opCode = OpCode.valueOf(insnName);
        // TODO: Get OpCode operand
        byte[] operand = new byte[]{};
        neoMethod.addInstruction(new NeoInstruction(opCode, operand, this.currentNeoAddr));
        this.currentNeoAddr += 1 + operand.length;
    }

    private void addPushNumber(long number, NeoMethod neoMethod) {
        byte[] insnBytes = new ScriptBuilder().pushInteger(BigInteger.valueOf(number)).toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        neoMethod.addInstruction(new NeoInstruction(
                OpCode.get(insnBytes[0]), operand, this.currentNeoAddr));
        this.currentNeoAddr += insnBytes.length;
    }

    private ContractManifest buildManifest(ClassNode classNode, ScriptHash scriptHash) {
        List<ContractGroup> groups = new ArrayList<>();
        ContractFeatures features = buildContractFeatures(classNode);
        ContractABI abi = buildABI(classNode, scriptHash);
        List<ContractPermission> permissions = Arrays.asList(
                new ContractPermission("*", Arrays.asList("*")));
        List<String> trusts = new ArrayList<>();
        List<String> safeMethods = new ArrayList<>();
        Map<String, String> extras = buildManifestExtra(classNode);
        return new ContractManifest(groups, features, abi, permissions, trusts, safeMethods,
                extras);
    }

    private ContractABI buildABI(ClassNode classNode, ScriptHash scriptHash) {
        List<ContractMethod> methods = new ArrayList<>();
        List<ContractEvent> events = new ArrayList<>();
        // TODO: Fill events list.
        for (MethodNode asmMethod : classNode.methods) {
            if (CONSTRUCTOR_NAME.equals(asmMethod.name)) {
                continue; // Skip the constructor.
            }
            // TODO: Make this compatible with non-static methods too. In that case the first
            //  local variable is the 'this' object.
            int paramsCount = Type.getArgumentTypes(asmMethod.desc).length;
            ContractParameter[] params = new ContractParameter[paramsCount];
            for (int i = 0; i < paramsCount; i++) {
                LocalVariableNode varNode = asmMethod.localVariables.get(i);
                ContractParameterType paramType =
                        mapTypeToParameterType(Type.getType(varNode.desc));
                params[i] = new ContractParameter(varNode.name, paramType, null);
            }
            ContractParameterType paramType = mapTypeToParameterType(
                    Type.getMethodType(asmMethod.desc).getReturnType());
            // TODO: Set the correct method offset.
            methods.add(new ContractMethod(asmMethod.name, Arrays.asList(params), paramType, 0));
        }
        return new ContractABI(scriptHash.toString(), methods, events);
    }

    private ContractParameterType mapTypeToParameterType(Type type) {
        String typeName = type.getClassName();
        if (typeName.equals(String.class.getTypeName())) {
            return ContractParameterType.STRING;
        }
        if (typeName.equals(Integer.class.getTypeName())
                || typeName.equals(int.class.getTypeName())
                || typeName.equals(Long.class.getTypeName())
                || typeName.equals(long.class.getTypeName())
                || typeName.equals(Byte.class.getTypeName())
                || typeName.equals(byte.class.getTypeName())) {
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

    private ContractFeatures buildContractFeatures(ClassNode n) {
        Optional<AnnotationNode> opt = n.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(ManifestFeature.class)))
                .findFirst();
        boolean payable = false;
        boolean hasStorage = false;
        if (opt.isPresent()) {
            AnnotationNode ann = opt.get();
            int i = ann.values.indexOf("payable");
            payable = i != -1 && (boolean) ann.values.get(i + 1);
            i = ann.values.indexOf("hasStorage");
            hasStorage = i != -1 && (boolean) ann.values.get(i + 1);
        }
        return new ContractFeatures(hasStorage, payable);
    }

    private Map<String, String> buildManifestExtra(ClassNode classNode) {
        Optional<AnnotationNode> opt = classNode.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(ManifestExtras.class)))
                .findFirst();
        if (!opt.isPresent()) {
            return null;
        }
        AnnotationNode ann = opt.get();
        Map<String, String> extras = new HashMap<>();
        String key;
        String value;
        for (Object a : (List<?>) ann.values.get(1)) {
            AnnotationNode manifestExtra = (AnnotationNode) a;
            int i = manifestExtra.values.indexOf("key");
            key = (String) manifestExtra.values.get(i + 1);
            i = manifestExtra.values.indexOf("value");
            value = (String) manifestExtra.values.get(i + 1);
            extras.put(key, value);
        }
        return extras;
    }

    public class CompilationResult {

        private NefFile nef;
        private ContractManifest manifest;

        private CompilationResult(NefFile nef, ContractManifest manifest) {
            this.nef = nef;
            this.manifest = manifest;
        }

        public NefFile getNef() {
            return nef;
        }

        public ContractManifest getManifest() {
            return manifest;
        }
    }
}
