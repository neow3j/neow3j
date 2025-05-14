package io.neow3j.helper;

import io.neow3j.contract.NeoToken;
import io.neow3j.protocol.Neow3j;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;

import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;

public class CandidateRegistrationHelper {

    public static void registerCandidateAndAwaitExecution(Neow3j neow3j, Account candidate) throws Throwable {
        Hash256 txHash = new NeoToken(neow3j).registerCandidate(candidate.getECKeyPair().getPublicKey())
                .signers(calledByEntry(candidate))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

    public static void unregisterCandidateAndAwaitExecution(Neow3j neow3j, Account candidate) throws Throwable {
        Hash256 txHash = new NeoToken(neow3j).unregisterCandidate(candidate.getECKeyPair().getPublicKey())
                .signers(calledByEntry(candidate))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

}
