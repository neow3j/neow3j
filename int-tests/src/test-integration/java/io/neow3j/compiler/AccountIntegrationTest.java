package io.neow3j.compiler;

import io.neow3j.devpack.AccountUtils;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static io.neow3j.test.TestProperties.committeeAccountAddress;
import static io.neow3j.test.TestProperties.defaultAccountAddress;
import static io.neow3j.test.TestProperties.defaultAccountPublicKey;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AccountIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(AccountIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void deriveStandardAccountScriptHash() throws IOException {
        NeoInvokeFunction res = ct.callInvokeFunction(testName, publicKey(defaultAccountPublicKey()));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(), is(defaultAccountAddress()));
    }

    @Test
    public void deriveMultiSigAccountScriptHash() throws IOException {
        NeoInvokeFunction res = ct.callInvokeFunction(testName, integer(1),
                array(publicKey(defaultAccountPublicKey())));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(), is(committeeAccountAddress()));
    }

    static class AccountIntegrationTestContract {

        public static Hash160 deriveStandardAccountScriptHash(ECPoint pubKey) {
            return AccountUtils.deriveStandardAccountScriptHash(pubKey);
        }

        public static Hash160 deriveMultiSigAccountScriptHash(int m, ECPoint[] pubKeys) {
            return AccountUtils.deriveMultiSigAccountScriptHash(m, pubKeys);
        }

    }

}
