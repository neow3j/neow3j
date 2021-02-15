package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.string;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.devpack.Runtime;
import io.neow3j.protocol.core.methods.response.NeoInvokeContractVerify;
import io.neow3j.transaction.Signer;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

public class VerificationMethodIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(VerificationMethodIntegrationTestContract.class.getName());
        // The RPC method invokecontractverify requires an open wallet on the
        // neo-node.
        neow3j.openWallet("wallet.json","neo").send();
    }

    @Test
    public void callVerifyWithContractOwner() throws Throwable {
        NeoInvokeContractVerify response = neow3j
                .invokeContractVerify(contract.getScriptHash().toString(),
                        Arrays.asList(string("hello, world!")),
                        Signer.calledByEntry(defaultAccount.getScriptHash()))
                .send();

        assertTrue(response.getInvocationResult().getStack().get(0).asBoolean().getValue());
    }

    @Test
    public void callVerifyWithOtherSigner() throws Throwable {
        NeoInvokeContractVerify response = neow3j
                .invokeContractVerify(contract.getScriptHash().toString(),
                        Arrays.asList(string("hello, world!")),
                        Signer.calledByEntry(committee.getScriptHash()))
                .send();

        assertFalse(response.getInvocationResult().getStack().get(0).asBoolean().getValue());
    }

    static class VerificationMethodIntegrationTestContract {

        // default account
        static Hash160 ownerScriptHash =
                StringLiteralHelper.addressToScriptHash("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");

        @OnVerification
        public static boolean verify(String s) {
            return Runtime.checkWitness(ownerScriptHash);
        }

    }
}
