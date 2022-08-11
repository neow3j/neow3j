package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.devpack.contracts.StdLib;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ObjectsTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(ObjectsTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

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

    @Test
    public void storeAndRetrieveLargePOJO() throws Throwable {
        int i = 5;
        String byteArray = "010203";
        ct.invokeFunctionAndAwaitExecution("put", string("some String"), byteArray(byteArray), integer(i));

        NeoInvokeFunction response = ct.callInvokeFunction("get");

        List<StackItem> pojo2 = response.getInvocationResult().getStack().get(0).getList();
        assertThat(pojo2.size(), is(14));
        assertThat(pojo2.get(0).getString(), is("other String"));
        assertThat(pojo2.get(1).getHexString(), is(byteArray));

        List<StackItem> pojo1 = pojo2.get(2).getList();
        assertThat(pojo1.get(0).getString(), is("other String in POJO1"));
        assertThat(pojo1.get(1).getInteger().intValue(), is(i));

        assertThat(pojo2.get(3).getList().size(), is(i));
        assertThat(pojo2.get(4).getInteger(), is(new BigInteger("2000000000")));
        assertThat(pojo2.get(11).getInteger(), is(new BigInteger("500000000")));
    }

    @Test
   public void forLoopInObjectMethodWithoutState() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, ContractParameter.integer(5));
        List<StackItem> ints = response.getInvocationResult().getStack().get(0).getList();
        assertThat(ints.get(0).getInteger().intValue(), is(0));
        assertThat(ints.get(1).getInteger().intValue(), is(2));
        assertThat(ints.get(2).getInteger().intValue(), is(4));
        assertThat(ints.get(3).getInteger().intValue(), is(6));
        assertThat(ints.get(4).getInteger().intValue(), is(8));
   }

    @Test
    public void forLoopInObjectMethodWithState() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> strings = response.getInvocationResult().getStack().get(0).getList();
        assertThat(strings.get(0).getString(), is("hello"));
        assertThat(strings.get(1).getString(), is("world"));
        assertThat(strings.get(2).getString(), is("!"));
    }

    static class ObjectsTestContract {

        public static byte[] instantiateObject(String s, int i) {
            POJO1 c = new POJO1(s, i);
            return Helper.concat(Helper.toByteArray(c.s), Helper.toByteArray(Helper.asByte(c.i)));
        }

        public static POJO1 returnObject(String s, int i) {
            POJO1 c = new POJO1(s, i);
            return c;
        }

        public static byte[] passObjectAsArgument(POJO1 c) {
            return Helper.concat(Helper.toByteArray(c.s), Helper.toByteArray(Helper.asByte(c.i)));
        }

        static StorageContext ctx = Storage.getStorageContext();

        public static void put(String s, byte[] bs, int i) {
            Storage.put(ctx, "key", new StdLib().serialize(new POJO2(s, bs, i)));
        }

        public static Object get() {
            POJO2 obj = (POJO2) new StdLib().deserialize(Storage.get(ctx, "key"));
            obj.s = "other String";
            obj.pojo1.s = "other String in POJO1";
            obj.i1 = 2_000_000_000;
            obj.i8 = 500_000_000;
            return obj;
        }

        public static int[] forLoopInObjectMethodWithoutState(int k) {
            POJO3 obj = new POJO3();
            return obj.method(k);
        }

        public static String[] forLoopInObjectMethodWithState() {
            POJO4 c = new POJO4("hello", "world", "!");
            return c.method();
        }
    }

    @Struct
    static class POJO1 {

        String s;
        int i;

        POJO1(String s, int i) {
            this.s = s;
            this.i = i;
        }
    }

    @Struct
    static class POJO2 {
        String s;
        byte[] bs;
        POJO1 pojo1;
        Hash160[] owners;
        int i1;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;

        POJO2(String s, byte[] bs, int i) {
            this.s = s;
            this.bs = bs;
            this.pojo1 = new POJO1(s, i);
            this.owners = new Hash160[i];
            this.i1 = i;
            this.i2 = i;
            this.i3 = i;
            this.i4 = i;
            this.i5 = i;
            this.i6 = i;
            this.i7 = i;
            this.i8 = i;
            this.i9 = i;
            this.i10 = i;
        }
    }

    @Struct
    static class POJO3 {

        public int[] method(int k) {
            int[] ints = new int[k];
            for (int i = 0; i < k; i++) {
                ints[i] = i * 2;
            }
            return ints;
        }
    }

    @Struct
    public static class POJO4 {

        io.neow3j.devpack.List<String> list = new io.neow3j.devpack.List<>();

        public POJO4(String... args) {
            for (String arg : args) {
                list.add(arg);
            }
        }

        public String[] method() {
            String[] copy = new String[this.list.size()];
            for (int i = 0; i < this.list.size(); i++) {
                copy[i] = list.get(i);
            }
            return copy;
        }

    }

}
