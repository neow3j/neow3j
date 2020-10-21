package io.neow3j.compiler;

import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class AsmHelper {

    /**
     * Gets the {@code MethodNode} corresponding to the method called in the given instruction.
     *
     * @param methodInsn The method instruction.
     * @param owner      The class that contains the method to be searched.
     * @return the method.
     */
    public static Optional<MethodNode> getMethodNode(MethodInsnNode methodInsn, ClassNode owner) {
        return owner.methods.stream()
                .filter(m -> m.desc.equals(methodInsn.desc) && m.name.equals(methodInsn.name))
                .findFirst();
    }

    /**
     * Gets the {@code ClassNode} for the given class name using the given classloader.
     *
     * @param internalName The class name in internal name representation as provided by ASM, e.g.,
     *                     in {@link Type#getInternalName()}.
     * @param classLoader  The classloader to use.
     * @return The class node.
     * @throws IOException If an error occurs when reading class files.
     */
    public static ClassNode getAsmClassForInternalName(String internalName,
            ClassLoader classLoader) throws IOException {

        return getAsmClass(Type.getObjectType(internalName).getClassName(), classLoader);
    }

    /**
     * Gets the {@code ClassNode} for the given fully qualified class name.
     *
     * @param fullyQualifiedClassName The name of the class to fetch.
     * @param classLoader             The classloader to use.
     * @return The class node.
     * @throws IOException If an error occurs when reading class files.
     */
    public static ClassNode getAsmClass(String fullyQualifiedClassName, ClassLoader classLoader)
            throws IOException {

        if (classLoader != null) {
            return getAsmClass(classLoader.getResourceAsStream(
                    fullyQualifiedClassName.replace('.', '/') + ".class"));
        }
        return getAsmClass(AsmHelper.class.getClassLoader().getResourceAsStream(
                fullyQualifiedClassName.replace('.', '/') + ".class"));
    }

    /**
     * Gets the {@code ClassNode} from the given input stream.
     *
     * @param classStream The stream containing the byte code of a Java class.
     * @return The class node.
     * @throws IOException If an error occurs when reading the stream.
     */
    public static ClassNode getAsmClass(InputStream classStream) throws IOException {
        ClassReader classReader = new ClassReader(classStream);
        ClassNode asmClass = new ClassNode();
        classReader.accept(asmClass, 0);
        return asmClass;
    }

    /**
     * Checks if the given method has one or more of the given annotations.
     *
     * @param asmMethod The method to check.
     * @param annotations The annotations.
     * @return True if the method has one or more of the given annotations. False, otherwise.
     */
    public static boolean hasAnnotations(MethodNode asmMethod, Class<?>... annotations) {
        return hasAnnotation(asmMethod.invisibleAnnotations, annotations);
    }

    /**
     * Checks if the given {@code ClassNode} has any of the given annotations.
     *
     * @param asmClass The class to check.
     * @param annotations The annotations to check for.
     * @return true if the class has one of the given annotations. False, otherwise.
     */
    public static boolean hasAnnotations(ClassNode asmClass, Class<?>... annotations) {
        return hasAnnotation(asmClass.invisibleAnnotations, annotations);
    }

    private static boolean hasAnnotation(List<AnnotationNode> annotations,
            Class<?>... annotationTypes) {

        if (annotationTypes.length == 0) {
            throw new IllegalArgumentException("Provide at least one annotation class.");
        }
        List<String> descriptors = Stream.of(annotationTypes)
                .map(Type::getDescriptor)
                .collect(Collectors.toList());

        return annotations != null && annotations.stream()
                .anyMatch(annotation -> descriptors.stream()
                        .anyMatch(desc -> annotation.desc.equals(desc)));
    }


    /**
     * Prints the JVM instructions for the given class.
     *
     * @param fullyQualifiedClassName The fully qualified name of the class to print the
     *                                instructions for.
     * @throws IOException If there are problems reading the class file.
     */
    public static void printJVMInstructions(String fullyQualifiedClassName) throws IOException {
        InputStream in = AsmHelper.class.getClassLoader().getResourceAsStream(
                fullyQualifiedClassName.replace('.', '/') + ".class");
        if (in == null) {
            throw new IllegalArgumentException(
                    format("Couldn't find .class file for %s.", fullyQualifiedClassName));
        }
        try (InputStream bufferedInputStream = new BufferedInputStream(in)) {
            bufferedInputStream.mark(0);
            printJVMInstructions(bufferedInputStream);
            bufferedInputStream.reset();
        }
    }

    /**
     * Prints the instructions of the given class input stream.
     *
     * @param in The input stream of the class.
     * @throws IOException If there are problems reading the class file.
     */
    public static void printJVMInstructions(InputStream in) throws IOException {
        ClassReader reader = new ClassReader(in);
        ClassNode classNode = new ClassNode();
        Printer printer = new Textifier();
        TraceMethodVisitor methodVisitor = new TraceMethodVisitor(printer);
        // change the "parsing options" if you would like to
        // see debug symbols from the JVM bytecode
        reader.accept(classNode, 2);
        final List<MethodNode> methods = classNode.methods;
        for (MethodNode m : methods) {
            InsnList inList = m.instructions;
            System.out.println(m.name);
            for (int i = 0; i < inList.size(); i++) {
                System.out.print(insnToString(inList.get(i), methodVisitor, printer));
                printer.getText().clear();
            }
        }
    }

    private static String insnToString(AbstractInsnNode insn, TraceMethodVisitor methodVisitor,
            Printer printer) {

        insn.accept(methodVisitor);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        return sw.toString();
    }

}
