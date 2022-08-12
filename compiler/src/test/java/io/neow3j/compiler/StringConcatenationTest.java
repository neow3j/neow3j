package io.neow3j.compiler;

import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.SortedMap;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringConcatenationTest {

//    The StringConcatenationContract contains the following lines of code:
//    (It also contains a method putSomething(), which is not required for this UT)
//
//    """
//    private static final StorageContext ctx = Storage.getStorageContext();
//    public static String getSomething(String key) {
//        return Storage.getString(ctx, key) + "foo" + get();
//    }
//
//    private static String get() {
//        return "hello";
//    }
//    """
//
//    It has been compiled with different Java compiler options. One contains an INVOKESPECIAL opcode to the empty
//    StringBuilder() ctor, whereas the second contains an INVOKESPECIAL opcode to the StringBuilder(String str) ctor.
//
//    The contract "_StringBuilderOneArg_withValueOf" contains a call to String.valueOf in the JVM code which has to
//    be ignored when transpiling.
//
//    The contract "_StringBuilderOneArg" holds a different concatenation that does not end up with a call to
//    String.valueOf in the JVM code, likely because the first argument is not a method return value but a constant:
//
//    """
//    public static String getSomething(String key) {
//        return "foo" + Storage.getString(ctx, key) + get();
//    }
//    """

    @Test
    public void testStringConcatenation_StringBuilderNoArgs() throws IOException {
        InputStream resourceAsStream = StringConcatenationTest.class.getClassLoader()
                .getResourceAsStream("StringConcatenationContract_StringBuilderNoArgs.class");
        CompilationUnit compUnit = new Compiler().compile(resourceAsStream);
        List<NeoMethod> sortedMethods = compUnit.getNeoModule().getSortedMethods();
        SortedMap<Integer, NeoInstruction> instructions = sortedMethods.get(0).getInstructions();

        // Storage.getString()
        // "foo"
        // CAT
        // get()
        // CAT
        // Convert to ByteString
        // return
        assertThat(instructions.get(3).getOpcode(), is(OpCode.LDSFLD0));
        assertThat(instructions.get(4).getOpcode(), is(OpCode.LDARG0));
        assertThat(instructions.get(5).getOpcode(), is(OpCode.SWAP));
        assertThat(instructions.get(6).getOpcode(), is(OpCode.SYSCALL));
        assertThat(instructions.get(6).getOperand(),
                is(hexStringToByteArray(InteropService.SYSTEM_STORAGE_GET.getHash())));
        assertThat(instructions.get(11).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(instructions.get(11).getOperand(), is("foo".getBytes()));
        assertThat(instructions.get(16).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(17).getOpcode(), is(OpCode.CALL_L));
        assertThat(instructions.get(22).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(23).getOpcode(), is(OpCode.CONVERT));
        assertThat(instructions.get(23).getOperand(), is(new byte[]{StackItemType.BYTE_STRING_CODE}));
        assertThat(instructions.get(25).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testStringConcatenation_StringBuilderOneArg() throws IOException {
        InputStream resourceAsStream = StringConcatenationTest.class.getClassLoader()
                .getResourceAsStream("StringConcatenationContract_StringBuilderOneArg_withValueOf.class");
        CompilationUnit compUnit = new Compiler().compile(resourceAsStream);
        SortedMap<Integer, NeoInstruction> instructions = compUnit.getNeoModule().getSortedMethods().get(0)
                .getInstructions();

        // "foo"
        // Storage.getString()
        // CAT
        // get()
        // CAT
        // Convert to ByteString
        // return
        assertThat(instructions.get(3).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(instructions.get(3).getOperand(), is("foo".getBytes()));
        assertThat(instructions.get(8).getOpcode(), is(OpCode.LDSFLD0));
        assertThat(instructions.get(9).getOpcode(), is(OpCode.LDARG0));
        assertThat(instructions.get(10).getOpcode(), is(OpCode.SWAP));
        assertThat(instructions.get(11).getOpcode(), is(OpCode.SYSCALL));
        assertThat(instructions.get(11).getOperand(),
                is(hexStringToByteArray(InteropService.SYSTEM_STORAGE_GET.getHash())));
        assertThat(instructions.get(16).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(17).getOpcode(), is(OpCode.CALL_L));
        assertThat(instructions.get(22).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(23).getOpcode(), is(OpCode.CONVERT));
        assertThat(instructions.get(23).getOperand(), is(new byte[]{StackItemType.BYTE_STRING_CODE}));
        assertThat(instructions.get(25).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testStringConcatenation_StringBuilderOneArg_withValueOf() throws IOException {
        InputStream resourceAsStream = StringConcatenationTest.class.getClassLoader()
                .getResourceAsStream("StringConcatenationContract_StringBuilderOneArg.class");
        CompilationUnit compUnit = new Compiler().compile(resourceAsStream);
        List<NeoMethod> sortedMethods = compUnit.getNeoModule().getSortedMethods();
        SortedMap<Integer, NeoInstruction> instructions = sortedMethods.get(0).getInstructions();

        // Storage.getString()
        // "foo"
        // CAT
        // get()
        // CAT
        // Convert to ByteString
        // return
        assertThat(instructions.get(3).getOpcode(), is(OpCode.LDSFLD0));
        assertThat(instructions.get(4).getOpcode(), is(OpCode.LDARG0));
        assertThat(instructions.get(5).getOpcode(), is(OpCode.SWAP));
        assertThat(instructions.get(6).getOpcode(), is(OpCode.SYSCALL));
        assertThat(instructions.get(6).getOperand(),
                is(hexStringToByteArray(InteropService.SYSTEM_STORAGE_GET.getHash())));
        assertThat(instructions.get(11).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(instructions.get(11).getOperand(), is("foo".getBytes()));
        assertThat(instructions.get(16).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(17).getOpcode(), is(OpCode.CALL_L));
        assertThat(instructions.get(22).getOpcode(), is(OpCode.CAT));
        assertThat(instructions.get(23).getOpcode(), is(OpCode.CONVERT));
        assertThat(instructions.get(23).getOperand(), is(new byte[]{StackItemType.BYTE_STRING_CODE}));
        assertThat(instructions.get(25).getOpcode(), is(OpCode.RET));
    }

}
