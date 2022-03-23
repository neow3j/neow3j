package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.types.StackItemType;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class InstanceInheritanceIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct =
            new ContractTestRule(InstanceInheritanceIntegrationTestContract.class.getName());

    @Test
    public void testStructMultiInheritance() throws IOException {
        io.neow3j.types.Hash160 scriptHash = new io.neow3j.types.Hash160("7b5a47622946b9e8c5fef9f6d31b6c9fc7f8d3fe");

        InvocationResult result = ct.callInvokeFunction(testName, integer(42), string("neow3j"), hash160(scriptHash),
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
        assertThat(structValue2.getString(), is("neow3j"));

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

    static class InstanceInheritanceIntegrationTestContract {

        public static ChildClass testStructMultiInheritance(int p1, String p2, Hash160 p3, String p4, int p5) {
            return new ChildClass(p1, p2, p3, p4, p5);
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
        static class ParentClass extends InstanceInheritanceIntegrationTestContract.GrandParentClass {
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
            public SimpleStruct fifth;

            GrandParentClass(int fifth) {
                this.fifth = new SimpleStruct(fifth);
            }
        }

        @Struct
        static class SimpleStruct {
            public int i;

            SimpleStruct(int i) {
                this.i = i;
            }
        }
    }

}
