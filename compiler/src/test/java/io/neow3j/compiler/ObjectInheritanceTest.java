package io.neow3j.compiler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

public class ObjectInheritanceTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testHashCode() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'hashCode'");
        new Compiler().compile(ObjectHashCode.class.getName());
    }

    @Test
    public void testEquals() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'equals'");
        new Compiler().compile(ObjectEquals.class.getName());
    }

    @Test
    public void testToString() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'toString'");
        new Compiler().compile(ObjectToString.class.getName());
    }

    @Test
    public void testNotify() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'notify'");
        new Compiler().compile(ObjectNotify.class.getName());
    }

    @Test
    public void testNotifyAll() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'notifyAll'");
        new Compiler().compile(ObjectNotifyAll.class.getName());
    }

    @Test
    public void testWaitLong() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'wait'");
        new Compiler().compile(ObjectWaitLong.class.getName());
    }

    @Test
    public void testWaitLongInt() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'wait'");
        new Compiler().compile(ObjectWaitLongInt.class.getName());
    }

    @Test
    public void testWaitNoParams() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'wait'");
        new Compiler().compile(ObjectWaitNoParams.class.getName());
    }

    @Test
    public void testGetClass() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("are not supported. Implement the method 'getClass'");
        new Compiler().compile(ObjectGetClass.class.getName());
    }

    @Test
    public void testClone() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("'clone' and 'finalize' of the superclass Object are not ");
        exceptionRule.expectMessage("avoid a 'super' call to");
        new Compiler().compile(ObjectClone.class.getName());
    }

    @Test
    public void testFinalize() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage("'finalize' of the superclass Object are not supported");
        exceptionRule.expectMessage("avoid a 'super' call to");
        new Compiler().compile(ObjectFinalize.class.getName());
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
        public static void test() throws CloneNotSupportedException {
            new TestClassWithSuper().clone();
        }
    }

}
