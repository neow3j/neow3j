package io.neow3j.compiler;

import io.neow3j.devpack.Account;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static io.neow3j.TestProperties.committeeAccountAddress;
import static io.neow3j.TestProperties.defaultAccountAddress;
import static io.neow3j.TestProperties.defaultAccountPublicKey;
import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AccountIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(AccountIntegrationTestContract.class.getName());
    }

    @Test
    public void createStandardAccount() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(publicKey(defaultAccountPublicKey()));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(),
                is(defaultAccountAddress()));
    }

    @Test
    public void createMultiSigAccount() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(
                integer(1), array(publicKey(defaultAccountPublicKey())));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(),
                is(committeeAccountAddress()));
    }

    @Ignore("See github issue https://github.com/neo-project/neo/issues/2399")
    @Test
    public void isStandard() throws Throwable {
        NeoInvokeFunction res = callInvokeFunction(
                hash160(io.neow3j.contract.Hash160.fromAddress(defaultAccountAddress())));
        assertTrue(res.getInvocationResult().getStack().get(0).getBoolean());
    }

    static class AccountIntegrationTestContract {

        public static Hash160 createStandardAccount(ECPoint pubKey) {
            return Account.createStandardAccount(pubKey);
        }

        public static Hash160 createMultiSigAccount(int m, ECPoint[] pubKeys) {
            return Account.createMultiSigAccount(m, pubKeys);
        }

        public static boolean isStandard(Hash160 hash) {
            return Account.isStandard(hash);
        }

    }
}
