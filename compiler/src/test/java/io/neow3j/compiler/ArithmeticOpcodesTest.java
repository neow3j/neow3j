package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ArithmeticOpcodesTest {

    @Test
    public void integerMethodsUseArithmeticOpcodes() throws IOException {
        CompilationUnit compUnit = new Compiler().compile(IntegerArithmeticContract.class.getName());
        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(7));

        assertThat(opcodes(method(methods, "absInt")),
                is(asList(OpCode.INITSLOT, OpCode.LDARG0, OpCode.ABS, OpCode.RET)));
        assertThat(opcodes(method(methods, "absLong")),
                is(asList(OpCode.INITSLOT, OpCode.LDARG0, OpCode.ABS, OpCode.RET)));
        assertThat(opcodes(method(methods, "minInt")),
                is(asList(OpCode.INITSLOT, OpCode.LDARG0, OpCode.LDARG1, OpCode.MIN, OpCode.RET)));
        assertThat(opcodes(method(methods, "maxLong")),
                is(asList(OpCode.INITSLOT, OpCode.LDARG0, OpCode.LDARG1, OpCode.MAX, OpCode.RET)));
        assertThat(opcodes(method(methods, "signInt")),
                is(asList(OpCode.INITSLOT, OpCode.LDARG0, OpCode.SIGN, OpCode.RET)));
        assertThat(opcodes(method(methods, "signLong")),
                is(asList(OpCode.INITSLOT, OpCode.LDARG0, OpCode.SIGN, OpCode.RET)));
        assertThat(opcodes(method(methods, "combined")),
                is(asList(OpCode.INITSLOT, OpCode.LDARG0, OpCode.ABS, OpCode.LDARG1, OpCode.LDARG2, OpCode.MIN,
                        OpCode.ADD, OpCode.RET)));
    }

    private static NeoMethod method(List<NeoMethod> methods, String name) {
        return methods.stream()
                .filter(method -> method.getName().equals(name))
                .findFirst()
                .get();
    }

    private static List<OpCode> opcodes(NeoMethod method) {
        return method.getInstructions().values().stream()
                .map(NeoInstruction::getOpcode)
                .collect(Collectors.toList());
    }

    static class IntegerArithmeticContract {

        public static int absInt(int value) {
            return Math.abs(value);
        }

        public static long absLong(long value) {
            return Math.abs(value);
        }

        public static int minInt(int left, int right) {
            return Math.min(left, right);
        }

        public static long maxLong(long left, long right) {
            return Math.max(left, right);
        }

        public static int signInt(int value) {
            return Integer.signum(value);
        }

        public static int signLong(long value) {
            return Long.signum(value);
        }

        public static int combined(int value, int left, int right) {
            return Math.abs(value) + Math.min(left, right);
        }

    }

}
