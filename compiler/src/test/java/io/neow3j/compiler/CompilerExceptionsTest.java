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

    @Test
    public void throwExceptionIfNonDefaultExceptionInstanceIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                IllegalArgumentException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
        CompilationUnit res = new Compiler().compileClass(UnsupportedException.class.getName());
    }

    @Test
    public void throwExceptionIfExceptionWithMoreThanOneArgumentIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("You provided 2 arguments."));
        CompilationUnit res = new Compiler().compileClass(
                UnsupportedNumberOfExceptionArguments.class.getName());
    }

    @Test
    public void throwExceptionIfExceptionWithANonStringArgumentIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("You provided a non-string argument."));
        CompilationUnit res = new Compiler().compileClass(
                UnsupportedExceptionArgument.class.getName());
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

    static class UnsupportedException {

        public static boolean method() {
            throw new IllegalArgumentException("Not allowed.");
        }
    }

    static class UnsupportedNumberOfExceptionArguments {

        public static boolean method() throws Exception {
            throw new Exception("Not allowed.", new Exception());
        }
    }

    static class UnsupportedExceptionArgument {

        public static boolean method() throws Exception {
            throw new Exception(new Exception());
        }
    }

}

