package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.SortedMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AssertionTest {

    @Test
    public void testInitsslotOnlyAssertionInstructions() throws IOException {
        CompilationUnit compUnit = new Compiler()
                .compile(InitsslotWithoutOtherStaticVar.class.getName());
        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(1));
        NeoMethod method = methods.get(0);
        assertThat(method.getName(), is("testAssert1"));

        SortedMap<Integer, NeoInstruction> insns = method.getInstructions();
        assertThat(insns.entrySet(), hasSize(7));
        assertThat(insns.get(11).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(insns.get(11).getOperand(),
                is("assertion failed".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testInitsslotWithAssertion() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(InitsslotWithStaticVar.class.getName());
        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(2));
        NeoMethod initsslotMethod = methods.get(1);
        assertThat(initsslotMethod.getName(), is("_initialize"));

        SortedMap<Integer, NeoInstruction> insns = initsslotMethod.getInstructions();
        assertThat(insns.entrySet(), hasSize(4));
        assertThat(insns.get(0).getOpcode(), is(OpCode.INITSSLOT));
        assertThat(insns.get(2).getOpcode(), is(OpCode.PUSHINT8));
        assertThat(insns.get(2).getOperand(), is(new byte[]{42}));
        assertThat(insns.get(4).getOpcode(), is(OpCode.STSFLD0));
        assertThat(insns.get(5).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testIsThrowableGetMessage() throws IOException {
        // Tests the method MethodsConverter.isThrowableGetMessage()
        // The method 'Throwable.getMessage()' should be ignored by the compiler and the message
        // on the stack should be returned.
        CompilationUnit compUnit = new Compiler().compile(GetMessageInCatch.class.getName());
        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(3));
        SortedMap<Integer, NeoInstruction> insns = methods.get(0).getInstructions();
        assertThat(insns.entrySet(), hasSize(7));
        assertThat(insns.get(19).getOpcode(), is(OpCode.THROW));
        assertThat(insns.get(20).getOpcode(), is(OpCode.STLOC0));
        assertThat(insns.get(21).getOpcode(), is(OpCode.LDLOC0));
        assertThat(insns.get(22).getOpcode(), is(OpCode.RET));

        insns = methods.get(1).getInstructions();
        assertThat(insns.entrySet(), hasSize(10));
        assertThat(insns.get(28).getOpcode(), is(OpCode.THROW));
        assertThat(insns.get(29).getOpcode(), is(OpCode.JMP_L));
        assertThat(insns.get(29).getOperand().length, is(4));
        assertThat(insns.get(34).getOpcode(), is(OpCode.STLOC0));
        assertThat(insns.get(35).getOpcode(), is(OpCode.LDLOC0));
        assertThat(insns.get(36).getOpcode(), is(OpCode.RET));

        insns = methods.get(2).getInstructions();
        assertThat(insns.entrySet(), hasSize(7));
        assertThat(insns.get(31).getOpcode(), is(OpCode.THROW));
        assertThat(insns.get(32).getOpcode(), is(OpCode.STLOC0));
        assertThat(insns.get(33).getOpcode(), is(OpCode.LDLOC0));
        assertThat(insns.get(34).getOpcode(), is(OpCode.RET));
    }

    static class InitsslotWithoutOtherStaticVar {
        public static void testAssert1(int i) {
            assert i == 17;
        }
    }

    static class InitsslotWithStaticVar {
        public static int VAR = 42;

        public static boolean testAssert2(int i) {
            assert VAR == i : "neoowww";
            return true;
        }
    }

    static class GetMessageInCatch {

        public static String exception() {
            try {
                throw new Exception();
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        public static String assertion() {
            try {
                assert false : "Assert failed.";
            } catch (Exception e) {
                return e.getMessage();
            }
            return "";
        }

        public static String newAssertion() {
            try {
                throw new AssertionError("Assertion failed.");
            } catch (Exception e) {
                return e.getMessage();
            }
        }
    }

}
