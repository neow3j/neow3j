package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.string;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.neow3j.contract.NeoToken;
import io.neow3j.crypto.Sign;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.devpack.neo.Runtime;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.methods.response.NeoInvokeContractVerify;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.utils.Await;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

public class VerificationMethodIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(VerificationMethodIntegrationTestContract.class.getName());
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
