package io.neow3j.compiler;

import static java.util.Arrays.asList;

import io.neow3j.devpack.neo.Iterator;
import io.neow3j.devpack.neo.Storage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CompilerExceptionsTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Ignore("This behavior is not yet implemented.")
    @Test
    public void failOnObjectComparison() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("compare object"));
        CompilationUnit res = new Compiler().compileClass(ObjectComparison.class.getName());
    }

    @Test
    public void failOnInstantiatingInheritingClass() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "Found call to super constructor of", ArrayList.class.getCanonicalName())));
        CompilationUnit res = new Compiler().compileClass(
                        UnsupportedInheritanceInConstructor.class.getName());
    }

    static class ObjectComparison {

        public static boolean method() {
            Iterator<String, byte[]> it1 = Storage.find("prefix");
            Iterator<String, byte[]> it2 = Storage.find("otherPrefix");
            return it1 == it2;
        }
    }

    static class UnsupportedInheritanceInConstructor {

        public static void method() {
            List<String> l = new ArrayList<>();
        }
    }

}

