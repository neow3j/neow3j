package io.neow3j.compiler;

import io.neow3j.script.OpCode;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ThrowExceptionTest {

    @Test
    public void exceptionWithStringLiteralArgument() throws IOException {
        CompilationUnit res = new Compiler().compile(ExceptionWithStringLiteralArgument.class.getName());

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        stream.write(OpCode.PUSHDATA1.getCode());
        stream.write(21);
        stream.write(Numeric.hexStringToByteArray("5468697320697320616e20657863657074696f6e2e"));
        stream.write(OpCode.THROW.getCode());
        byte[] expectedScript = byteStream.toByteArray();

        assertThat(res.getNefFile().getScript(), is(expectedScript));
    }

    @Test
    public void exceptionWithoutArgument() throws IOException {
        CompilationUnit res = new Compiler().compile(ExceptionWithoutArgument.class.getName());

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        stream.write(OpCode.PUSHDATA1.getCode());
        stream.write(5);
        stream.write(Numeric.hexStringToByteArray("6572726f72"));
        stream.write(OpCode.THROW.getCode());
        byte[] expectedScript = byteStream.toByteArray();

        assertThat(res.getNefFile().getScript(), is(expectedScript));
    }

    @Test
    public void exceptionWithStringVariableArgument() throws IOException {
        CompilationUnit res = new Compiler().compile(ExceptionWithStringVariableArgument.class.getName());

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        stream.write(OpCode.INITSLOT.getCode());
        stream.write(new byte[]{0x00, 0x01});
        stream.write(OpCode.LDARG0.getCode());
        stream.write(OpCode.THROW.getCode());
        byte[] expectedScript = byteStream.toByteArray();

        assertThat(res.getNefFile().getScript(), is(expectedScript));
    }

    @Test
    public void exceptionWithStringReturnValueFromMethodCall() throws IOException {
        CompilationUnit res = new Compiler().compile(ExceptionWithStringReturnValueFromMethod.class.getName());

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        stream.write(OpCode.CALL_L.getCode());
        stream.write(new byte[]{0x06, 0x00, 0x00, 0x00});
        stream.write(OpCode.THROW.getCode());
        stream.write(OpCode.PUSHDATA1.getCode());
        stream.write(13);
        stream.write(Numeric.hexStringToByteArray("6572726f72206d657373616765"));
        stream.write(OpCode.RET.getCode());
        byte[] expectedScript = byteStream.toByteArray();

        assertThat(res.getNefFile().getScript(), is(expectedScript));
    }

    @Test
    public void testIsThrowableGetMessage() throws IOException {
        // Tests the method MethodsConverter.isThrowableGetMessage()
        // The method 'Throwable.getMessage()' should be ignored by the compiler and the message on the stack should
        // be returned.
        CompilationUnit compUnit = new Compiler().compile(ExceptionGetMessageInCatch.class.getName());

        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(1));

        SortedMap<Integer, NeoInstruction> insns = methods.get(0).getInstructions();
        assertThat(insns.entrySet(), hasSize(7));
        assertThat(insns.get(3).getOpcode(), is(OpCode.TRY_L));
        assertThat(insns.get(12).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(insns.get(12).getOperand(), is("Neowww!".getBytes(UTF_8)));
        assertThat(insns.get(21).getOpcode(), is(OpCode.THROW));
        assertThat(insns.get(22).getOpcode(), is(OpCode.STLOC0));
        assertThat(insns.get(23).getOpcode(), is(OpCode.LDLOC0));
        assertThat(insns.get(24).getOpcode(), is(OpCode.RET));
    }

    @Test
    public void testExceptionGetMessageEmpty() throws IOException {
        // Tests the method MethodsConverter.isThrowableGetMessage()
        // The method 'Exception.getMessage()' should be ignored by the compiler and the message on the stack should
        // be returned.
        CompilationUnit compUnit = new Compiler().compile(ExceptionGetMessageInCatchEmpty.class.getName());

        List<NeoMethod> methods = compUnit.getNeoModule().getSortedMethods();
        assertThat(methods, hasSize(1));

        SortedMap<Integer, NeoInstruction> insns = methods.get(0).getInstructions();
        assertThat(insns.entrySet(), hasSize(7));
        assertThat(insns.get(3).getOpcode(), is(OpCode.TRY_L));
        assertThat(insns.get(12).getOpcode(), is(OpCode.PUSHDATA1));
        assertThat(insns.get(12).getOperand(), is("error".getBytes(UTF_8)));
        assertThat(insns.get(19).getOpcode(), is(OpCode.THROW));
        assertThat(insns.get(20).getOpcode(), is(OpCode.STLOC0));
        assertThat(insns.get(21).getOpcode(), is(OpCode.LDLOC0));
        assertThat(insns.get(22).getOpcode(), is(OpCode.RET));
    }

    static class ExceptionWithStringLiteralArgument {

        public static boolean exceptionWithStringLiteralArgument() throws Exception {
            throw new Exception("This is an exception.");
        }
    }

    static class ExceptionWithoutArgument {

        public static boolean exceptionWithoutArgument() throws Exception {
            throw new Exception();
        }
    }

    static class ExceptionWithStringVariableArgument {

        public static boolean exceptionWithStringVariableArgument(String s) throws Exception {
            throw new Exception(s);
        }
    }
    static class ExceptionWithStringReturnValueFromMethod {

        public static boolean exceptionWithStringReturnValueFromMethod() throws Exception {
            throw new Exception(getErrorMessage());
        }

        static String getErrorMessage() {
            return "error message";
        }
    }

    static class ExceptionGetMessageInCatch {
        public static String exception() {
            try {
                throw new Exception("Neowww!");
            } catch (Exception e) {
                return e.getMessage();
            }
        }
    }

    static class ExceptionGetMessageInCatchEmpty {
        public static String exception() {
            try {
                throw new Exception();
            } catch (Exception e) {
                return e.getMessage();
            }
        }
    }

}
