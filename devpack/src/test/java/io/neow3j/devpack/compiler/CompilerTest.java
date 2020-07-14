package io.neow3j.devpack.compiler;

import io.neow3j.contract.ScriptReader;
import io.neow3j.devpack.compiler.Compiler.CompilationResult;
import io.neow3j.protocol.ObjectMapperFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Test;

public class CompilerTest {

    @Test
    public void compileExampleContract() throws IOException {
        CompilationResult res = new Compiler().compileClass(
                "io.neow3j.devpack.template.ExampleContract");
        System.out.println(ScriptReader.convertToOpCodeString(res.getNef().getScript()));
        String tmpDir = System.getProperty("user.home") + "/tmp/";
        File tmpDirFile = new File(tmpDir);
        if (!tmpDirFile.exists() && !new File(tmpDir).mkdir()) {
            return;
        }
        FileOutputStream s = new FileOutputStream(tmpDir + "Neow3jContract.nef");
        s.write(res.getNef().toArray());
        s.close();
        s = new FileOutputStream(tmpDir + "Neow3jContract.manifest.json");
        ObjectMapperFactory.getObjectMapper().writeValue(s, res.getManifest());
        s.close();
        s = new FileOutputStream(tmpDir + "neow3j-script.txt");
        String script = ScriptReader.convertToOpCodeString(res.getNef().getScript());
        s.write(script.getBytes());
        s.close();
    }
}