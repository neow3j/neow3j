package io.neow3j.compiler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.neow3j.utils.ClassUtils.getClassInputStreamForClassName;
import static java.lang.String.format;

public class AsmHelper {

    protected static final List<Character> PRIMITIVE_TYPE_NAMES = new ArrayList<>(
            Arrays.asList('V', 'Z', 'C', 'B', 'S', 'I', 'F', 'J', 'D'));

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
     * Gets the {@code ClassNode} for the given class descriptor using the given classloader.
     *
     * @param descriptor  The class' descriptor as provided by ASM, e.g., in {@link
     *                    Type#getDescriptor()}.
     * @param classLoader The classloader to use.
     * @return The class node.
     * @throws IOException If an error occurs when reading class files.
     */
    public static ClassNode getAsmClassForDescriptor(String descriptor,
            ClassLoader classLoader) throws IOException {

        return getAsmClass(Type.getType(descriptor).getClassName(), classLoader);
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
            return getAsmClass(getClassInputStreamForClassName(
                    fullyQualifiedClassName, classLoader));
        }
        return getAsmClass(getClassInputStreamForClassName(
                fullyQualifiedClassName, AsmHelper.class.getClassLoader()));
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
     * @param asmMethod   The method to check.
     * @param annotations The annotations.
     * @return True if the method has one or more of the given annotations. False, otherwise.
     */
    public static boolean hasAnnotations(MethodNode asmMethod, Class<?>... annotations) {
        return hasAnnotation(asmMethod.invisibleAnnotations, annotations);
    }

