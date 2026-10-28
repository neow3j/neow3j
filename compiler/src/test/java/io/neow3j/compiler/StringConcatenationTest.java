package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.SortedMap;

import static io.neow3j.script.InteropService.SYSTEM_STORAGE_LOCAL_GET;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringConcatenationTest {

    // The class files loaded below are produced from the sources in
    // compiler/src/test/resources/string-concatenation/. Each file's header says which class file it produces.

    @Test
    public void testStringConcatenation_StringBuilderNoArgs() throws IOException {
        // resources/string-concatenation/no-args/StringConcatenationContract.java compiled with Java 8 javac

        // JVM bytecode transpiled into the Neo opcodes asserted below.
        // javap -c -p compiler/src/test/resources/StringConcatenationContract_StringBuilderNoArgs.class
        InputStream resourceAsStream = StringConcatenationTest.class.getClassLoader()
                .getResourceAsStream("StringConcatenationContract_StringBuilderNoArgs.class");
        CompilationUnit compUnit = new Compiler().compile(resourceAsStream);
        List<NeoMethod> sortedMethods = compUnit.getNeoModule().getSortedMethods();
        SortedMap<Integer, NeoInstruction> instructions = sortedMethods.get(0).getInstructions();

        // String getSomething(String key) { return Storage.getString(key) + "foo" + get(); };
        // String get() { return "hello" };
        // void putSomething(String key, String value) { Storage.put(key, value) };

        // Storage.getString()
        // "foo"
        // CAT
        // get()
        // CAT
        // Convert to ByteString
        // return
        assertThat(instructions.get(3).getOpcode(), is(OpCode.LDARG0));
        assertThat(instructions.get(4).getOpcode(), is(OpCode.SYSCALL));
        assertThat(instructions.get(4).getOperand(), is(hexStringToByteArray(SYSTEM_STORAGE_LOCAL_GET.getHash())));
        assertThat(instructions.get(9).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(instructions.get(9).getOperand(), is("foo".getBytes()));
        assertThat(instructions.get(14).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(15).getOpcode(), is(OpCode.CALL_L));
        assertThat(instructions.get(20).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(21).getOpcode(), is(OpCode.CONVERT));
        assertThat(instructions.get(21).getOperand(), is(new byte[]{StackItemType.BYTE_STRING.byteValue()}));
        assertThat(instructions.get(23).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testStringConcatenation_StringBuilderOneArg() throws IOException {
        // resources/string-concatenation/foo-first/StringConcatenationContract.java compiled with the Eclipse compiler

        // JVM bytecode transpiled into the Neo opcodes asserted below.
        // javap -c -p compiler/src/test/resources/StringConcatenationContract_StringBuilderOneArg_withValueOf.class
        InputStream resourceAsStream = StringConcatenationTest.class.getClassLoader()
                .getResourceAsStream("StringConcatenationContract_StringBuilderOneArg_withValueOf.class");
        CompilationUnit compUnit = new Compiler().compile(resourceAsStream);
        SortedMap<Integer, NeoInstruction> instructions = compUnit.getNeoModule().getSortedMethods().get(0)
                .getInstructions();

        // String getSomething(String key) { return "foo" + Storage.getString(key) + get(); };
        // String get() { return "hello" };
        // void putSomething(String key, String value) { Storage.put(key, value) };

        // "foo"
        // Storage.getString(arg0)
        // CAT
        // get()
        // CAT
        // Convert to ByteString
        // return
        assertThat(instructions.get(3).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(instructions.get(3).getOperand(), is("foo".getBytes()));
        assertThat(instructions.get(8).getOpcode(), is(OpCode.LDARG0));
        assertThat(instructions.get(9).getOpcode(), is(OpCode.SYSCALL));
        assertThat(instructions.get(9).getOperand(), is(hexStringToByteArray(SYSTEM_STORAGE_LOCAL_GET.getHash())));
        assertThat(instructions.get(14).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(15).getOpcode(), is(OpCode.CALL_L));
        assertThat(instructions.get(20).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(21).getOpcode(), is(OpCode.CONVERT));
        assertThat(instructions.get(21).getOperand(), is(new byte[]{StackItemType.BYTE_STRING.byteValue()}));
        assertThat(instructions.get(23).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testStringConcatenation_StringBuilderOneArg_withValueOf() throws IOException {
        // resources/string-concatenation/no-args/StringConcatenationContract.java compiled with the Eclipse compiler

        // JVM bytecode transpiled into the Neo opcodes asserted below.
        // javap -c -p compiler/src/test/resources/StringConcatenationContract_StringBuilderOneArg.class
        InputStream resourceAsStream = StringConcatenationTest.class.getClassLoader()
                .getResourceAsStream("StringConcatenationContract_StringBuilderOneArg.class");
        CompilationUnit compUnit = new Compiler().compile(resourceAsStream);
        List<NeoMethod> sortedMethods = compUnit.getNeoModule().getSortedMethods();
        SortedMap<Integer, NeoInstruction> instructions = sortedMethods.get(0).getInstructions();

        // String getSomething(String key) { return Storage.getString(key) + "foo" + get(); };
        // String get() { return "hello" };
        // void putSomething(String key, String value) { Storage.put(key, value) };

        // Storage.getString(arg0)
        // "foo"
        // CAT
        // get()
        // CAT
        // Convert to ByteString
        // return
        assertThat(instructions.get(3).getOpcode(), is(OpCode.LDARG0));
        assertThat(instructions.get(4).getOpcode(), is(OpCode.SYSCALL));
        assertThat(instructions.get(4).getOperand(), is(hexStringToByteArray(SYSTEM_STORAGE_LOCAL_GET.getHash())));
        assertThat(instructions.get(9).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(instructions.get(9).getOperand(), is("foo".getBytes()));
        assertThat(instructions.get(14).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(15).getOpcode(), is(OpCode.CALL_L));
        assertThat(instructions.get(20).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(21).getOpcode(), is(OpCode.CONVERT));
        assertThat(instructions.get(21).getOperand(), is(new byte[]{StackItemType.BYTE_STRING.byteValue()}));
        assertThat(instructions.get(23).getOpcode(), is(OpCode.RET));
    }

}
