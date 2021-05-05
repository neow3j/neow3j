package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.FindOptions;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Iterator.Struct;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IteratorTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(IteratorTestContract.class.getName());

    @BeforeClass
    public static void setUp() throws Throwable {
        ct.invokeFunctionAndAwaitExecution("setUp", array(string("val1"), string("val2"),
                string("val3"), string("val4"), string("val5")));
    }

    @Test
    public void getIteratorFromStorageAndIterate() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        List<StackItem> arr = response.getInvocationResult().getStack().get(0).getList();
        assertThat(arr.size(), is(5));
        assertThat(arr.get(0).getString(), is("val1"));
        assertThat(arr.get(1).getString(), is("val2"));
        assertThat(arr.get(2).getString(), is("val3"));
        assertThat(arr.get(3).getString(), is("val4"));
        assertThat(arr.get(4).getString(), is("val5"));
    }

    static class IteratorTestContract {

        static StorageContext ctx = Storage.getStorageContext();
        static ByteString prefix = hexToBytes("010203");

        public static void setUp(ByteString[] values) {
            for (ByteString val : values) {
                Storage.put(ctx, prefix.concat(val), val);
            }
        }

        public static io.neow3j.devpack.List<ByteString> getIteratorFromStorageAndIterate() {
            Iterator<Struct<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);

            io.neow3j.devpack.List<ByteString> values = new io.neow3j.devpack.List<>();
            while (it.next()) {
                values.add(it.get().value);
            }
            return values;
        }

    }

}
