package io.neow3j.compiler;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.NefFile.Version;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.contract.ScriptHash;
import io.neow3j.devpack.framework.ScriptContainer;
import io.neow3j.devpack.framework.annotations.Appcall;
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
import io.neow3j.utils.ArrayUtils;
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
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
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
    public static final Version COMPILER_VERSION = new Version(0, 1, 0, 0);

    public static final int MAX_PARAMS_COUNT = 255;
    public static final int MAX_LOCAL_VARIABLES_COUNT = 255;
    public static final int MAX_STATIC_FIELDS_COUNT = 255;

    private static final String INSTANCE_CTOR = "<init>";
    private static final String CLASS_CTOR = "<clinit>";
    private static final String INITSSLOT_METHOD_NAME = "_initialize";
    private static final String THIS_KEYWORD = "this";
    private static final String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);
    private static final String VALUEOF_METHOD_NAME = "valueOf";
    private static final List<String> PRIMITIVE_TYPE_CAST_METHODS = Arrays.asList(
            "intValue", "longValue", "byteValue", "shortValue", "booleanValue", "charValue");
    private static final List<String> PRIMITIVE_TYPE_WRAPPER_CLASSES = Arrays.asList(
            "java/lang/Integer", "java/lang/Long", "java/lang/Byte", "java/lang/Short",
            "java/lang/Boolean", "java/lang/Character");

    private NeoModule neoModule;

    /**
     * Compiles the class with the given name to NeoVM code.
     *
     * @param name the fully qualified name of the class.
     */
    public CompilationResult compileClass(String name) throws IOException {
        ClassNode asmClass = getAsmClass(name);
        this.neoModule = new NeoModule(asmClass);
        collectAndInitializeStaticFields(asmClass);
        collectAndInitializeMethods(asmClass);
        // Need to create a new list from the methods that have been added to the NeoModule so
        // far because we are potentially adding new methods to the module in the compilation,
        // which leads to concurrency errors.
        for (NeoMethod neoMethod : new ArrayList<>(this.neoModule.methods.values())) {
            compileMethod(neoMethod);
        }
        this.neoModule.finalizeModule();
        byte[] script = this.neoModule.toByteArray();
        NefFile nef = new NefFile(COMPILER_NAME, COMPILER_VERSION, script);
        ContractManifest manifest = buildManifest(this.neoModule, nef.getScriptHash());
        return new CompilationResult(nef, manifest);
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
        // At this point we know that there are only static variables. Check if a static instructor
        // or an instance constructor is used and throw an exception if so. The instructions of the
        // instance ctor and the static ctor are both in the JVM "<init>" method.
        Optional<MethodNode> instanceCtorOpt = asmClass.methods.stream()
                .filter(m -> m.name.equals(INSTANCE_CTOR))
                .findFirst();
        if (instanceCtorOpt.isPresent()) {
            MethodNode instanceCtor = instanceCtorOpt.get();
            removeInsnsUpToObjectCtorCall(instanceCtor);
            if (hasInstructions(instanceCtor)) {
                throw new CompilerException("Class " + asmClass.name + " has an explicit instance "
                        + "constructor or static constructor. But, neither is supported in smart "
                        + "contracts.");
            }
        }

        NeoMethod neoMethod = createInitsslotMethod(asmClass);
        this.neoModule.addMethod(neoMethod);
    }

    // Creates the method (beginning with INITSSLOT) that initializes static variables in the NeoVM
    // script.
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

    private void collectAndInitializeMethods(ClassNode asmClass) {
        boolean entryPointFound = false;
        for (MethodNode asmMethod : asmClass.methods) {
            if (asmMethod.name.equals(INSTANCE_CTOR) || asmMethod.name.equals(CLASS_CTOR)) {
                continue; // Handled in method `collectAndInitializeStaticFields()`.
            }
            if ((asmMethod.access & Opcodes.ACC_STATIC) == 0) {
                throw new CompilerException("Method " + asmClass.name + "." + asmMethod.name
                        + "() is an non-static method but only static smart contract methods are "
                        + "supported.");
            }
            NeoMethod neoMethod = new NeoMethod(asmMethod, asmClass);
            initializeMethod(neoMethod);
            if (entryPointFound && neoMethod.isEntryPoint) {
                throw new CompilerException("Multiple entry points found. Only one method of "
                        + "a smart contract can be the entry point.");
            } else {
                entryPointFound = entryPointFound || neoMethod.isEntryPoint;
            }
            this.neoModule.addMethod(neoMethod);
        }
        if (!entryPointFound) {
            throw new CompilerException("No entry point found. Specify one method as the entry "
                    + "point of the smart contract.");
        }
    }

    private boolean isEntryPoint(MethodNode asmMethod) {
        return asmMethod.invisibleAnnotations != null && asmMethod.invisibleAnnotations.stream()
                .anyMatch(a -> a.desc.equals(Type.getDescriptor(EntryPoint.class)));
    }

    private ClassNode getAsmClass(String name) throws IOException {
        ClassReader reader = new ClassReader(name);
        ClassNode asmClass = new ClassNode();
        reader.accept(asmClass, 0);
        return asmClass;
    }

    private void compileMethod(NeoMethod neoMethod) throws IOException {
        for (int insnAddr = 0; insnAddr < neoMethod.asmMethod.instructions.size(); insnAddr++) {
            AbstractInsnNode insn = neoMethod.asmMethod.instructions.get(insnAddr);
            insnAddr += handleInsn(neoMethod, insn);
        }
    }

    private int handleInsn(NeoMethod neoMethod, AbstractInsnNode insn) throws IOException {
        int skipCount = 0;
        if (insn.getType() == AbstractInsnNode.LINE) {
            neoMethod.currentLine = ((LineNumberNode) insn).line;
        }
        if (insn.getType() == AbstractInsnNode.LABEL) {
            neoMethod.currentLabelNode = (LabelNode) insn;
        }
        JVMOpcode opcode = JVMOpcode.get(insn.getOpcode());
        if (opcode == null) {
            return skipCount;
        }
        log.info(opcode.toString());
        switch (opcode) {

            // region ### OBJECTS ###
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
                handleNew(insn, neoMethod);
                break;
            case ARRAYLENGTH:
            case INSTANCEOF:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
                // endregion ### OBJECTS ###

                // region ### CONSTANTS ###
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
            case LCONST_0:
            case LCONST_1:
                addPushNumber(opcode.getOpcode() - 3, neoMethod);
                break;
            case LDC:
            case LDC_W:
            case LDC2_W:
                addLoadConstant(insn, neoMethod);
                break;
            case ACONST_NULL:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
            case BIPUSH: // Has an operand with an int value from -128 to 127.
            case SIPUSH: // Has an operand with an int value from -32768 to 32767.
                addPushNumber(((IntInsnNode) insn).operand, neoMethod);
                break;
            // endregion ### CONSTANTS ###

            // region ### METHODS ###
            case RETURN:
            case IRETURN:
            case ARETURN:
            case LRETURN:
                neoMethod.addInstruction(new NeoInstruction(OpCode.RET));
                break;
            case INVOKESTATIC:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
                handleMethodInstruction(insn, neoMethod);
                break;
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
                // endregion ### METHODS ###

                // region ### LOCAL VARIABLES ###
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
            case ALOAD_0:
            case ALOAD_1:
            case ALOAD_2:
            case ALOAD_3:
            case ILOAD:
            case ILOAD_0:
            case ILOAD_1:
            case ILOAD_2:
            case ILOAD_3:
            case LLOAD:
            case LLOAD_0:
            case LLOAD_1:
            case LLOAD_2:
            case LLOAD_3:
                // Load a variable from the local variable pool. Such a variable can be a
                // method parameter or a normal variable in the method body. The index of the
                // variable in the pool is given in the instruction and not on the operand
                // stack.
                addLoadLocalVariable(insn, neoMethod);
                break;
            // endregion ### LOCAL VARIABLES ###

            // region ### ARRAYS ###
            case NEWARRAY:
            case ANEWARRAY:
                neoMethod.addInstruction(new NeoInstruction(OpCode.NEWARRAY));
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
                neoMethod.addInstruction(new NeoInstruction(OpCode.SETITEM));
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
                neoMethod.addInstruction(new NeoInstruction(OpCode.PICKITEM));
                break;
            case PUTFIELD:
                // Sets a value for a field variable on an object. The compiler doesn't
                // support non-static variables in the smart contract, but we currently handle this
                // JVM opcode because we support instantiation of simple objects like the
                // `StorageMap`.
                addSetItem(insn, neoMethod);
                break;
            case GETFIELD:
                // Get a field variable from an object. The index of the field inside the
                // object is given with the instruction and the object itself must be on top
                // of the operand stack.
                addGetField(insn, neoMethod);
                break;
            case MULTIANEWARRAY:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
                // endregion ### ARRAYS ###

                // region ### STACK MANIPULATION ###
            case NOP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.NOP));
                break;
            case DUP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.DUP));
                break;
            case POP:
                neoMethod.addInstruction(new NeoInstruction(OpCode.DROP));
                break;
            case POP2:
            case DUP_X1:
            case DUP_X2:
            case DUP2:
            case DUP2_X1:
            case DUP2_X2:
            case SWAP:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
                // endregion ### STACK MANIPULATION ###

                // region ### JUMP OPCODES ###
                // Java jump addresses are restricted to 2 bytes, i.e. there are no 4-byte jump
                // addresses as in NeoVM. It is simpler for the compiler implementation to always
                // use the 4-byte NeoVM jump opcodes and then optimize (to 1-byte addresses) in a
                // second step. This is how the dotnet-devpack handeles it too.
            case IF_ICMPEQ:
            case IF_ACMPEQ: // Object reference comparison.
                addJumpInstruction(neoMethod, insn, OpCode.JMPEQ_L);
                break;
            case IF_ICMPNE:
            case IF_ACMPNE: // Object reference comparison.
                addJumpInstruction(neoMethod, insn, OpCode.JMPNE_L);
                break;
            case IF_ICMPLT:
                addJumpInstruction(neoMethod, insn, OpCode.JMPLT_L);
                break;
            case IF_ICMPGT:
                addJumpInstruction(neoMethod, insn, OpCode.JMPGT_L);
                break;
            case IF_ICMPLE:
                addJumpInstruction(neoMethod, insn, OpCode.JMPLE_L);
                break;
            case IF_ICMPGE:
                addJumpInstruction(neoMethod, insn, OpCode.JMPGE_L);
                break;
            // These opcodes operate on boolean, byte, char, short, and int. In the latter four
            // cases (IFLT, IFLE, IFGT, and IFGE) the NeoVM opcode is switched, e.g., from GT to
            // LE because zero value will be on top of the stack and not the integer value.
            case IFEQ: // Tests if the value on the stack is equal to zero.
            case IFNULL: // Object comparison with null.
                // JMPIFNOT_L means that the process should jump if the value is not set, is null,
                // or is zero.
                addJumpInstruction(neoMethod, insn, OpCode.JMPIFNOT_L);
                break;
            case IFNE: // Tests if the value on the stack is not equal to zero.
            case IFNONNULL: // Object comparison with null.
                // JMPIF_L means that the process should jump if the value is set, is not null, or
                // not zero.
                addJumpInstruction(neoMethod, insn, OpCode.JMPIF_L);
                break;
            case IFLT: // Tests if the value on the stack is less than zero.
                addPushNumber(0, neoMethod);
                addJumpInstruction(neoMethod, insn, OpCode.JMPGT_L);
                break;
            case IFLE: // Tests if the value on the stack is less than or equal to zero.
                addPushNumber(0, neoMethod);
                addJumpInstruction(neoMethod, insn, OpCode.JMPGE_L);
                break;
            case IFGT: // Tests if the value on the stack is greater than zero.
                addPushNumber(0, neoMethod);
                addJumpInstruction(neoMethod, insn, OpCode.JMPLT_L);
                break;
            case IFGE: // Tests if the value on the stack is greater than or equal to zero.
                addPushNumber(0, neoMethod);
                addJumpInstruction(neoMethod, insn, OpCode.JMPLE_L);
                break;

            case LCMP:
                // Comparison of two longs resulting in an integer with value -1, 0, or 1.
                // This opcode has no direct counterpart in NeoVM because NeoVM does not
                // differentiate between int and long.
                skipCount += handleLongComparison(neoMethod, insn);
                break;
            case GOTO:
            case GOTO_W:
                // Unconditionally branch of to another code location.
                neoMethod.addInstruction(new NeoJumpInstruction(OpCode.JMP_L,
                        ((JumpInsnNode) insn).label.getLabel()));
                break;
            case LOOKUPSWITCH:
            case TABLESWITCH:
            case JSR:
            case RET:
            case JSR_W:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
                // endregion ### JUMP OPCODES ###

                // region ### CONVERSION ###
            case I2B:
            case L2I:
            case I2L:
            case I2C:
            case I2S:
                // Nothing to do because the NeoVM treats these types all the same.
                break;
            // endregion ### CONVERSION ###

            // region ### ARITHMETICS ###
            case IINC:
            case IADD:
            case LADD:
            case ISUB:
            case LSUB:
            case IMUL:
            case LMUL:
            case IDIV:
            case LDIV:
            case IREM:
            case LREM:
            case INEG:
            case LNEG:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
                // endregion ### ARITHMETICS ###

                // region ### BIT OPERATIONS ###
            case ISHL:
            case LSHL:
            case ISHR:
            case LSHR:
            case IUSHR:
            case LUSHR:
            case IAND:
            case LAND:
            case IOR:
            case LOR:
            case IXOR:
            case LXOR:
                throw new CompilerException("Instruction " + opcode + " in " +
                        neoMethod.asmMethod.name + " not yet supported.");
                // endregion ### BIT OPERATIONS ###

                // region ### FLOATING POINT (unsupported) ###
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
            case FRETURN:
            case DRETURN:
            case F2I:
            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
            case I2F:
            case I2D:
            case L2F:
            case L2D:
            case FNEG:
            case DNEG:
            case FDIV:
            case DDIV:
            case FREM:
            case DREM:
            case FMUL:
            case DMUL:
            case FSUB:
            case DSUB:
            case FADD:
            case DADD:
            case FASTORE:
            case DASTORE:
            case FALOAD:
            case DALOAD:
            case FSTORE:
            case DSTORE:
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case DCONST_0:
            case DCONST_1:
            case FSTORE_0:
            case FSTORE_1:
            case FSTORE_2:
            case FSTORE_3:
            case DSTORE_0:
            case DSTORE_1:
            case DSTORE_2:
            case DSTORE_3:
            case FLOAD_0:
            case FLOAD_1:
            case FLOAD_2:
            case FLOAD_3:
            case DLOAD_0:
            case DLOAD_1:
            case DLOAD_2:
            case DLOAD_3:
            case FLOAD:
            case DLOAD:
                throw new CompilerException("Floating point numbers are not supported.");
                // endregion ### FLOATING POINT (unsupported) ###

                // region ### MISCELLANEOUS ###
            case ATHROW:
            case MONITORENTER:
            case MONITOREXIT:
            case WIDE:
                // endregion ### MISCELLANEOUS ###

            default:
                throw new CompilerException("Unsupported instruction " + opcode + " in: " +
                        neoMethod.asmMethod.name + ".");
        }
        return skipCount;
    }

    private int handleLongComparison(NeoMethod neoMethod, AbstractInsnNode insn) {
        JumpInsnNode jumpInsn = (JumpInsnNode) insn.getNext();
        JVMOpcode jvmOpcode = JVMOpcode.get(jumpInsn.getOpcode());
        if (jvmOpcode == null) {
            // TODO: Add meaningful exception message.
            throw new CompilerException("");
        }
        switch (jvmOpcode) {
            case IFEQ:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPEQ_L);
                break;
            case IFNE:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPNE_L);
                break;
            case IFLT:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPLT_L);
                break;
            case IFGT:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPGT_L);
                break;
            case IFLE:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPLE_L);
                break;
            case IFGE:
                addJumpInstruction(neoMethod, jumpInsn, OpCode.JMPGE_L);
                break;
            default:
                // TODO: Insert informative exception message.
                throw new CompilerException("");
        }
        return 1;
    }

    private void addJumpInstruction(NeoMethod neoMethod, AbstractInsnNode insn, OpCode jmpOpcode) {
        Label jmpLabel = ((JumpInsnNode) insn).label.getLabel();
        neoMethod.addInstruction(new NeoJumpInstruction(jmpOpcode, jmpLabel));
    }

    private void addLoadStaticField(AbstractInsnNode insn, NeoMethod neoMethod) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, neoMethod.ownerType);
        neoMethod.addInstruction(buildStoreOrLoadVariableInsn(idx, OpCode.LDSFLD));
    }

    private void addStoreStaticField(AbstractInsnNode insn, NeoMethod neoMethod) {
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, neoMethod.ownerType);
        neoMethod.addInstruction(buildStoreOrLoadVariableInsn(idx, OpCode.STSFLD));
    }

    private void addSetItem(AbstractInsnNode insn, NeoMethod neoMethod) {
        // NeoVM doesn't support objects but can imitate them by using  arrays or structs. The
        // field variables of an object then simply becomes an index in the array. This is done
        // with the SETITEM opcode.
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, neoMethod.ownerType);
        addPushNumber(idx, neoMethod);
        // SETITEM expects the item to be on top of the stack (item -> index -> array)
        neoMethod.addInstruction(new NeoInstruction(OpCode.SWAP));
        neoMethod.addInstruction(new NeoInstruction(OpCode.SETITEM));
    }

    private void addGetField(AbstractInsnNode insn, NeoMethod neoMethod) {
        // NeoVM gets fields of objects simply by calling PICKITEM. The operand stack has to have
        // an index on top that is used by PICKITEM. We get this index from the class to which the
        // field belongs to.
        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
        int idx = getFieldIndex(fieldInsn, neoMethod.ownerType);
        addPushNumber(idx, neoMethod);
        neoMethod.addInstruction(new NeoInstruction(OpCode.PICKITEM));
    }

    private int getFieldIndex(FieldInsnNode fieldInsn, ClassNode owner) {
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
        neoMethod.addInstruction(new NeoInstruction(OpCode.NEWARRAY));
        // TODO: Clarify when we need to use `NEWSTRUCT`.
//        if (type.DeclaringType.IsValueType) {
//            Insert1(VM.OpCode.NEWSTRUCT, null, to);
//        }
        // The NEW is followed by a DUP opcode which makes the object reference available to the
        // constructor call. Only in case of empty constructors do we need to remove the DUP opcode
        // because we don't need to call the ctor method. That is handled in `addCallMethod()`.
    }

    private void initializeMethod(NeoMethod neoMethod) {
        if ((neoMethod.asmMethod.access & Opcodes.ACC_PUBLIC) > 0 &&
                neoMethod.ownerType.equals(this.neoModule.asmSmartContractClass)) {
            // Only contract methods that are public and on the smart contract class are added to
            // the ABI and are invokable.
            neoMethod.isAbiMethod = true;
        }
        neoMethod.isEntryPoint = isEntryPoint(neoMethod.asmMethod);

        // Look for method params and local variables and add them to the NeoMethod. Note that
        // JVM does not have a special opcode for method parameters like NeoVM has with LDARG and
        // STARG.
        if (neoMethod.asmMethod.localVariables == null
                || neoMethod.asmMethod.localVariables.size() == 0) {
            return;
        }
        int paramCount = 0;
        if (neoMethod.asmMethod.localVariables.get(0).name.equals(THIS_KEYWORD)) {
            paramCount++;
        }
        paramCount += Type.getArgumentTypes(neoMethod.asmMethod.desc).length;
        int localVarCount = neoMethod.asmMethod.localVariables.size() - paramCount;
        if (paramCount > MAX_PARAMS_COUNT) {
            throw new CompilerException("The method has more than the max number of parameters.");
        }
        if (localVarCount > MAX_LOCAL_VARIABLES_COUNT) {
            throw new CompilerException("The method has more than the max number of local "
                    + "variables.");
        }
        for (int i = 0; i < paramCount; i++) {
            LocalVariableNode varNode = neoMethod.asmMethod.localVariables.get(i);
            neoMethod.addParameter(new NeoVariable(i, varNode.index, varNode));
        }
        for (int i = paramCount; i < neoMethod.asmMethod.localVariables.size(); i++) {
            LocalVariableNode varNode = neoMethod.asmMethod.localVariables.get(i);
            neoMethod.addVariable(new NeoVariable(i - paramCount, varNode.index, varNode));
        }

        // Add the INITSLOT opcode as first instruction of the method if the method has parameters
        // and/or local variables.
        if (paramCount + localVarCount > 0) {
            neoMethod.addInstruction(new NeoInstruction(
                    OpCode.INITSLOT, new byte[]{(byte) localVarCount, (byte) paramCount}));
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
        addLoadOrStoreLocalVariable((VarInsnNode) insn, neoMethod, OpCode.LDARG, OpCode.LDLOC);
    }

    private void addStoreLocalVariable(AbstractInsnNode insn, NeoMethod neoMethod) {
        addLoadOrStoreLocalVariable((VarInsnNode) insn, neoMethod, OpCode.STARG, OpCode.STLOC);
    }

    private void addLoadOrStoreLocalVariable(VarInsnNode insn, NeoMethod neoMethod,
            OpCode argOpcode, OpCode varOpcode) {

        if (insn.var >= MAX_LOCAL_VARIABLES_COUNT) {
            throw new CompilerException("Local variable index to high. Was " + insn + " but "
                    + "maximally " + MAX_LOCAL_VARIABLES_COUNT + " local variables are supported.");
        }
        // The local variable can either be a method parameter or a normal variable defined in
        // the method body. The NeoMethod has been initialized with all the local variables.
        // Therefore, we can check here if it is a parameter or a normal variable and treat it
        // accordingly.
        NeoVariable param = neoMethod.getParameterByJVMIndex(insn.var);
        if (param != null) {
            neoMethod.addInstruction(buildStoreOrLoadVariableInsn(param.index, argOpcode));
        } else {
            NeoVariable var = neoMethod.getVariableByJVMIndex(insn.var);
            neoMethod.addInstruction(buildStoreOrLoadVariableInsn(var.index, varOpcode));
        }
    }

    private NeoInstruction buildStoreOrLoadVariableInsn(int index, OpCode opcode) {
        NeoInstruction neoInsn;
        if (index <= 6) {
            OpCode storeCode = OpCode.get(opcode.getCode() - 7 + index);
            neoInsn = new NeoInstruction(storeCode);
        } else {
            byte[] operand = new byte[]{(byte) index};
            neoInsn = new NeoInstruction(opcode, operand);
        }
        return neoInsn;
    }

    private void addLoadConstant(AbstractInsnNode insn, NeoMethod neoMethod) {
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

    private void addPushDataArray(byte[] data, NeoMethod neoMethod) {
        byte[] insnBytes = new ScriptBuilder().pushData(data).toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        neoMethod.addInstruction(new NeoInstruction(
                OpCode.get(insnBytes[0]), operand));
    }

    private void handleMethodInstruction(AbstractInsnNode insn, NeoMethod callingNeoMethod)
            throws IOException {

        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        ClassNode owner = getAsmClass(Type.getObjectType(methodInsn.owner).getClassName());
        Optional<MethodNode> calledAsmMethodOpt = owner.methods.stream()
                .filter(m -> m.desc.equals(methodInsn.desc) && m.name.equals(methodInsn.name))
                .findFirst();
        // If the called method cannot be found on the owner type, we look through the super
        // types until we find the method.
        while (!calledAsmMethodOpt.isPresent()) {
            if (owner.superName == null) {
                throw new CompilerException("Couldn't find method " + methodInsn.name + " on "
                        + "owning type and its super types.");
            }
            owner = getAsmClass(Type.getObjectType(owner.superName).getClassName());
            calledAsmMethodOpt = owner.methods.stream()
                    .filter(m -> m.desc.equals(methodInsn.desc) && m.name.equals(methodInsn.name))
                    .findFirst();
        }
        MethodNode calledAsmMethod = calledAsmMethodOpt.get();
        if (hasSyscallAnnotation(calledAsmMethod)) {
            addSyscall(calledAsmMethod, callingNeoMethod);
        } else if (hasInstructionAnnotation(calledAsmMethod)) {
            addInstruction(calledAsmMethod, callingNeoMethod);
        } else if (hasAppcallAnnotation(calledAsmMethod)) {
            addAppcall(calledAsmMethod, callingNeoMethod);
        } else {
            handleMethodCall(callingNeoMethod, owner, calledAsmMethod);
        }
    }

    private void handleMethodCall(NeoMethod callingNeoMethod, ClassNode owner,
            MethodNode calledAsmMethod) throws IOException {

        if (isPrimitiveTypeCast(calledAsmMethod, owner)) {
            // Nothing to do if Java casts between primitive type and wrapper classes.
            return;
        }
        // Checks if the called method is already in the NeoModule. If yes, then simply adds a call
        // opcode with that method. If not, creates a new NeoMethod, adds it to the module, and
        // compiles it to NeoVM code.
        String calledMethodId = NeoMethod.getMethodId(calledAsmMethod, owner);
        NeoMethod calledNeoMethod;
        if (this.neoModule.methods.containsKey(calledMethodId)) {
            calledNeoMethod = this.neoModule.methods.get(calledMethodId);
        } else {
            calledNeoMethod = new NeoMethod(calledAsmMethod, owner);
            if (calledAsmMethod.name.equals(INSTANCE_CTOR)) {
                if (!hasInstructions(calledAsmMethod)) {
                    // If the constructor is empty we don't need to parse it. The DUP opcode
                    // added before must be removed again.
                    int lastKey = callingNeoMethod.instructions.lastKey();
                    assert callingNeoMethod.instructions.get(lastKey).opcode.equals(OpCode.DUP);
                    callingNeoMethod.instructions.remove(lastKey);
                    return;
                }
                // After this removal the method will be compiled if it were a normal method.
                removeInsnsUpToObjectCtorCall(calledNeoMethod.asmMethod);
            }
            this.neoModule.addMethod(calledNeoMethod);
            initializeMethod(calledNeoMethod);
            compileMethod(calledNeoMethod);
        }
        addReverseArguments(calledAsmMethod, callingNeoMethod);
        // We explicitly add the 4-element byte array as an operand so that instruction addresses
        // can easily be calculated even before the correct offset is set on the CALL_L opcode.
        // The actual setting of the correct offset happens in NeoModule.
        NeoInstruction invokeInsn = new NeoInstruction(OpCode.CALL_L, new byte[4])
                .setExtra(calledNeoMethod);
        // Note that we never make use of Opcode.CALL because that complicates calculation of
        // instruction addresses in the NeoModule.
        callingNeoMethod.addInstruction(invokeInsn);
    }

    private boolean isPrimitiveTypeCast(MethodNode calledAsmMethod, ClassNode owner) {
        boolean isConversionFromPrimitiveType = calledAsmMethod.name.equals(VALUEOF_METHOD_NAME);
        boolean isConversionToPrimitiveType =
                PRIMITIVE_TYPE_CAST_METHODS.contains(calledAsmMethod.name);
        boolean isOwnerPrimitiveTypeWrapper = PRIMITIVE_TYPE_WRAPPER_CLASSES.contains(owner.name);
        return (isConversionFromPrimitiveType || isConversionToPrimitiveType)
                && isOwnerPrimitiveTypeWrapper;
    }

    private boolean hasInstructions(MethodNode asmMethod) {
        if (asmMethod.instructions == null || asmMethod.instructions.size() == 0) {
            return false;
        }
        return Stream.of(asmMethod.instructions.toArray()).anyMatch(insn ->
                insn.getType() != AbstractInsnNode.LINE &&
                        insn.getType() != AbstractInsnNode.LABEL &&
                        insn.getType() != AbstractInsnNode.FRAME &&
                        insn.getOpcode() != JVMOpcode.RETURN.getOpcode());
    }

    // Goes through the instructions of the given method and looks for the call to the `Object`
    // constructor. Removes all instructions up to it (including the super call itself). Assumes
    // that the constructed class does not extend any other class than `Object`.
    private void removeInsnsUpToObjectCtorCall(MethodNode asmMethod) {
        boolean hasSuperCallToObject = false;
        Iterator<AbstractInsnNode> it = asmMethod.instructions.iterator();
        while (it.hasNext()) {
            AbstractInsnNode insn = it.next();
            it.remove();
            if (insn.getType() == AbstractInsnNode.METHOD_INSN
                    && ((MethodInsnNode) insn).name.equals(INSTANCE_CTOR)
                    && ((MethodInsnNode) insn).owner.equals(OBJECT_INTERNAL_NAME)) {
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

    private boolean hasAppcallAnnotation(MethodNode asmMethod) {
        return asmMethod.invisibleAnnotations != null && asmMethod.invisibleAnnotations.stream()
                .anyMatch(a -> a.desc.equals(Type.getDescriptor(Appcall.class)));
    }

    private void addSyscall(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
        // Before doing the syscall the arguments have to be reversed. Additionally, the Opcode
        // NOP is inserted before every Syscall.
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.NOP));
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

    // Adds an opcode that reverses the ordering of the arguments on the evaluation stack
    // according to the number of arguments the called method takes.
    private void addReverseArguments(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
        int paramsCount = Type.getMethodType(calledAsmMethod.desc).getArgumentTypes().length;
        if (calledAsmMethod.localVariables != null
                && calledAsmMethod.localVariables.size() > 0
                && calledAsmMethod.localVariables.get(0).name.equals(THIS_KEYWORD)) {
            // The called method is an instance method, i.e., the instance itself ("this") is
            // also an argument.
            paramsCount++;
        }
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

    private void addSingleSyscall(AnnotationNode syscallAnnotation, NeoMethod neoMethod) {
        String syscallName = ((String[]) syscallAnnotation.values.get(1))[1];
        InteropServiceCode syscall = InteropServiceCode.valueOf(syscallName);
        byte[] hash = Numeric.hexStringToByteArray(syscall.getHash());
        neoMethod.addInstruction(new NeoInstruction(OpCode.SYSCALL, hash));
    }

    private void addInstruction(MethodNode asmMethod, NeoMethod neoMethod) {
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

    private void addSingleInstruction(AnnotationNode insnAnnotation, NeoMethod neoMethod) {
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

    private byte[] getOperand(AnnotationNode insnAnnotation, OpCode opcode) {
        List<?> operandAsList = (List<?>) insnAnnotation.values.get(3);
        if (operandAsList.size() != OpCode.getOperandSize(opcode).size()) {
            throw new CompilerException("Opcode " + opcode.name() + " was used with a wrong "
                    + "number of operand byts.");
        }
        byte[] operand = new byte[operandAsList.size()];
        int i = 0;
        for (Object element : operandAsList) {
            operand[i++] = (byte) element;
        }
        return operand;
    }

    private void addAppcall(MethodNode calledAsmMethod, NeoMethod callingNeoMethod) {
        addReverseArguments(calledAsmMethod, callingNeoMethod);
        AnnotationNode appCallAnnotation = calledAsmMethod.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Appcall.class))).findFirst().get();
        String scriptHash = (String) appCallAnnotation.values.get(1);
        byte[] data = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(scriptHash));
        addPushDataArray(data, callingNeoMethod);
        byte[] callHash = Numeric.hexStringToByteArray(
                InteropServiceCode.SYSTEM_CONTRACT_CALL.getHash());
        callingNeoMethod.addInstruction(new NeoInstruction(OpCode.SYSCALL, callHash));
    }

    private void addPushNumber(long number, NeoMethod neoMethod) {
        byte[] insnBytes = new ScriptBuilder().pushInteger(BigInteger.valueOf(number)).toArray();
        byte[] operand = Arrays.copyOfRange(insnBytes, 1, insnBytes.length);
        neoMethod.addInstruction(new NeoInstruction(OpCode.get(insnBytes[0]), operand));
    }

    private ContractManifest buildManifest(NeoModule neoModule, ScriptHash scriptHash) {
        List<ContractGroup> groups = new ArrayList<>();
        ContractFeatures features = buildContractFeatures(neoModule.asmSmartContractClass);
        ContractABI abi = buildABI(neoModule, scriptHash);
        List<ContractPermission> permissions = Arrays.asList(
                new ContractPermission("*", Arrays.asList("*")));
        List<String> trusts = new ArrayList<>();
        List<String> safeMethods = new ArrayList<>();
        Map<String, String> extras = buildManifestExtra(neoModule.asmSmartContractClass);
        return new ContractManifest(groups, features, abi, permissions, trusts, safeMethods,
                extras);
    }

    private ContractABI buildABI(NeoModule neoModule, ScriptHash scriptHash) {
        List<ContractMethod> methods = new ArrayList<>();
        // TODO: Fill events list.
        List<ContractEvent> events = new ArrayList<>();
        for (NeoMethod neoMethod : neoModule.methods.values()) {
            if (!neoMethod.isAbiMethod) {
                // TODO: This needs to change when enabling inheritance.
                continue; // Only add methods to the ABI that appear in the contract itself.
            }
            List<ContractParameter> contractParams = new ArrayList<>();
            for (NeoVariable var : neoMethod.parameters) {
                contractParams.add(new ContractParameter(var.asmVariable.name,
                        mapTypeToParameterType(Type.getType(var.asmVariable.desc)), null));
            }
            ContractParameterType paramType = mapTypeToParameterType(
                    Type.getMethodType(neoMethod.asmMethod.desc).getReturnType());
            methods.add(new ContractMethod(neoMethod.name, contractParams, paramType,
                    neoMethod.startAddress));
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

    public static class CompilationResult {

        private final NefFile nef;
        private final ContractManifest manifest;

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
