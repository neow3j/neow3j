package io.neow3j.compiler;

import io.neow3j.contract.NefFile;
import io.neow3j.script.ScriptReader;
import io.neow3j.serialization.exceptions.DeserializationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainPrintOpcodes {

    public static void main(String[] args) throws Throwable {

        printOpcodes(args[0], args[1]);
    }

    private static void printOpcodes(String filePath, String outPath) {
        try {
            NefFile nef = NefFile.readFromFile(new File(filePath));
            FileWriter w = new FileWriter(outPath);
            w.write(ScriptReader.convertToOpCodeString(nef.getScript()));
            w.close();
            System.out.println(ScriptReader.convertToOpCodeString(nef.getScript()));
        } catch (DeserializationException | IOException e) {
            e.printStackTrace();
        }
    }


//    private static Printer printer = new Textifier();
//    private static TraceMethodVisitor mp = new TraceMethodVisitor(printer);
//
//    private static void printJVMInstructions(String fullyQualifiedName) throws IOException {
//        InputStream in = MainCompileAndDeploy.class.getClassLoader().getResourceAsStream
//        (fullyQualifiedName);
//        InputStream bufferedInputStream = new BufferedInputStream(in);
//        bufferedInputStream.mark(0);
//        printJVMOpCode(bufferedInputStream);
//    }
//
//    private static void printJVMOpCode(InputStream in) throws IOException {
//        ClassReader reader = new ClassReader(in);
//        ClassNode classNode = new ClassNode();
//        reader.accept(classNode, 2);
//        @SuppressWarnings("unchecked") final List<MethodNode> methods = classNode.methods;
//        for (MethodNode m : methods) {
//            InsnList inList = m.instructions;
//            System.out.println(m.name);
//            for (int i = 0; i < inList.size(); i++) {
//                System.out.print(insnToString(inList.get(i)));
//            }
//        }
//    }
//
//    private static String insnToString(AbstractInsnNode insn) {
//        insn.accept(mp);
//        StringWriter sw = new StringWriter();
//        printer.print(new PrintWriter(sw));
//        printer.getText().clear();
//        return sw.toString();
//    }

}
