package io.neow3j.devpack.compiler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.junit.Test;

public class CompilerTest {

    @Test
    public void compileHelloWorldContract() throws IOException {
        byte[] neoCode = Compiler.compileClass("io.neow3j.devpack.template.HelloWorldContract");
        assertThat(Numeric.toHexStringNoPrefix(neoCode),
                is("0c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140"));
    }

    @Test
    public void compileExampleContract() throws IOException {
        byte[] neoCode = Compiler.compileClass("io.neow3j.devpack.template.ExampleContract");
        assertThat(Numeric.toHexStringNoPrefix(neoCode),
                is("0c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140"));
    }
}