package io.neow3j.compiler;

import io.neow3j.contract.Hash256;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.Role;
import io.neow3j.devpack.contracts.RoleManagement;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.TestProperties.roleManagementHash;
import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RoleManagementTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            RoleManagementTestContract.class.getName());

    @Test
    public void getHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(roleManagementHash()));
    }

    @Test
    public void setAndGetDesignateAsRole() throws Throwable {
        byte[] pubKey = ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true);
        ct.signAsCommittee();

        Hash256 txHash = ct.invokeFunctionAndAwaitExecution("designateAsRole",
                integer(Role.STATE_VALIDATOR), array(publicKey(pubKey)));
        int blockIndex = ct.getNeow3j().getTransactionHeight(txHash).send().getHeight().intValue();

        // Check if the role has been successfully assigned.
        List<ECKeyPair.ECPublicKey> pubKeys = new io.neow3j.contract.RoleManagement(ct.getNeow3j())
                .getDesignatedByRole(io.neow3j.protocol.core.Role.STATE_VALIDATOR, blockIndex + 1);
        assertThat(pubKeys.get(0).getEncoded(true), is(pubKey));

        // Test if the designate can be fetched via a smart contract call.
        NeoInvokeFunction response = ct.callInvokeFunction("getDesignatedByRole",
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
