package io.neow3j.compiler;

import static io.neow3j.TestProperties.defaultAccountPublicKey;
import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.utils.ContractCompilationTestRule;
import io.neow3j.devpack.Crypto;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class CryptoIntegrationTest extends ContractTest {

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(privateNetContainer)
            .around(
                    new ContractCompilationTestRule(
                            CryptoIntegrationTestContract.class.getName(),
                            privateNetContainer
                    )
            );

    @Test
    public void checkSig() throws Throwable {
        signWithDefaultAccount();
        String pubKey = defaultAccountPublicKey();
        // Some signature, but not the correct one that is going to be on the transaction.
        // Therefore, the checkSig will return false.
        String signature =
                "a30ded6e19be5573a6f6a5ff37c35d4ae76ff35ab4bee03b5b5bfbbef371f812ff70b5b480462807948a2ffb24dd8771484d9ca5a90333f9e6db69a6c8802a63";
        io.neow3j.contract.Hash256 res =
                invokeFunctionAndAwaitExecution(publicKey(pubKey), byteArray(signature));
        List<NeoApplicationLog.Execution> executions =
                neow3j.getApplicationLog(res).send().getApplicationLog().getExecutions();
        assertFalse(executions.get(0).getStack().get(0).getBoolean());
    }

    @Test
    public void checkMultiSig() throws Throwable {
        String pubKey1 = defaultAccountPublicKey();
        String pubKey2 = defaultAccountPublicKey();
        // Some signatures, but not the correct ones that are going to be on the transaction.
        // Therefore, the checkMultiSig will return false.
        String signature1 =
                "a30ded6e19be5573a6f6a5ff37c35d4ae76ff35ab4bee03b5b5bfbbef371f812ff70b5b480462807948a2ffb24dd8771484d9ca5a90333f9e6db69a6c8802a63";
        String signature2 =
                "a30ded6e19be5573a6f6a5ff37c35d4ae76ff35ab4bee03b5b5bfbbef371f812ff70b5b480462807948a2ffb24dd8771484d9ca5a90333f9e6db69a6c8802a63";
        io.neow3j.contract.Hash256 res = invokeFunctionAndAwaitExecution(
                array(publicKey(pubKey1), publicKey(pubKey2)),
                array(byteArray(signature1), byteArray(signature2)));
        List<NeoApplicationLog.Execution> executions =
                neow3j.getApplicationLog(res).send().getApplicationLog().getExecutions();
        assertFalse(executions.get(0).getStack().get(0).getBoolean());
    }

    @Test
    public void hash256() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(byteArray("0102030405"));
        assertThat(res.getInvocationResult().getStack().get(0).getHexString(),
                is("a26baf5a9a07d9eb7ba10f43924dcdf3f75f0abf066cd9f0c76f983121302e01"));
    }

    @Test
    public void hash160() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(byteArray("0102030405"));
        assertThat(res.getInvocationResult().getStack().get(0).getHexString(),
                is("1fcc83c91e862661592480531afa87c3e2f59332"));
    }

    static class CryptoIntegrationTestContract {

        public static boolean checkSig(ECPoint pubKey, String signature) {
            return Crypto.checkSig(pubKey, signature);
        }

        public static boolean checkMultiSig(ECPoint[] pubKeys, String[] signatures) {
            return Crypto.checkMultisig(pubKeys, signatures);
        }

        public static Hash256 hash256(String value) {
            return Crypto.hash256(value);
        }

        public static Hash160 hash160(String value) {
            return Crypto.hash160(value);
        }
    }

}
