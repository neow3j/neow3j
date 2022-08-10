package io.neow3j.compiler;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;

public class ObjectInheritanceTest {

    @Test
    public void testHashCode() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectHashCode.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'hashCode'"));
    }

    @Test
    public void testEquals() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectEquals.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'equals'"));
    }

    @Test
    public void testToString() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectToString.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'toString'"));
    }

    @Test
    public void testNotify() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectNotify.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'notify'"));
    }

    @Test
    public void testNotifyAll() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectNotifyAll.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'notifyAll'"));
    }

    @Test
    public void testWaitLong() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectWaitLong.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'wait'"));
    }

    @Test
    public void testWaitLongInt() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectWaitLongInt.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'wait'"));
    }

    @Test
    public void testWaitNoParams() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectWaitNoParams.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'wait'"));
    }

    @Test
    public void testGetClass() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectGetClass.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'getClass'"));
    }

    @Test
    public void testClone() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectClone.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'clone'"));
    }

    @Test
    public void testFinalize() {
        CompilerException thrown =
                assertThrows(CompilerException.class, () -> new Compiler().compile(ObjectFinalize.class.getName()));
        assertThat(thrown.getMessage(), containsString("are not supported. Implement the method 'finalize'"));
    }

    public static class TestClass {
    }

    public static class TestClassWithSuper {
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }

    static class ObjectHashCode {
        public static int test() {
            return new TestClass().hashCode();
        }
    }

    static class ObjectEquals {
        public static boolean test() {
            return new TestClass().equals(new TestClass());
        }
    }

    static class ObjectToString {
        public static String test() {
            return new TestClass().toString();
        }
    }

    static class ObjectNotify {
        public static void test() {
            new TestClass().notify();
        }
    }

    static class ObjectNotifyAll {
        public static void test() {
            new TestClass().notifyAll();
        }
    }

    static class ObjectWaitLong {
        public static void test() throws InterruptedException {
            new TestClass().wait(1L);
        }
    }

    static class ObjectWaitLongInt {
        public static void test() throws InterruptedException {
            new TestClass().wait(1L, 1);
        }
    }

    static class ObjectWaitNoParams {
        public static void test() throws InterruptedException {
            new TestClass().wait();
        }
    }

    static class ObjectGetClass {
        public static Class<? extends TestClass> test() throws InterruptedException {
            return new TestClass().getClass();
        }
    }

    static class ObjectClone {
        public static Object test() throws CloneNotSupportedException {
            return new TestClassWithSuper().clone();
        }
    }

    static class ObjectFinalize {
        public static void test() throws Throwable {
            new TestClassWithSuper().finalize();
        }
    }

}
