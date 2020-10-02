package io.neow3j.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class AsmHelper {

    public static Optional<MethodNode> getMethodNode(MethodInsnNode methodInsn, ClassNode owner) {
        return owner.methods.stream()
                .filter(m -> m.desc.equals(methodInsn.desc) && m.name.equals(methodInsn.name))
                .findFirst();
    }

    public static ClassNode getClassNodeForInternalName(String internalName,
            ClassLoader classLoader) throws IOException {

        return getAsmClass(Type.getObjectType(internalName).getClassName(), classLoader);
    }

    public static ClassNode getAsmClass(String fullyQualifiedClassName, ClassLoader classLoader)
            throws IOException {

        if (classLoader != null) {
            return getAsmClass(classLoader.getResourceAsStream(
                    fullyQualifiedClassName.replace('.', '/') + ".class"));
        }
        return getAsmClass(AsmHelper.class.getClassLoader().getResourceAsStream(
                fullyQualifiedClassName.replace('.', '/') + ".class"));
    }

    public static ClassNode getAsmClass(InputStream classStream) throws IOException {
        ClassReader classReader = new ClassReader(classStream);
        return getAsmClass(classReader);
    }

    public static ClassNode getAsmClass(ClassReader classReader) {
        if (classReader == null) {
            throw new InvalidParameterException("Class reader not found.");
        }
        ClassNode asmClass = new ClassNode();
        classReader.accept(asmClass, 0);
        return asmClass;
    }

    public static boolean hasAnnotations(MethodNode asmMethod, Class<?>... annotations) {
        if (annotations.length == 0) {
            throw new IllegalArgumentException("Provide at least one annotation class.");
        }
        List<String> descriptors = Arrays.stream(annotations)
                .map(Type::getDescriptor)
                .collect(Collectors.toList());

        return asmMethod.invisibleAnnotations != null && asmMethod.invisibleAnnotations.stream()
                .anyMatch(methodAnnotation -> descriptors.stream()
                        .anyMatch(desc -> methodAnnotation.desc.equals(desc)));
    }

}
