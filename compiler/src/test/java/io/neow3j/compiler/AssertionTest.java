package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.SortedMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AssertionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

}