    /**
     * Checks if the given {@code ClassNode} has any of the given annotations.
     *
     * @param asmClass    The class to check.
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

    public static Optional<AnnotationNode> getAnnotationNode(FieldNode fieldNode,
            Class<?> annotation) {
        return getAnnotationNode(fieldNode.invisibleAnnotations, annotation);
    }

    public static Optional<AnnotationNode> getAnnotationNode(MethodNode methodNode,
            Class<?> annotation) {
        return getAnnotationNode(methodNode.invisibleAnnotations, annotation);
    }

    public static Optional<AnnotationNode> getAnnotationNode(ClassNode fieldNode,
            Class<?> annotation) {
        return getAnnotationNode(fieldNode.invisibleAnnotations, annotation);
    }

    private static Optional<AnnotationNode> getAnnotationNode(List<AnnotationNode> annotationNodes,
            Class<?> annotation) {
        if (annotationNodes == null) {
            return Optional.empty();
        }
        return annotationNodes.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(annotation)))
                .findFirst();
    }

    /**
     * Gets the annotations specified by {@code singleClass} and {@code multiClass} from the
     * given method. This only works for annotations that have a single instance and
     * multi-instance version, e.g., {@link io.neow3j.devpack.annotations.Instruction} and
     * {@link io.neow3j.devpack.annotations.Instruction.Instructions}.
     *
     * @param method      The method to get the annotations from.
     * @param singleClass The single instance annotation class.
     * @param multiClass  The multi instance annotation class.
     * @return The matching annotations found on the method.
     */
    @SuppressWarnings("unchecked")
    public static List<AnnotationNode> getAnnotations(MethodNode method, Class<?> singleClass,
            Class<?> multiClass) {

        AnnotationNode node = method.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(multiClass))
                        || a.desc.equals(Type.getDescriptor(singleClass)))
                .findFirst().get();

        if (node.desc.equals(Type.getDescriptor(singleClass))) {
            return Arrays.asList(node);
        }
        return (List<AnnotationNode>) node.values.get(1);
    }

    /**
     * Gets the property's value on the given annotation.
     *
     * @param annotation   The annotation to look for the property on.
     * @param propertyName The property's name.
     * @return the property value or null if the property was not found on the annotation.
     */
    public static Object getAnnotationProperty(AnnotationNode annotation, String propertyName) {
        if (annotation.values == null) {
            return null;
        }
        int idx = annotation.values.indexOf(propertyName);
        if (idx == -1) {
            return null;
        }
        return annotation.values.get(idx + 1);
    }

    /**
     * Gets the byte array property with {@code propertyName} from the given annotation.
     * Expects the property to be a byte array or a list of bytes.
     *
     * @param annotation   The annotation to get the property from.
     * @param propertyName The property's name.
     * @return the property's value.
     */
    public static byte[] getByteArrayAnnotationProperty(AnnotationNode annotation,
            String propertyName) {
        Object property = getAnnotationProperty(annotation, propertyName);
        byte[] bytes = new byte[]{};
        if (property instanceof byte[]) {
            bytes = (byte[]) property;
        } else if (property instanceof List) {
            List<?> prefixObjAsList = (List<?>) property;
            bytes = new byte[prefixObjAsList.size()];
            int i = 0;
            for (Object element : prefixObjAsList) {
                bytes[i++] = (byte) element;
            }
        }
        return bytes;
    }

    /**
     * Gets the value of a string property with {@code propertyName} on the given annotation.
     *
     * @param annotation   The annotation to get the property from.
     * @param propertyName The name of the string property.
     * @return The property value;
     */
    public static String getStringAnnotationProperty(AnnotationNode annotation,
            String propertyName) {

        Object property = getAnnotationProperty(annotation, propertyName);
        if (property == null) {
            return null;
        }
        return ((String[])property)[1];
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

    public static int getFieldIndex(FieldInsnNode fieldInsn, CompilationUnit compUnit)
            throws IOException {

        ClassNode owner = getAsmClassForInternalName(fieldInsn.owner, compUnit.getClassLoader());
        int idx = 0;
        boolean fieldFound = false;
        for (FieldNode field : owner.fields) {
            if (field.name.equals(fieldInsn.name)) {
                fieldFound = true;
                break;
            }
            idx++;
        }
        if (!fieldFound) {
            throw new CompilerException(owner, format("Tried to access a field variable with " +
                    "name '%s', but such a field does not exist on this class.", fieldInsn.name));
        }
        return idx;
    }

    /**
     * Extracts generic type parameters from the given field, if it has any.
     * <p>
     * E.g., {@code Event<Integer, String>} will return the list containing {@code
     * Ljava/lang/Integer;} and {@code Ljava/lang/String;}. The returned strings are ASM internal
     * names.
     *
     * @param fieldNode The field.
     * @return the list of type paramters.
     */
    public static List<String> extractTypeParametersFromSignature(FieldNode fieldNode) {
        String sig = fieldNode.signature;
        if (!sig.contains("<")) {
            return new ArrayList<>();
        }
        int startIdx = sig.indexOf("<") + 1;
        int endIdx = sig.lastIndexOf(">");
        String typesString = sig.substring(startIdx, endIdx);
        return extractTypeParamsFromString(typesString);
    }

    private static List<String> extractTypeParamsFromString(String types) {
        // Remove any generic type parameters.
        types = types.replaceAll("<[^<>]*>", "");
        List<String> separatedTypes = new ArrayList<>();
        int i = 0;
        while (i < types.length()) {
            if (types.charAt(i) == '[' && PRIMITIVE_TYPE_NAMES.contains(types.charAt(i + 1))) {
                separatedTypes.add(Character.toString(types.charAt(i++)) + types.charAt(i++));
            } else {
                String t = types.substring(i, types.indexOf(";", i) + 1);
                separatedTypes.add(t);
                i += t.length();
            }
        }
        return separatedTypes;
    }

    /**
     * Strips the given object descriptor of the starting 'L' and ending ';' characters if they are
     * present. Sometimes object descriptors come with those characters but not always. If those
     * characters are not present, the unchanged string is returned.
     *
     * @param desc The object descriptor to strip.
     * @return the stripped object descriptor.
     */
    public static String stripObjectDescriptor(String desc) {
        if (desc.charAt(0) == 'L') {
            return desc.substring(1, desc.length() - 1);
        }
        return desc;
    }
}
