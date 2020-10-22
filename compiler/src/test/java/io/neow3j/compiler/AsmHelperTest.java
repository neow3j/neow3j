package io.neow3j.compiler;

import static io.neow3j.compiler.JVMOpcode.PUTSTATIC;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Syscall;
import io.neow3j.devpack.neo.Storage;
import io.neow3j.devpack.neo.StorageMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class AsmHelperTest {

    @Test
    public void gettingMethodShouldReturnCorrectMethodNode() throws IOException {
        String desc = "(Ljava/lang/String;)[B";
        String name = "get";
        MethodInsnNode insn = new MethodInsnNode(JVMOpcode.INVOKESTATIC.getOpcode(),
                "io/neow3j/devpack/neo/Storage", "get", desc);
        ClassNode owner = new ClassNode();
        ClassReader r = new ClassReader(Storage.class.getCanonicalName());
        r.accept(owner, 0);

        Optional<MethodNode> method = AsmHelper.getMethodNode(insn, owner);
        if (!method.isPresent()) {
            fail();
        }
        assertThat(method.get().name, is(name));
        assertThat(method.get().desc, is(desc));
        assertThat(method.get().invisibleAnnotations, hasSize(1)); // The @Syscall annotation.
    }

    @Test
    public void gettingClassForInternalNameShouldReturnTheCorrectClassNode() throws IOException {
        Type t = Type.getType(Storage.class);
        ClassNode c = AsmHelper.getAsmClassForInternalName(t.getInternalName(),
                this.getClass().getClassLoader());
        assertThat(c.name, is(Storage.class.getCanonicalName().replace(".", "/")));
        assertThat(c.sourceFile, is("Storage.java"));
        assertThat(c.methods, not(hasSize(0)));
    }

    @Test
    public void gettingClassForFqnShouldReturnTheCorrectClassNode() throws IOException {
        ClassNode c = AsmHelper.getAsmClass(Storage.class.getCanonicalName(),
                this.getClass().getClassLoader());
        assertThat(c.name, is(Storage.class.getCanonicalName().replace(".", "/")));
        assertThat(c.sourceFile, is("Storage.java"));
        assertThat(c.methods, not(hasSize(0)));
    }

    @Test
    public void gettingClassFromInputStreamShouldReturnTheCorrectClassNode() throws IOException {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
                Storage.class.getCanonicalName().replace('.', '/') + ".class");
        ClassNode c = AsmHelper.getAsmClass(stream);
        assertThat(c.name, is(Storage.class.getCanonicalName().replace(".", "/")));
        assertThat(c.sourceFile, is("Storage.java"));
        assertThat(c.methods, not(hasSize(0)));
    }

    @Test
    public void hasAnnotationsShouldReturnTrueForAMethodWithGivenAnnotations() throws IOException {
        ClassNode asmClass = AsmHelper.getAsmClass(this.getClass().getCanonicalName(),
                this.getClass().getClassLoader());
        MethodNode method = asmClass.methods.stream()
                .filter(m -> m.name.contains("annotatedMethod"))
                .findFirst().get();
        assertTrue(AsmHelper.hasAnnotations(method, Syscall.class));
        assertTrue(AsmHelper.hasAnnotations(method, Instruction.class));
        assertTrue(AsmHelper.hasAnnotations(method, Syscall.class, Instruction.class));
    }

    // This method is used to test annotations.
    @Syscall(InteropServiceCode.NEO_CRYPTO_SHA256)
    @Instruction
    private void annotatedMethod() {

    }

    @Test
    public void getFieldIndex() throws IOException {
        ClassNode owner = AsmHelper.getAsmClass(StorageMap.class.getCanonicalName(),
                this.getClass().getClassLoader());
        String ownerName = StorageMap.class.getCanonicalName().replace(".", "/");
        FieldInsnNode insn = new FieldInsnNode(PUTSTATIC.getOpcode(), ownerName, "prefix", "[B");
        assertThat(AsmHelper.getFieldIndex(insn, owner), is(1));
    }

}
