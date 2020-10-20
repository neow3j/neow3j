package io.neow3j.compiler;

import io.neow3j.devpack.neo.Iterator;
import io.neow3j.devpack.neo.Storage;
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
        CompilationUnit res = new Compiler().compileClass(CONTRACT_NAME);
    }

    static class Contract {

        public static boolean objectComparison() {
            Iterator<String, byte[]> it1 = Storage.find("prefix");
            Iterator<String, byte[]> it2 = Storage.find("otherPrefix");
            return it1 == it2;
        }
    }

}

