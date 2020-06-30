package io.neow3j.devpack.compiler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.NefFile;
import io.neow3j.contract.NefFile.Version;
import io.neow3j.contract.ScriptReader;
import io.neow3j.devpack.compiler.Compiler.CompilationResult;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.junit.Test;

public class CompilerTest {

    @Test
    public void compileHelloWorldContract() throws IOException {
        CompilationResult res = new Compiler().compileClass(
                "io.neow3j.devpack.template.HelloWorldContract");
        assertThat(Numeric.toHexStringNoPrefix(res.getNef().getScript()),
                is("5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140"));
    }

    @Test
    public void compileExampleContract() throws IOException {
        CompilationResult res = new Compiler().compileClass(
                "io.neow3j.devpack.template.ExampleContract");
        System.out.println(ScriptReader.convertToOpCodeString(res.getNef().getScript()));
        NefFile nef = new NefFile("neow3j", new Version(3, 0, 0, 0), res.getNef().getScript());
        System.out.println(Numeric.toHexStringNoPrefix(nef.toArray()));
        System.out.println("ScriptHash: 0x" + nef.getScriptHash().toString());
    }
}