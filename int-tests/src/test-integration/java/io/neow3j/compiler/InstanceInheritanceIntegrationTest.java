package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.Notification;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.types.StackItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;

import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class InstanceInheritanceIntegrationTest {

    private String testName;
    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(InstanceInheritanceContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void testStructMultiInheritance() throws IOException {
        io.neow3j.types.Hash160 scriptHash = new io.neow3j.types.Hash160("7b5a47622946b9e8c5fef9f6d31b6c9fc7f8d3fe");
        InvocationResult result = ct.callInvokeFunction(testName, integer(42), string("io/neow3j"), hash160(scriptHash),
                string("axlabs"), integer(2468)).getInvocationResult();

        assertThat(result.getState(), is(NeoVMStateType.HALT));
        List<StackItem> stack = result.getStack();
        assertThat(stack, hasSize(1));
        assertThat(stack.get(0).getType(), is(StackItemType.ARRAY));
        List<StackItem> list = stack.get(0).getList();
        assertThat(list, hasSize(5));

        StackItem structValue1 = list.get(0);
        assertThat(structValue1.getType(), is(StackItemType.ARRAY));
        List<StackItem> simpleStruct1 = structValue1.getList();
        assertThat(simpleStruct1, hasSize(1));
        assertThat(simpleStruct1.get(0).getType(), is(StackItemType.INTEGER));
        assertThat(simpleStruct1.get(0).getInteger().intValue(), is(42));

        StackItem structValue2 = list.get(1);
        assertThat(structValue2.getType(), is(StackItemType.BYTE_STRING));
        assertThat(structValue2.getString(), is("io/neow3j"));

        StackItem structValue3 = list.get(2);
        assertThat(structValue3.getType(), is(StackItemType.BYTE_STRING));
        assertThat(structValue3.getAddress(), is(scriptHash.toAddress()));

        StackItem structValue4 = list.get(3);
        assertThat(structValue4.getType(), is(StackItemType.BYTE_STRING));
        assertThat(structValue4.getString(), is("axlabs"));

        StackItem structValue5 = list.get(4);
        assertThat(structValue5.getType(), is(StackItemType.BYTE_STRING));
        assertThat(structValue5.getInteger().intValue(), is(2468));
    }

    @Test
    public void testReuseChildClassConstructorMethodInNeoModule() throws IOException {
        io.neow3j.types.Hash160 scriptHash = new io.neow3j.types.Hash160("7b5a47622946b9e8c5fef9f6d31b6c9fc7f8d3fe");
        InvocationResult result = ct.callInvokeFunction(testName, integer(42), string("io/neow3j"), hash160(scriptHash),
                string("axlabs"), integer(2468)).getInvocationResult();

        assertThat(result.getState(), is(NeoVMStateType.HALT));
        List<StackItem> stack = result.getStack();
        assertThat(stack, hasSize(1));
        assertThat(stack.get(0).getType(), is(StackItemType.ARRAY));
        List<StackItem> list = stack.get(0).getList();
        // Check that the field size of the struct is set correctly by the compiler.
        assertThat(list, hasSize(5));
    }

    @Test
    public void testInheritedFieldAccess() throws IOException {
        io.neow3j.types.Hash160 scriptHash = new io.neow3j.types.Hash160("7b5a47622946b9e8c5fef9f6d31b6c9fc7f8d3fe");
        InvocationResult result = ct.callInvokeFunction(testName, integer(13579), string("io/neow3j"), hash160(scriptHash),
                string("axlabs"), integer(2468)).getInvocationResult();

        assertThat(result.getState(), is(NeoVMStateType.HALT));
        assertThat(result.getStack(), hasSize(1));
        assertThat(result.getStack().get(0).getType(), is(StackItemType.ARRAY));
        List<StackItem> list = result.getStack().get(0).getList();
        assertThat(list, hasSize(1));
        assertThat(list.get(0).getType(), is(StackItemType.INTEGER));
        assertThat(list.get(0).getInteger().intValue(), is(13579));
    }

    @Test
    public void testFieldAccessWithEvent() throws Throwable {
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution(testName, integer(42000), string("io/neow3j"));

        NeoApplicationLog applicationLog = ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog();
        NeoApplicationLog.Execution exec = applicationLog.getExecutions().get(0);
        assertThat(exec.getState(), is(NeoVMStateType.HALT));
        assertThat(exec.getNotifications(), hasSize(1));

        Notification notification = exec.getNotifications().get(0);
        assertThat(notification.getEventName(), is("event"));
        assertThat(notification.getState().getType(), is(StackItemType.ARRAY));
        assertThat(notification.getState().getList(), hasSize(2));
        assertThat(notification.getState().getList().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(notification.getState().getList().get(0).getInteger().intValue(), is(42000));
        assertThat(notification.getState().getList().get(1).getType(), is(StackItemType.BYTE_STRING));
        assertThat(notification.getState().getList().get(1).getString(), is("io/neow3j"));
    }

    @Test
    public void testFieldAccessWithEventFromStructParameter() throws Throwable {
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution(testName, array(integer(13), string("neo")));
        NeoApplicationLog applicationLog = ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog();

        NeoApplicationLog.Execution exec = applicationLog.getExecutions().get(0);
        assertThat(exec.getState(), is(NeoVMStateType.HALT));
        assertThat(exec.getNotifications(), hasSize(1));

        Notification notification = exec.getNotifications().get(0);
        assertThat(notification.getEventName(), is("event"));
        assertThat(notification.getState().getType(), is(StackItemType.ARRAY));
        assertThat(notification.getState().getList(), hasSize(2));
        assertThat(notification.getState().getList().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(notification.getState().getList().get(0).getInteger().intValue(), is(13));
        assertThat(notification.getState().getList().get(1).getType(), is(StackItemType.BYTE_STRING));
        assertThat(notification.getState().getList().get(1).getString(), is("neo"));
    }

    @Test
    public void testStructWithParentTryCatch() throws Throwable {
        InvocationResult result = ct.callInvokeFunction(testName, integer(10), integer(22)).getInvocationResult();

        assertThat(result.getState(), is(NeoVMStateType.HALT));
        assertThat(result.getStack(), hasSize(1));
        assertThat(result.getStack().get(0).getType(), is(StackItemType.ARRAY));
        List<StackItem> list = result.getStack().get(0).getList();
        assertThat(list, hasSize(2));
        assertThat(list.get(0).getType(), is(StackItemType.INTEGER));
        assertThat(list.get(0).getInteger().intValue(), is(0));
        assertThat(list.get(1).getType(), is(StackItemType.INTEGER));
        assertThat(list.get(1).getInteger().intValue(), is(22));

        result = ct.callInvokeFunction(testName, integer(1), integer(23)).getInvocationResult();

        assertThat(result.getState(), is(NeoVMStateType.HALT));
        assertThat(result.getStack(), hasSize(1));
        assertThat(result.getStack().get(0).getType(), is(StackItemType.ARRAY));
        list = result.getStack().get(0).getList();
        assertThat(list, hasSize(2));
        assertThat(list.get(0).getType(), is(StackItemType.INTEGER));
        assertThat(list.get(0).getInteger().intValue(), is(1));
        assertThat(list.get(1).getType(), is(StackItemType.INTEGER));
        assertThat(list.get(1).getInteger().intValue(), is(23));
    }

    static class InstanceInheritanceContract {

        static Event2Args<Integer, String> event;

        public static ChildClass testStructMultiInheritance(int p1, String p2, Hash160 p3, String p4, int p5) {
            return new ChildClass(p1, p2, p3, p4, p5);
        }

        public static ChildClass testReuseChildClassConstructorMethodInNeoModule(int p1, String p2, Hash160 p3,
                String p4, int p5) {
            return new ChildClass(p1, p2, p3, p4, p5);
        }

        public static SimpleStruct testInheritedFieldAccess(int p1, String p2, Hash160 p3, String p4, int p5) {
            ChildClass childClass = testStructMultiInheritance(p1, p2, p3, p4, p5);
            return childClass.first;
        }

        @Struct
        static class ChildClass extends ParentClass {
            public String fourth;
            public ByteString fifth;

            ChildClass(int first, String second, Hash160 third, String fourth, int fifth) {
                super(first, new ByteString(second), third);
                this.fourth = fourth;
                this.fifth = new ByteString(fifth);
            }
        }

        @Struct
        static class ParentClass extends GrandParentClass {
            public ByteString second;
            public Hash160 third;

            ParentClass(int first, ByteString second, Hash160 third) {
                super(first);
                this.second = second;
                this.third = third;
            }
        }

        @Struct
        static class GrandParentClass {
            public SimpleStruct first;

            GrandParentClass(int first) {
                this.first = new SimpleStruct(first);
            }
        }

        @Struct
        static class SimpleStruct {
            public int i;

            SimpleStruct(int i) {
                this.i = i;
            }
        }

        public static void testFieldAccessWithEvent(int i, String s) {
            MySubclass subclass = new MySubclass(i, s);
            event.fire(subclass.i, subclass.s);
        }

        public static void testFieldAccessWithEventFromStructParameter(MySubclass subclass) {
            event.fire(subclass.i, subclass.s);
        }

        @Struct
        static class MyClass {
            public int i;

            MyClass(int i) {
                this.i = i;
            }
        }

        @Struct
        static class MySubclass extends MyClass {
            public String s;

            MySubclass(int i, String s) {
                super(i);
                this.s = s;
            }
        }

        public static StructWithParentTryCatch testStructWithParentTryCatch(int i1, int i2) {
            return new StructWithParentTryCatch(i1, i2);
        }

        @Struct
        static class StructWithTryCatch {
            public int i1;

            StructWithTryCatch(int i1) {
                try {
                    if (i1 != 1) {
                        throw new Exception("Exception");
                    } else {
                        this.i1 = 1;
                    }
                } catch (Exception e) {
                    this.i1 = 0;
                }
            }
        }

        @Struct
        static class StructWithParentTryCatch extends StructWithTryCatch {
            public int i2;

            StructWithParentTryCatch(int i1, int i2) {
                super(i1);
                this.i2 = i2;
            }
        }

    }

}
