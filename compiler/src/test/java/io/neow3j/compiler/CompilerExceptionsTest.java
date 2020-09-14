package io.neow3j.compiler;

import io.neow3j.compiler.Compiler.CompilationResult;
import io.neow3j.devpack.framework.Iterator;
import io.neow3j.devpack.framework.Storage;
import io.neow3j.devpack.framework.annotations.EntryPoint;
import java.io.IOException;
import org.hamcrest.core.StringContains;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CompilerExceptionsTest {

    private static final String CONTRACT_NAME = Contract.class.getName();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Ignore("This behavior is not yet implemented.")
    @Test
    public void failOnObjectComparison() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("compare object"));
        CompilationResult res = new Compiler().compileClass(CONTRACT_NAME);
    }

}

class Contract {

    @EntryPoint
    public static boolean objectComparison() {
        Iterator<String, byte[]> it1 = Storage.find("prefix");
        Iterator<String, byte[]> it2 = Storage.find("otherPrefix");
        return it1 == it2;
    }
}
