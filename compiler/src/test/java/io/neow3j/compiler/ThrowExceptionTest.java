package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.script.OpCode;
import io.neow3j.utils.Numeric;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.Test;

public class ThrowExceptionTest {

    @Test
    public void exceptionWithStringLiteralArgument() throws IOException {
        CompilationUnit res = new Compiler().compile(
                ExceptionWithStringLiteralArgument.class.getName());

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
        CompilationUnit res = new Compiler().compile(
                ExceptionWithStringVariableArgument.class.getName());

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
        CompilationUnit res = new Compiler().compile(
                ExceptionWithStringReturnValueFromMethod.class.getName());

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
}
