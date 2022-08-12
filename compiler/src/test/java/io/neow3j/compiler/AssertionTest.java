package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertionTest {

    private static final String NO_INSN_BEFORE_ASSERTION_MSG = " seems to hold a hard coded 'assert false' statement " +
            "or it throws an 'AssertionError'. The compiler does not support that. Use 'Helper.abort()' instead.";
    private static final String UNSUPPORTED_JUMP_CONDITION_CONVERSION_MSG = "Could not handle jump condition. The " +
            "compiler does not support hard coded 'assert false' statements nor throwing an 'AssertionError'. Use " +
            "'Helper.abort()' instead.";

    @Test
    public void testInitsslotOnlyAssertionInstructions() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(InitsslotWithoutStaticVar.class.getName());
        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(1));
        NeoMethod method = methods.get(0);
        assertThat(method.getName(), is("testAssert1"));

        SortedMap<Integer, NeoInstruction> insns = method.getInstructions();
        assertThat(insns.entrySet(), hasSize(6));
        assertThat(insns.get(6).getOpcode(), is(OpCode.EQUAL));
        assertThat(insns.get(7).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(8).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testInitsslotWithAssertion() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(InitsslotWithStaticVar.class.getName());
        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(2));

        NeoMethod method = methods.get(0);
        assertThat(method.getName(), is("testAssert2"));

        SortedMap<Integer, NeoInstruction> insns = method.getInstructions();
        assertThat(insns.entrySet(), hasSize(7));
        assertThat(insns.get(3).getOpcode(), is(OpCode.LDSFLD0));
        assertThat(insns.get(4).getOpcode(), is(OpCode.LDARG0));
        assertThat(insns.get(5).getOpcode(), is(OpCode.NOTEQUAL));
        assertThat(insns.get(5).getOpcode(), is(OpCode.NOTEQUAL));
        assertThat(insns.get(6).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(7).getOpcode(), is(OpCode.PUSH1));
        assertThat(insns.get(8).getOpcode(), is(OpCode.RET));

        NeoMethod initsslotMethod = methods.get(1);
        assertThat(initsslotMethod.getName(), is("_initialize"));

        insns = initsslotMethod.getInstructions();
        assertThat(insns.entrySet(), hasSize(4));
        assertThat(insns.get(0).getOpcode(), is(OpCode.INITSSLOT));
        assertThat(insns.get(2).getOpcode(), is(OpCode.PUSHINT8));
        assertThat(insns.get(2).getOperand(), is(new byte[]{42}));
        assertThat(insns.get(4).getOpcode(), is(OpCode.STSFLD0));
        assertThat(insns.get(5).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testAssertConditions() throws IOException {
        // The JVM assert conditions are jump instructions to jump over the <init> instruction of AssertionError and
        // potential additional instructions (e.g., a message). For the NeoVM ASSERT opcode, these jump instructions
        // should be transpiled into corresponding NeoVM opcodes that just return 0 or 1.
        CompilationUnit compUnit = new Compiler().compile(AssertConditionsContract.class.getName());
        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(8));

        NeoMethod method_eq = methods.get(0);
        SortedMap<Integer, NeoInstruction> insns = method_eq.getInstructions();
        assertThat(insns.get(5).getOpcode(), is(OpCode.EQUAL));
        assertThat(insns.get(6).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(7).getOpcode(), is(OpCode.RET));

        NeoMethod method_ne = methods.get(1);
        insns = method_ne.getInstructions();
        assertThat(insns.entrySet(), hasSize(6));
        assertThat(insns.get(5).getOpcode(), is(OpCode.NOTEQUAL));
        assertThat(insns.get(6).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(7).getOpcode(), is(OpCode.RET));

        NeoMethod method_lt = methods.get(2);
        insns = method_lt.getInstructions();
        assertThat(insns.entrySet(), hasSize(6));
        assertThat(insns.get(5).getOpcode(), is(OpCode.LT));
        assertThat(insns.get(6).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(7).getOpcode(), is(OpCode.RET));

        NeoMethod method_gt = methods.get(3);
        insns = method_gt.getInstructions();
        assertThat(insns.entrySet(), hasSize(6));
        assertThat(insns.get(5).getOpcode(), is(OpCode.GT));
        assertThat(insns.get(6).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(7).getOpcode(), is(OpCode.RET));

        NeoMethod method_le = methods.get(4);
        insns = method_le.getInstructions();
        assertThat(insns.entrySet(), hasSize(6));
        assertThat(insns.get(5).getOpcode(), is(OpCode.LE));
        assertThat(insns.get(6).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(7).getOpcode(), is(OpCode.RET));

        NeoMethod method_ge = methods.get(5);
        insns = method_ge.getInstructions();
        assertThat(insns.entrySet(), hasSize(6));
        assertThat(insns.get(5).getOpcode(), is(OpCode.GE));
        assertThat(insns.get(6).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(7).getOpcode(), is(OpCode.RET));

        NeoMethod method_ifnot = methods.get(6);
        insns = method_ifnot.getInstructions();
        assertThat(insns.entrySet(), hasSize(5));
        assertThat(insns.get(4).getOpcode(), is(OpCode.NOT));
        assertThat(insns.get(5).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(6).getOpcode(), is(OpCode.RET));

        NeoMethod method_if = methods.get(7);
        insns = method_if.getInstructions();
        assertThat(insns.entrySet(), hasSize(4));
        assertThat(insns.get(4).getOpcode(), is(OpCode.ASSERT));
        assertThat(insns.get(5).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testNoMessageSupportInAssertion() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(AssertionWithMessage.class.getName()));
        assertThat(thrown.getMessage(), is("Passing a message with the 'assert' statement is not supported."));
    }

    static class InitsslotWithoutStaticVar {
        public static void testAssert1(int i) {
            assert i == 17;
        }
    }

    static class InitsslotWithStaticVar {
        public static int VAR = 42;

        public static boolean testAssert2(int i) {
            assert VAR != i;
            return true;
        }
    }

    @Test
    public void testTryCatchAssertionError() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(TryCatchAssertionErrorContract.class.getName()));
        assertThat(thrown.getMessage(), is(UNSUPPORTED_JUMP_CONDITION_CONVERSION_MSG));
    }

    @Test
    public void testUseOfAssertFalse() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(UseOfAssertFalseContract.class.getName()));
        assertThat(thrown.getMessage(), is(UNSUPPORTED_JUMP_CONDITION_CONVERSION_MSG));
    }

    @Test
    public void testThrowingAssertionError() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(ThrowingAssertionErrorContract.class.getName()));
        assertThat(thrown.getMessage(), containsString(NO_INSN_BEFORE_ASSERTION_MSG));
    }

    @Test
    public void testCallingMethodWithOnlyAssertFalse() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(CallingMethodWithOnlyAssertFalseContract.class.getName()));
        assertThat(thrown.getMessage(), containsString(NO_INSN_BEFORE_ASSERTION_MSG));
    }

    @Test
    public void testMethodWithOnlyAssertFalse() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(MethodWithOnlyAssertFalseContract.class.getName()));
        assertThat(thrown.getMessage(), containsString(NO_INSN_BEFORE_ASSERTION_MSG));
    }

    static class AssertConditionsContract {
        public static void testEQ(int i) {
            assert 11 == i;
        }

        public static void testNE(int i) {
            assert 11 != i;
        }

        public static void testLT(int i) {
            assert 11 < i;
        }

        public static void testGT(int i) {
            assert 12 > i;
        }

        public static void testLE(int i) {
            assert 1 <= i;
        }

        public static void testGE(int i) {
            assert 1 >= i;
        }

        public static void testIFNOT(boolean b) {
            assert !b;
        }

        public static void testIF(boolean b) {
            assert b;
        }
    }

    static class AssertionWithMessage {
        public static void assertWithMessage(int i) {
            assert i != 1 : "Value must be 1.";
        }
    }

    static class TryCatchAssertionErrorContract {
        public static String tryCatchingAssertionError() {
            try {
                throw new AssertionError();
            } catch (Exception e) {
                return e.getMessage();
            }
        }
    }

    static class UseOfAssertFalseContract {
        public static String usingAssertFalse() {
            String s = "hello";
            assert false;
            return s;
        }
    }

    static class ThrowingAssertionErrorContract {
        public static String throwingAssertionError() {
            throw new AssertionError();
        }
    }

    static class MethodWithOnlyAssertFalseContract {
        public static void assertFalse() {
            assert false;
        }
    }

    static class CallingMethodWithOnlyAssertFalseContract {
        public static void callingMethodWithOnlyAssertFalse() {
            assertFalse();
        }

        private static void assertFalse() {
            assert false;
        }
    }

}
