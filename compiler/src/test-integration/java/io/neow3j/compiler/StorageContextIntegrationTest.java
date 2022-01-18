package io.neow3j.compiler;

import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.protocol.core.response.InvocationResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StorageContextIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StorageContextIntegrationTestContract.class.getName());

    @Test
    public void getReadOnlyStorageContext() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        // The method tries to write with a read-only storage context, i.e., it should FAULT.
        assertThat(res.getState(), is(NeoVMStateType.FAULT));
        assertThat(res.getException(), is("Value does not fall within the expected range."));
    }


    static class StorageContextIntegrationTestContract {

        public static void getReadOnlyStorageContext() {
            StorageContext ctx = Storage.getStorageContext();
            ctx = ctx.asReadOnly();
            Storage.put(ctx, "key", "value");
        }

    }

}
