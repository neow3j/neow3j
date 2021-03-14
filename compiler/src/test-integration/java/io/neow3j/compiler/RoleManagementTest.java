package io.neow3j.compiler;

import static io.neow3j.TestProperties.roleManagementHash;
import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.Hash256;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.Role;
import io.neow3j.devpack.contracts.RoleManagement;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.util.List;

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
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(roleManagementHash()));
    }

    @Test
    public void setAndGetDesignateAsRole() throws Throwable {
        byte[] pubKey = defaultAccount.getECKeyPair().getPublicKey().getEncoded(true);
        signAsCommittee();

        Hash256 txHash = invokeFunctionAndAwaitExecution("designateAsRole",
                integer(Role.STATE_VALIDATOR), array(publicKey(pubKey)));
        int blockIndex = neow3j.getTransactionHeight(txHash).send().getHeight().intValue();

        // Check if the role has been successfully assigned.
        List<ECKeyPair.ECPublicKey> pubKeys = new io.neow3j.contract.RoleManagement(neow3j)
                .getDesignatedByRole(io.neow3j.protocol.core.Role.STATE_VALIDATOR, blockIndex + 1);
        assertThat(pubKeys.get(0).getEncoded(true), is(pubKey));

        // Test if the designate can be fetched via a smart contract call.
        NeoInvokeFunction response = callInvokeFunction("getDesignatedByRole",
                integer(Role.STATE_VALIDATOR), integer(blockIndex + 1));
        List<StackItem> pubKeysItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(pubKeysItem.get(0).getByteArray(), is(pubKey));
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
