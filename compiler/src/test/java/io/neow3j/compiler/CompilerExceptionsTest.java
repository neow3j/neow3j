package io.neow3j.compiler;

import static java.util.Arrays.asList;

import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.events.Event1Arg;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CompilerExceptionsTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void failOnInstantiatingInheritingClass() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "Found call to super constructor of", ArrayList.class.getCanonicalName())));
        new Compiler().compileClass(UnsupportedInheritanceInConstructor.class.getName());
    }

    @Test
    public void throwExceptionIfNonDefaultExceptionInstanceIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                IllegalArgumentException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
        new Compiler().compileClass(UnsupportedException.class.getName());
    }

    @Test
    public void throwExceptionIfNonDefaultExceptionIsUsedInCatchClause() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("catch",
                RuntimeException.class.getCanonicalName(),
                Exception.class.getCanonicalName())));
        new Compiler().compileClass(UnsupportedExceptionInCatchClause.class.getName());
    }

    @Test
    public void throwExceptionIfExceptionWithMoreThanOneArgumentIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("You provided 2 arguments."));
        new Compiler().compileClass(UnsupportedNumberOfExceptionArguments.class.getName());
    }

    @Test
    public void throwExceptionIfExceptionWithANonStringArgumentIsUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContains("You provided a non-string argument."));
        new Compiler().compileClass(UnsupportedExceptionArgument.class.getName());
    }

    @Test
    public void throwExceptionIfTwoEventsAreGivenTheSameName() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList("Two events", "transfer")));
        new Compiler().compileClass(DuplicateUseOfEventDisplayName.class.getName());
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

    static class UnsupportedExceptionInCatchClause {

        public static boolean method(int i) {
            try {
                if (i == 0) {
                    throw new Exception("hello");
                }
            } catch (RuntimeException e) {
                return true;
            } catch (Exception e) {
                return false;
            }
            return true;
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

    static class DuplicateUseOfEventDisplayName {

        @DisplayName("transfer")
        private static Event1Arg<String> event1;

        @DisplayName("transfer")
        private static Event1Arg<String> event2;

        public static boolean method() throws Exception {
            event1.send("notification");
            event2.send("notification");
            return true;
        }
    }
}

