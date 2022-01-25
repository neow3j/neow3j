package io.neow3j.compiler;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class ObjectInheritanceTest {

    @Test
    public void testHashCode() {
        assertThrows("are not supported. Implement the method 'hashCode'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectHashCode.class.getName())
        );
    }

    @Test
    public void testEquals() {
        assertThrows("are not supported. Implement the method 'equals'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectEquals.class.getName())
        );
    }

    @Test
    public void testToString() {
        assertThrows("are not supported. Implement the method 'toString'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectToString.class.getName())
        );
    }

    @Test
    public void testNotify() {
        assertThrows("are not supported. Implement the method 'notify'",
                CompilerException.class,
                () ->new Compiler().compile(ObjectNotify.class.getName())
        );
    }

    @Test
    public void testNotifyAll() {
        assertThrows("are not supported. Implement the method 'notifyAll'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectNotifyAll.class.getName())
        );
    }

    @Test
    public void testWaitLong() {
        assertThrows("are not supported. Implement the method 'wait'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectWaitLong.class.getName())
        );
    }

    @Test
    public void testWaitLongInt() {
        assertThrows("are not supported. Implement the method 'wait'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectWaitLongInt.class.getName())
        );
    }

    @Test
    public void testWaitNoParams() {
        assertThrows("are not supported. Implement the method 'wait'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectWaitNoParams.class.getName())
        );
    }

    @Test
    public void testGetClass() {
        assertThrows("are not supported. Implement the method 'getClass'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectGetClass.class.getName())
        );
    }

    @Test
    public void testClone() {
        assertThrows("are not supported. Implement the method 'clone'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectClone.class.getName())
        );
    }

    @Test
    public void testFinalize() {
        assertThrows("are not supported. Implement the method 'finalize'",
                CompilerException.class,
                () -> new Compiler().compile(ObjectFinalize.class.getName())
        );
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
