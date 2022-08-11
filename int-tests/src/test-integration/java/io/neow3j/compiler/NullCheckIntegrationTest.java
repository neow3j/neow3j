package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.devpack.contracts.StdLib;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static io.neow3j.types.ContractParameter.integer;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NullCheckIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(NullCheckIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void nullCheckWithLargeObject() throws IOException {
        NeoInvokeFunction resp = ct.callInvokeFunction(testName, integer(20));
        assertTrue(resp.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void notNullCheckWithLargeObject() throws IOException {
        NeoInvokeFunction resp = ct.callInvokeFunction(testName, integer(20));
        assertTrue(resp.getInvocationResult().getStack().get(0).getBoolean());
    }

    static class NullCheckIntegrationTestContract {

        public static boolean nullCheckWithLargeObject(int i) throws Exception {
            POJO obj = new POJO(i);
            if (new StdLib().serialize(obj).length() < 32) {
                throw new Exception("Object is not big enough for this test.");
            }
            if (new StdLib().serialize(obj) == null) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            return true;
        }

        public static boolean notNullCheckWithLargeObject(int i) throws Exception {
            POJO obj = new POJO(i);
            if (new StdLib().serialize(obj).length() < 32) {
                throw new Exception("Object is not big enough for this test.");
            }
            if (new StdLib().serialize(obj) != null) {
                if (obj != null) {
                    return true;
                }
            }
            return false;
        }

        @Struct
        static class POJO {

            public int i;
            public Hash160[] addresses;

            public POJO(int i) {
                this.i = i;
                this.addresses = new Hash160[i];
            }

        }

    }

}
