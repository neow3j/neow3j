package io.neow3j.compiler;

import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.types.NeoVMStateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StorageContextIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            StorageContextIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

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
