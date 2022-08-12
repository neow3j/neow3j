package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Crypto;
import io.neow3j.devpack.ECPoint;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static io.neow3j.test.TestProperties.defaultAccountPublicKey;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.publicKey;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CryptoIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(CryptoIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void checkSig() throws Throwable {
        ct.signWithDefaultAccount();
        String pubKey = defaultAccountPublicKey();
        // Some signature, but not the correct one that is going to be on the transaction.
        // Therefore, the checkSig will return false.
        String signature =
                "a30ded6e19be5573a6f6a5ff37c35d4ae76ff35ab4bee03b5b5bfbbef371f812ff70b5b480462807948a2ffb24dd8771484d9ca5a90333f9e6db69a6c8802a63";
        io.neow3j.types.Hash256 res = ct.invokeFunctionAndAwaitExecution(testName,
                publicKey(pubKey), byteArray(signature));
        List<NeoApplicationLog.Execution> executions =
                ct.getNeow3j().getApplicationLog(res).send().getApplicationLog().getExecutions();
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
        io.neow3j.types.Hash256 res = ct.invokeFunctionAndAwaitExecution(testName,
                array(publicKey(pubKey1), publicKey(pubKey2)),
                array(byteArray(signature1), byteArray(signature2)));
        List<NeoApplicationLog.Execution> executions =
                ct.getNeow3j().getApplicationLog(res).send().getApplicationLog().getExecutions();
        assertFalse(executions.get(0).getStack().get(0).getBoolean());
    }

    static class CryptoIntegrationTestContract {

        public static boolean checkSig(ECPoint pubKey, ByteString signature) {
            return Crypto.checkSig(pubKey, signature);
        }

        public static boolean checkMultiSig(ECPoint[] pubKeys, ByteString[] signatures) {
            return Crypto.checkMultisig(pubKeys, signatures);
        }

    }

}
