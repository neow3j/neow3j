package io.neow3j.contract;

import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_WALLET;
import static io.neow3j.transaction.Signer.calledByEntry;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThan;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.NeoTestContainer;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Role;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Account;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

public class RoleManagementIntegrationTest {

    private static Neow3j neow3j;
    private static RoleManagement roleManagement;

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeClass
    public static void setUp() {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        roleManagement = new RoleManagement(neow3j);
    }

    @Test
    public void testDesignateByRoleAndGetDesignated_stateValidator() throws Throwable {
        Account account = Account.create();
        Hash256 txHash = roleManagement
                .designateAsRole(Role.STATE_VALIDATOR,
                        singletonList(account.getECKeyPair().getPublicKey()))
                .wallet(COMMITTEE_WALLET)
                .signers(calledByEntry(COMMITTEE_ACCOUNT.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        // The designation is active starting on the next block after the designate transaction
        BigInteger blockIndex = neow3j.getBlockCount().send().getBlockIndex();
        waitUntilBlockCountIsGreaterThan(neow3j, blockIndex);
        BigInteger nextBlockIndex = blockIndex.add(BigInteger.ONE);

        List<ECKeyPair.ECPublicKey> designatedByRole =
                roleManagement.getDesignatedByRole(Role.STATE_VALIDATOR, nextBlockIndex);

        assertThat(designatedByRole, hasSize(1));
        assertThat(designatedByRole.get(0), is(account.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testDesignateByRoleAndGetDesignated_oracle() throws Throwable {
        Account account = Account.create();
        Hash256 txHash = roleManagement
                .designateAsRole(Role.ORACLE,
                        singletonList(account.getECKeyPair().getPublicKey()))
                .wallet(COMMITTEE_WALLET)
                .signers(calledByEntry(COMMITTEE_ACCOUNT.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        // The designation is active starting on the next block after the designate transaction
        BigInteger blockIndex = neow3j.getBlockCount().send().getBlockIndex();
        waitUntilBlockCountIsGreaterThan(neow3j, blockIndex);
        BigInteger nextBlockIndex = blockIndex.add(BigInteger.ONE);

        List<ECKeyPair.ECPublicKey> designatedByRole =
                roleManagement.getDesignatedByRole(Role.ORACLE, nextBlockIndex);

        assertThat(designatedByRole, hasSize(1));
        assertThat(designatedByRole.get(0), is(account.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testDesignateByRoleAndGetDesignated_fsAlphabetNode() throws Throwable {
        Account account = Account.create();
        Hash256 txHash = roleManagement
                .designateAsRole(Role.NEO_FS_ALPHABET_NODE,
                        singletonList(account.getECKeyPair().getPublicKey()))
                .wallet(COMMITTEE_WALLET)
                .signers(calledByEntry(COMMITTEE_ACCOUNT.getScriptHash()))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        // The designation is active starting on the next block after the designate transaction
        BigInteger blockIndex = neow3j.getBlockCount().send().getBlockIndex();
        waitUntilBlockCountIsGreaterThan(neow3j, blockIndex);
        BigInteger nextBlockIndex = blockIndex.add(BigInteger.ONE);

        List<ECKeyPair.ECPublicKey> designatedByRole =
                roleManagement.getDesignatedByRole(Role.NEO_FS_ALPHABET_NODE, nextBlockIndex);

        assertThat(designatedByRole, hasSize(1));
        assertThat(designatedByRole.get(0), is(account.getECKeyPair().getPublicKey()));
    }

}
