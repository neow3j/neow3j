package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.test.TestProperties.client1AccountWIF;
import static io.neow3j.test.TestProperties.client2AccountWIF;
import static io.neow3j.test.TestProperties.defaultAccountWIF;
import static io.neow3j.test.TestProperties.gasTokenHash;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static io.neow3j.wallet.Account.createMultiSigAccount;
import static io.neow3j.wallet.Account.fromWIF;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class IntegrationTestHelper {

    // Native token hashes.
    static final Hash160 NEO_HASH = new Hash160(neoTokenHash());
    static final Hash160 GAS_HASH = new Hash160(gasTokenHash());

    public static final Account DEFAULT_ACCOUNT = fromWIF(defaultAccountWIF());
    public static final Account COMMITTEE_ACCOUNT =
            createMultiSigAccount(singletonList(DEFAULT_ACCOUNT.getECKeyPair().getPublicKey()), 1);
    public static final Account CLIENT_1 = fromWIF(client1AccountWIF());
    public static final Account CLIENT_2 = fromWIF(client2AccountWIF());

    public static void fundAccountsWithGas(Neow3j neow3j, BigDecimal amount, Account... accounts) throws Throwable {
        GasToken gasToken = new GasToken(neow3j);
        for (Account account : accounts) {
            transferFromGenesisToAccount(neow3j, gasToken, gasToken.toFractions(amount), account);
        }
    }

    public static void fundAccountsWithGas(Neow3j neow3j, Account... accounts) throws Throwable {
        GasToken gasToken = new GasToken(neow3j);
        BigInteger fractions = gasToken.toFractions(new BigDecimal("100000"));
        for (Account account : accounts) {
            transferFromGenesisToAccount(neow3j, gasToken, fractions, account);
        }
    }

    static void fundAccountsWithNeo(Neow3j neow3j, BigInteger amount, Account... accounts)
            throws Throwable {
        NeoToken neoToken = new NeoToken(neow3j);
        for (Account account : accounts) {
            transferFromGenesisToAccount(neow3j, neoToken, amount, account);
        }
    }

    static void transferFromGenesisToAccount(Neow3j neow3j, FungibleToken token,
            BigInteger amount, Account a) throws Throwable {

        Transaction unsignedTx = token.transfer(COMMITTEE_ACCOUNT, a.getScriptHash(), amount)
                .getUnsignedTransaction();

        Witness multiSigWitness = createMultiSigWitness(
                asList(signMessage(unsignedTx.getHashData(), DEFAULT_ACCOUNT.getECKeyPair())),
                COMMITTEE_ACCOUNT.getVerificationScript());

        Hash256 txHash = unsignedTx.addWitness(multiSigWitness)
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

    static void registerCandidateAndAwaitExecution(Neow3j neow3j, Account candidate) throws Throwable {
        Hash256 txHash = new NeoToken(neow3j).registerCandidate(candidate.getECKeyPair().getPublicKey())
                .signers(calledByEntry(candidate))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

    static void unregisterCandidateAndAwaitExecution(Neow3j neow3j, Account candidate) throws Throwable {
        Hash256 txHash = new NeoToken(neow3j).unregisterCandidate(candidate.getECKeyPair().getPublicKey())
                .signers(calledByEntry(candidate))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

}
