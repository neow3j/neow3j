package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.Hash256;
import io.neow3j.devpack.Account;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AccountIntegrationTest extends ContractTest {

    String DEFAULT_ACCOUNT_PUBKEY =
            "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
    String DEFAULT_ACCOUNT_ADDRESS = "NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj";
    String COMMITTEE_ACCOUNT_ADDRESS = "NX8GreRFGFK5wpGMWetpX93HmtrezGogzk";

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(AccountIntegrationTestContract.class.getName());
    }

    @Test
    public void createStandardAccount() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(publicKey(DEFAULT_ACCOUNT_PUBKEY));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(),
                is(DEFAULT_ACCOUNT_ADDRESS));
    }

    @Test
    public void createMultiSigAccount() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(
                integer(1), array(publicKey(DEFAULT_ACCOUNT_PUBKEY)));
        assertThat(res.getInvocationResult().getStack().get(0).getAddress(),
                is(COMMITTEE_ACCOUNT_ADDRESS));
    }

    @Test
    public void isStandard() throws Throwable {
        NeoInvokeFunction res = callInvokeFunction(
                hash160(io.neow3j.contract.Hash160.fromAddress(DEFAULT_ACCOUNT_ADDRESS)));
        assertTrue(res.getInvocationResult().getStack().get(0).getBoolean());
    }

    static class AccountIntegrationTestContract {

        public static Hash160 createStandardAccount(ECPoint pubKey)  {
            return Account.createStandardAccount(pubKey);
        }

        public static Hash160 createMultiSigAccount(int m, ECPoint[] pubKeys)  {
            return Account.createMultiSigAccount(m, pubKeys);
        }

        public static boolean isStandard(Hash160 hash) {
            return Account.isStandard(hash);
        }

    }
}
