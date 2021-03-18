package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.Helper;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ObjectsTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            ObjectsTestContract.class.getName());

    @Test
    public void instantiateObject() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.string("Neo"), // 0x4e656f
                ContractParameter.integer(1));

        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{(byte) 0x4e, (byte) 0x65, (byte) 0x6f, 0x01}));
    }

    @Test
    public void returnObject() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.string("Neo"), // 0x4e656f
                ContractParameter.integer(1));

        List<StackItem> obj = response.getInvocationResult().getStack().get(0).getList();
        assertThat(obj.get(0).getString(), is("Neo"));
        assertThat(obj.get(1).getInteger().intValue(), is(1));
    }

    @Test
    public void passObjectAsArgument() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, ContractParameter.array(
                ContractParameter.string("Neo"), /* 0x4e656f*/
                ContractParameter.integer(1)));

        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(new byte[]{(byte) 0x4e, (byte) 0x65, (byte) 0x6f, 0x01}));
    }


    static class ObjectsTestContract {

        public static byte[] instantiateObject(String s, int i) {
            MyClass c = new MyClass(s, i);
            return Helper.concat(Helper.toByteArray(c.s), Helper.toByteArray(Helper.asByte(c.i)));
        }

        public static MyClass returnObject(String s, int i) {
            MyClass c = new MyClass(s, i);
            return c;
        }

        public static byte[] passObjectAsArgument(MyClass c) {
            return Helper.concat(Helper.toByteArray(c.s), Helper.toByteArray(Helper.asByte(c.i)));
        }
    }

    static class MyClass {

        public String s;
        public int i;

        public MyClass(String s, int i) {
            this.s = s;
            this.i = i;
        }
    }
}

