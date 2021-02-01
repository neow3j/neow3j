package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.Role;
import io.neow3j.devpack.contracts.RoleManagement;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

public class RoleManagementTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(RoleManagementTestContract.class.getName());
    }

    @Test
    public void getHash() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getValue(),
                is(Numeric.hexStringToByteArray("c0073f4c7069bf38995780c9da065f9b3949ea7a")));
    }

    @Test
    public void setAndGetDesignateAsRole() throws Throwable {
        byte[] pubKey = defaultAccount.getECKeyPair().getPublicKey().getEncoded(true);

        signAsCommittee();
        String txHash = invokeFunctionAndAwaitExecution("designateAsRole",
                integer(Role.STATE_VALIDATOR), array(publicKey(pubKey)));

        int blockIndex = neow3j.getTransactionHeight(txHash).send().getHeight().intValue();
        ArrayStackItem pubKeys = neow3j.invokeFunction(
                "c0073f4c7069bf38995780c9da065f9b3949ea7a",
                "getDesignatedByRole",
                Arrays.asList(integer(4), integer(blockIndex + 1)))
                .send().getInvocationResult().getStack().get(0).asArray();
        assertThat(pubKeys.get(0).asByteString().getValue(), is(pubKey));

        NeoInvokeFunction response = callInvokeFunction("getDesignatedByRole",
                integer(Role.STATE_VALIDATOR), integer(blockIndex + 1));
        pubKeys = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(pubKeys.get(0).asByteString().getValue(), is(pubKey));
    }

    static class RoleManagementTestContract {

        public static Hash160 getHash() {
            return RoleManagement.getHash();
        }

        public static ECPoint[] getDesignatedByRole(byte role, int index) {
            return RoleManagement.getDesignatedByRole(role, index);
        }

        public static void designateAsRole(byte role, ECPoint[] publicKeys) {
            RoleManagement.designateAsRole(role, publicKeys);
        }
    }
}
