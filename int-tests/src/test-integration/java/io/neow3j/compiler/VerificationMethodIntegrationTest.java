package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.protocol.core.response.NeoInvokeContractVerify;
import io.neow3j.transaction.AccountSigner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.neow3j.types.ContractParameter.string;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerificationMethodIntegrationTest {

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            VerificationMethodIntegrationTestContract.class.getName());

    @BeforeAll
    public static void setUp() throws Throwable {
        // The RPC method invokecontractverify requires an open wallet on the neo-node.
        ct.getNeow3j().openWallet("wallet.json", "neo").send();
    }

    @Test
    public void callVerifyWithContractOwner() throws Throwable {
        NeoInvokeContractVerify response = ct.getNeow3j()
                .invokeContractVerify(ct.getContract().getScriptHash(),
                        singletonList(string("hello, world!")),
                        AccountSigner.calledByEntry(ct.getDefaultAccount().getScriptHash()))
                .send();

        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void callVerifyWithOtherSigner() throws Throwable {
        NeoInvokeContractVerify response = ct.getNeow3j().invokeContractVerify(
                ct.getContract().getScriptHash(),
                singletonList(string("hello, world!")),
                AccountSigner.calledByEntry(ct.getCommittee().getScriptHash()))
                .send();

        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    static class VerificationMethodIntegrationTestContract {

        // default account
        static Hash160 ownerScriptHash =
                StringLiteralHelper.addressToScriptHash("NM7Aky765FG8NhhwtxjXRx7jEL1cnw7PBP");

        @OnVerification
        public static boolean verify(String s) {
            return Runtime.checkWitness(ownerScriptHash);
        }

    }

}
