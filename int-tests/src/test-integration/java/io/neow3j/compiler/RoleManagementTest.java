package io.neow3j.compiler;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.Role;
import io.neow3j.devpack.contracts.RoleManagement;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.test.TestProperties.roleManagementHash;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RoleManagementTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(RoleManagementTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void getHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(roleManagementHash())));
    }

    @Test
    public void setAndGetDesignateAsRole() throws Throwable {
        byte[] pubKey = ct.getDefaultAccount().getECKeyPair().getPublicKey().getEncoded(true);
        ct.signWithCommitteeAccount();

        Hash256 txHash = ct.invokeFunctionAndAwaitExecution("designateAsRole",
                integer(Role.StateValidator), array(publicKey(pubKey)));
        int blockIndex = ct.getNeow3j().getTransactionHeight(txHash).send().getHeight().intValue();

        // Check if the role has been successfully assigned.
        List<ECKeyPair.ECPublicKey> pubKeys = new io.neow3j.contract.RoleManagement(ct.getNeow3j())
                .getDesignatedByRole(io.neow3j.protocol.core.Role.STATE_VALIDATOR,
                        BigInteger.valueOf(blockIndex + 1));
        assertThat(pubKeys.get(0).getEncoded(true), is(pubKey));

        // Test if the designate can be fetched via a smart contract call.
        NeoInvokeFunction response = ct.callInvokeFunction("getDesignatedByRole",
                integer(Role.StateValidator), integer(blockIndex + 1));
        List<StackItem> pubKeysItem = response.getInvocationResult().getStack().get(0).getList();
        assertThat(pubKeysItem.get(0).getByteArray(), is(pubKey));
    }

    @Permission(contract = "49cf4e5378ffcd4dec034fd98a174c5491e395e2")
    static class RoleManagementTestContract {

        public static Hash160 getHash() {
            return new RoleManagement().getHash();
        }

        public static ECPoint[] getDesignatedByRole(byte role, int index) {
            return new RoleManagement().getDesignatedByRole(role, index);
        }

        public static void designateAsRole(byte role, ECPoint[] publicKeys) {
            new RoleManagement().designateAsRole(role, publicKeys);
        }

    }

}
