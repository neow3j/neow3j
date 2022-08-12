package io.neow3j.contract;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Role;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigInteger;
import java.util.List;

import static io.neow3j.contract.IntegrationTestHelper.COMMITTEE_ACCOUNT;
import static io.neow3j.contract.IntegrationTestHelper.DEFAULT_ACCOUNT;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThan;
import static io.neow3j.utils.Await.waitUntilBlockCountIsGreaterThanZero;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Testcontainers
public class RoleManagementIntegrationTest {

    private static Neow3j neow3j;
    private static RoleManagement roleManagement;

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl()));
        waitUntilBlockCountIsGreaterThanZero(neow3j);
        roleManagement = new RoleManagement(neow3j);
    }

    @Test
    public void testDesignateByRoleAndGetDesignated_stateValidator() throws Throwable {
        Account account = Account.create();
        Transaction tx = roleManagement.designateAsRole(Role.STATE_VALIDATOR,
                        asList(account.getECKeyPair().getPublicKey()))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        // The designation is active starting on the next block after the designate transaction
        BigInteger blockCount = neow3j.getBlockCount().send().getBlockCount();
        waitUntilBlockCountIsGreaterThan(neow3j, blockCount);
        BigInteger nextBlockIndex = blockCount.add(BigInteger.ONE);

        List<ECKeyPair.ECPublicKey> designatedByRole =
                roleManagement.getDesignatedByRole(Role.STATE_VALIDATOR, nextBlockIndex);

        assertThat(designatedByRole, hasSize(1));
        assertThat(designatedByRole.get(0), is(account.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testDesignateByRoleAndGetDesignated_oracle() throws Throwable {
        Account account = Account.create();
        Transaction tx = roleManagement.designateAsRole(Role.ORACLE,
                        asList(account.getECKeyPair().getPublicKey()))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        // The designation is active starting on the next block after the designate transaction
        BigInteger blockCount = neow3j.getBlockCount().send().getBlockCount();
        waitUntilBlockCountIsGreaterThan(neow3j, blockCount);
        BigInteger nextBlockIndex = blockCount.add(BigInteger.ONE);

        List<ECKeyPair.ECPublicKey> designatedByRole =
                roleManagement.getDesignatedByRole(Role.ORACLE, nextBlockIndex);

        assertThat(designatedByRole, hasSize(1));
        assertThat(designatedByRole.get(0), is(account.getECKeyPair().getPublicKey()));
    }

    @Test
    public void testDesignateByRoleAndGetDesignated_fsAlphabetNode() throws Throwable {
        Account account = Account.create();
        Transaction tx = roleManagement.designateAsRole(Role.NEO_FS_ALPHABET_NODE,
                        asList(account.getECKeyPair().getPublicKey()))
                .signers(calledByEntry(COMMITTEE_ACCOUNT))
                .getUnsignedTransaction();
        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(tx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());
        Hash256 txHash = tx.addWitness(multiSigWitness).send().getSendRawTransaction().getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);

        // The designation is active starting on the next block after the designate transaction
        BigInteger blockCount = neow3j.getBlockCount().send().getBlockCount();
        waitUntilBlockCountIsGreaterThan(neow3j, blockCount);
        BigInteger nextBlockIndex = blockCount.add(BigInteger.ONE);

        List<ECKeyPair.ECPublicKey> designatedByRole =
                roleManagement.getDesignatedByRole(Role.NEO_FS_ALPHABET_NODE, nextBlockIndex);

        assertThat(designatedByRole, hasSize(1));
        assertThat(designatedByRole.get(0), is(account.getECKeyPair().getPublicKey()));
    }

}
