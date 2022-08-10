package io.neow3j.compiler;

import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageMap;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.OnNEP11Payment;
import io.neow3j.devpack.annotations.OnVerification;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.neow3j.compiler.AsmHelper.extractTypeParametersFromSignature;
import static io.neow3j.compiler.AsmHelper.getAnnotationNode;
import static io.neow3j.compiler.AsmHelper.getAsmClass;
import static io.neow3j.compiler.AsmHelper.getAsmClassForDescriptor;
import static io.neow3j.compiler.AsmHelper.getAsmClassForInternalName;
import static io.neow3j.compiler.AsmHelper.getFieldIndex;
import static io.neow3j.compiler.AsmHelper.getMethodNode;
import static io.neow3j.compiler.AsmHelper.hasAnnotations;
import static io.neow3j.compiler.AsmHelper.stripObjectDescriptor;
import static io.neow3j.compiler.JVMOpcode.PUTSTATIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AsmHelperTest {

    @Test
    public void gettingMethodShouldReturnCorrectMethodNode() throws IOException {
        String desc = "(Lio/neow3j/devpack/StorageContext;Lio/neow3j/devpack/ByteString;)" +
                "Lio/neow3j/devpack/ByteString;";
        String name = "get";
        MethodInsnNode insn = new MethodInsnNode(JVMOpcode.INVOKESTATIC.getOpcode(),
                "io/neow3j/devpack/neo/Storage", "get", desc);
        ClassNode owner = new ClassNode();
        ClassReader r = new ClassReader(Storage.class.getCanonicalName());
        r.accept(owner, 0);

        Optional<MethodNode> method = getMethodNode(insn, owner);
        assertTrue(method.isPresent());
        assertThat(method.get().name, is(name));
        assertThat(method.get().desc, is(desc));
        assertThat(method.get().invisibleAnnotations, hasSize(1)); // The @Syscall annotation.
    }

    @Test
    public void gettingClassForInternalNameShouldReturnTheCorrectClassNode() throws IOException {
        Type t = Type.getType(Storage.class);
        ClassNode c = getAsmClassForInternalName(t.getInternalName(),
                this.getClass().getClassLoader());
        assertThat(c.name, is(Storage.class.getCanonicalName().replace(".", "/")));
        assertThat(c.sourceFile, is("Storage.java"));
        assertThat(c.methods, not(hasSize(0)));
    }

    @Test
    public void gettingClassForFqnShouldReturnTheCorrectClassNode() throws IOException {
        ClassNode c = getAsmClass(Storage.class.getCanonicalName(), this.getClass().getClassLoader());
        assertThat(c.name, is(Storage.class.getCanonicalName().replace(".", "/")));
        assertThat(c.sourceFile, is("Storage.java"));
        assertThat(c.methods, not(hasSize(0)));
    }

    @Test
    public void gettingClassFromInputStreamShouldReturnTheCorrectClassNode() throws IOException {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
                Storage.class.getCanonicalName().replace('.', '/') + ".class");
        ClassNode c = getAsmClass(stream);
        assertThat(c.name, is(Storage.class.getCanonicalName().replace(".", "/")));
        assertThat(c.sourceFile, is("Storage.java"));
        assertThat(c.methods, not(hasSize(0)));
    }

    @Test
    public void hasAnnotationsShouldReturnTrueForAMethodWithGivenAnnotations() throws IOException {
        ClassNode asmClass = getAsmClass(this.getClass().getCanonicalName(),
                this.getClass().getClassLoader());
        MethodNode method = asmClass.methods.stream()
                .filter(m -> m.name.contains("annotatedMethod"))
                .findFirst().get();
        assertTrue(hasAnnotations(method, Instruction.class));
        assertTrue(hasAnnotations(method, OnNEP11Payment.class));
    }

    // This method is used to test annotations.
    @Instruction
    @OnNEP11Payment
    private void annotatedMethod() {

    }

    @Test
    public void gettingFieldIndexShouldReturnCorrectIndex() throws IOException {
//        ClassNode owner = getAsmClass(StorageMap.class.getCanonicalName(),
//                this.getClass().getClassLoader());
        String ownerName = StorageMap.class.getCanonicalName().replace(".", "/");
        FieldInsnNode insn = new FieldInsnNode(PUTSTATIC.getOpcode(), ownerName, "prefix", "[B");
        CompilationUnit compUnit = new CompilationUnit(this.getClass().getClassLoader());
        assertThat(getFieldIndex(insn, compUnit), is(1));
    }

    @Test
    public void extractTypeParamFromSignatureWithOneParam() {
        // One non-generic event parameter
        FieldNode field = new FieldNode(0, null, null,
                "Lio/neow3j/devpack/events/Event1Arg<Ljava/lang/Integer;>;", null);
        List<String> types = extractTypeParametersFromSignature(field);
        assertThat(types.get(0), is("Ljava/lang/Integer;"));
    }

    @Test
    public void extractTypeParamFromSignatureWithTwoParams() {
        // Two non-generic event parameters
        FieldNode field = new FieldNode(0, null, null,
                "Lio/neow3j/devpack/events/Event2Args<Ljava/lang/Integer;Ljava/lang/String;>;",
                null);
        List<String> types = extractTypeParametersFromSignature(field);
        assertThat(types.get(0), is("Ljava/lang/Integer;"));
        assertThat(types.get(1), is("Ljava/lang/String;"));

        // One event parameter with a generic type parameter, i.e., List<Integer>.
        field = new FieldNode(0, null, null, "Lio/neow3j/devpack/events/Event1Arg<"
                + "Lio/neow3j/devpack/List<Ljava/lang/Integer;>;>;", null);
        types = extractTypeParametersFromSignature(field);
        assertThat(types.get(0), is("Lio/neow3j/devpack/List;"));

        // Two event parameters with a generic type parameters.
        field = new FieldNode(0, null, null, "Lio/neow3j/devpack/events/Event1Arg<"
                + "Lio/neow3j/devpack/List<Ljava/lang/Integer;>;"
                + "Lio/neow3j/devpack/List<Ljava/lang/String;>;>;", null);
        types = extractTypeParametersFromSignature(field);
        assertThat(types.get(0), is("Lio/neow3j/devpack/List;"));
        assertThat(types.get(1), is("Lio/neow3j/devpack/List;"));
    }

    @Test
    public void extractTypeParamFromSignatureWithOneParamThatAlsoHasATypeParam() {
        // One event parameter with a generic type parameter, i.e., List<Integer>.
        FieldNode field = new FieldNode(0, null, null, "Lio/neow3j/devpack/events/Event1Arg<"
                + "Lio/neow3j/devpack/List<Ljava/lang/Integer;>;>;", null);
        List<String> types = extractTypeParametersFromSignature(field);
        assertThat(types.get(0), is("Lio/neow3j/devpack/List;"));
    }

    @Test
    public void extractTypeParamFromSignatureWithParamThatHasTwoTypeParamsItself() {
        // One event parameter with two generic type parameters.
        FieldNode field = new FieldNode(0, null, null, "Lio/neow3j/devpack/events/Event2Args<"
                + "Lio/neow3j/devpack/List<Ljava/lang/Integer;>;"
                + "Lio/neow3j/devpack/List<Ljava/lang/String;>;>;", null);
        List<String> types = extractTypeParametersFromSignature(field);
        assertThat(types.get(0), is("Lio/neow3j/devpack/List;"));
        assertThat(types.get(1), is("Lio/neow3j/devpack/List;"));
    }

    @Test
    public void extractTypeParamFromSignatureWithPrimitiveArrayParamAndOtherParams() {
        // One event parameter with two generic type parameters.
        FieldNode field = new FieldNode(0, null, null, "Lio/neow3j/devpack/events/Event2Args<"
                + "[B" + "Lio/neow3j/devpack/List<Ljava/lang/Integer;>;>;",
                null);
        List<String> types = extractTypeParametersFromSignature(field);
        assertThat(types.get(0), is("[B"));
        assertThat(types.get(1), is("Lio/neow3j/devpack/List;"));
    }

    @Test
    public void extractTypeParamFromSignatureWithMultiplePrimitiveArrayParamAndOtherParams() {
        // One event parameter with two generic type parameters.
        FieldNode field = new FieldNode(0, null, null, "Lio/neow3j/devpack/events/Event5Args<"
                + "[B" + "[C" + "[Lio/neow3j/devpack/List<Ljava/lang/Integer;>;" + "[I" +
                "[Ljava/lang/Integer;>;",
                null);
        List<String> types = extractTypeParametersFromSignature(field);
        assertThat(types.get(0), is("[B"));
        assertThat(types.get(1), is("[C"));
        assertThat(types.get(2), is("[Lio/neow3j/devpack/List;"));
        assertThat(types.get(3), is("[I"));
        assertThat(types.get(4), is("[Ljava/lang/Integer;"));
    }

    @Test
    public void gettingClassForDescriptorShouldReturnTheCorrectClassNode() throws IOException {
        Type t = Type.getType(Storage.class);
        ClassNode c = getAsmClassForDescriptor(t.getDescriptor(), this.getClass().getClassLoader());
        assertThat(c.name, is(Storage.class.getCanonicalName().replace(".", "/")));
        assertThat(c.sourceFile, is("Storage.java"));
        assertThat(c.methods, not(hasSize(0)));
    }

    @Test
    public void gettingAnnotationFromAFieldNodeShouldReturnTheCorrectAnnotationNode() {
        FieldNode fieldNode = new FieldNode(0, "variableName",
                "Lio/neow3j/devpack/events/Event2Args;",
                "Lio/neow3j/devpack/events/Event2Args<Ljava/lang/Integer;Ljava/lang/String;>;",
                null);

        AnnotationNode annNode = new AnnotationNode("Lio/neow3j/devpack/annotations/DisplayName;");
        annNode.values = new ArrayList<>();
        annNode.values.add("value");
        annNode.values.add("displayName");
        List<AnnotationNode> annotations = new ArrayList<>();
        annotations.add(annNode);
        fieldNode.invisibleAnnotations = annotations;

        Optional<AnnotationNode> opt = getAnnotationNode(fieldNode, DisplayName.class);
        assertThat(opt.get(), is(annNode));
    }

    @Test
    public void gettingAnnotationFromAMethodNodeShouldReturnTheCorrectAnnotationNode() {
        MethodNode methodNode = new MethodNode(0, "methodName", "()V", null, null);
        List<AnnotationNode> annotations = new ArrayList<>();
        AnnotationNode ann = new AnnotationNode("Lio/neow3j/devpack/annotations/OnVerification;");
        annotations.add(ann);
        methodNode.invisibleAnnotations = annotations;

        Optional<AnnotationNode> opt = getAnnotationNode(methodNode, OnVerification.class);
        assertThat(opt.get(), is(ann));
    }

    @Test
    public void strippinObjectDescriptorShouldReturnCorrectDescriptor() {
        String descriptor = "Lio/neow3j/devpack/neo/Storage;";
        assertThat(stripObjectDescriptor(descriptor), is("io/neow3j/devpack/neo/Storage"));

        descriptor = "io/neow3j/devpack/neo/Storage";
        assertThat(stripObjectDescriptor(descriptor), is("io/neow3j/devpack/neo/Storage"));
    }
}
